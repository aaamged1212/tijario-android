package app.tijario.features.backup

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.FilterInputStream
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import javax.crypto.AEADBadTagException
import javax.crypto.Cipher
import javax.crypto.CipherInputStream
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

@Serializable
data class BackupManifest(
    val formatVersion: Int,
    val roomDatabaseVersion: Int,
    val applicationVersion: String,
    val minimumApplicationVersion: String,
    val accountId: String,
    val installationId: String,
    val backupSequence: Long,
    val createdAtEpochMillis: Long,
    val encryptionVersion: Int,
    val keyVersion: Int,
    val recordCounts: Map<String, Int>,
    val documentCount: Int,
    val documentCreationEventCount: Int,
    val pdfIncludedCount: Int,
    val pdfMissingCount: Int,
    val pdfFailedGenerationCount: Int,
)

@Serializable
private data class BackupChecksums(val files: Map<String, String>)

data class DecodedBackup(
    val manifest: BackupManifest,
    val entries: Map<String, ByteArray>,
    internal val stagedAssetFiles: Map<String, File> = emptyMap(),
    private val stagingRoot: File? = null,
) {
    internal fun discardStaging() {
        stagingRoot?.deleteRecursively()
    }
}

open class BackupValidationException(message: String, cause: Throwable? = null) : Exception(message, cause)

object BackupArchiveCodec {
    internal const val MAX_ARCHIVE_BYTES = 256 * 1024 * 1024
    internal const val MAX_ENTRY_COUNT = 4_096
    internal const val MAX_ENTRY_BYTES = 64 * 1024 * 1024
    internal const val MAX_UNCOMPRESSED_BYTES = 256 * 1024 * 1024
    private const val ARCHIVE_VERSION = 1
    private const val NONCE_SIZE = 12
    private const val GCM_TAG_BITS = 128
    private const val MANIFEST_PATH = "manifest.json"
    private const val CHECKSUMS_PATH = "checksums.json"
    private val magic = "TIJARIO1".encodeToByteArray()
    private val json = Json { encodeDefaults = true; ignoreUnknownKeys = false }
    private const val HEADER_BYTES = 8 + 4 + 4 + NONCE_SIZE + 4
    private val requiredLogicalEntries = setOf(
        "data/documents.json",
        "data/document-items.json",
        "data/document-creation-events.json",
    )

    fun create(
        manifest: BackupManifest,
        logicalEntries: Map<String, ByteArray>,
        encryptionKey: ByteArray,
        secureRandom: SecureRandom = SecureRandom(),
    ): ByteArray {
        validateKey(encryptionKey)
        require(manifest.formatVersion == ARCHIVE_VERSION) { "Unsupported backup format version" }
        require(manifest.encryptionVersion == ARCHIVE_VERSION) { "Unsupported encryption version" }
        require(manifest.accountId.isNotBlank()) { "Backup account is required" }
        require(logicalEntries.keys.containsAll(requiredLogicalEntries)) { "Required structured document data is missing" }
        require(logicalEntries.size + 2 <= MAX_ENTRY_COUNT) { "Backup contains too many entries" }
        require(logicalEntries.values.all { it.size <= MAX_ENTRY_BYTES }) { "Backup entry is too large" }
        require(logicalEntries.values.sumOf { it.size.toLong() } <= MAX_UNCOMPRESSED_BYTES) {
            "Backup content is too large"
        }
        logicalEntries.keys.forEach(::validateRelativePath)

        val manifestBytes = json.encodeToString(manifest).encodeToByteArray()
        val checksums = buildMap {
            put(MANIFEST_PATH, sha256(manifestBytes))
            logicalEntries.toSortedMap().forEach { (path, bytes) -> put(path, sha256(bytes)) }
        }
        val payload = zip(
            buildMap {
                put(MANIFEST_PATH, manifestBytes)
                put(CHECKSUMS_PATH, json.encodeToString(BackupChecksums(checksums)).encodeToByteArray())
                putAll(logicalEntries)
            },
        )

        val nonce = ByteArray(NONCE_SIZE).also(secureRandom::nextBytes)
        val aad = headerAad(manifest.keyVersion)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, SecretKeySpec(encryptionKey, "AES"), GCMParameterSpec(GCM_TAG_BITS, nonce))
        cipher.updateAAD(aad)
        val encryptedPayload = cipher.doFinal(payload)

        return ByteArrayOutputStream().use { output ->
            DataOutputStream(output).use { data ->
                data.write(magic)
                data.writeInt(ARCHIVE_VERSION)
                data.writeInt(manifest.keyVersion)
                data.write(nonce)
                data.writeInt(encryptedPayload.size)
                data.write(encryptedPayload)
            }
            output.toByteArray().also {
                require(it.size <= MAX_ARCHIVE_BYTES) { "Backup archive is too large" }
            }
        }
    }

    fun peekKeyVersion(archive: ByteArray): Int = try {
        DataInputStream(ByteArrayInputStream(archive)).use { input ->
            val actualMagic = ByteArray(magic.size).also(input::readFully)
            if (!actualMagic.contentEquals(magic)) throw BackupValidationException("Invalid Tijario backup header")
            if (input.readInt() != ARCHIVE_VERSION) throw BackupValidationException("Unsupported backup format version")
            input.readInt().also { if (it <= 0) throw BackupValidationException("Backup key version is invalid") }
        }
    } catch (error: BackupValidationException) {
        throw error
    } catch (error: Exception) {
        throw BackupValidationException("Backup header is invalid", error)
    }

    fun peekKeyVersion(archiveFile: File): Int = try {
        if (!archiveFile.isFile || archiveFile.length() < HEADER_BYTES) {
            throw BackupValidationException("Backup header is invalid")
        }
        DataInputStream(FileInputStream(archiveFile)).use { input ->
            readHeader(input, archiveFile.length()).keyVersion
        }
    } catch (error: BackupValidationException) {
        throw error
    } catch (error: Exception) {
        throw BackupValidationException("Backup header is invalid", error)
    }

    fun open(
        archive: ByteArray,
        encryptionKey: ByteArray,
        expectedAccountId: String,
    ): DecodedBackup {
        validateKey(encryptionKey)
        require(expectedAccountId.isNotBlank()) { "Expected account is required" }
        if (archive.size > MAX_ARCHIVE_BYTES) throw BackupValidationException("Backup archive is too large")

        val payload = try {
            DataInputStream(ByteArrayInputStream(archive)).use { input ->
                val actualMagic = ByteArray(magic.size).also(input::readFully)
                if (!actualMagic.contentEquals(magic)) throw BackupValidationException("Invalid Tijario backup header")
                val archiveVersion = input.readInt()
                if (archiveVersion != ARCHIVE_VERSION) throw BackupValidationException("Unsupported backup format version")
                val keyVersion = input.readInt()
                val nonce = ByteArray(NONCE_SIZE).also(input::readFully)
                val encryptedSize = input.readInt()
                if (encryptedSize <= 0 || encryptedSize > archive.size) {
                    throw BackupValidationException("Invalid encrypted payload length")
                }
                val encryptedPayload = ByteArray(encryptedSize).also(input::readFully)
                if (input.available() != 0) throw BackupValidationException("Unexpected trailing backup data")

                val cipher = Cipher.getInstance("AES/GCM/NoPadding")
                cipher.init(Cipher.DECRYPT_MODE, SecretKeySpec(encryptionKey, "AES"), GCMParameterSpec(GCM_TAG_BITS, nonce))
                cipher.updateAAD(headerAad(keyVersion))
                cipher.doFinal(encryptedPayload)
            }
        } catch (error: BackupValidationException) {
            throw error
        } catch (error: AEADBadTagException) {
            throw BackupValidationException("Backup authentication failed", error)
        } catch (error: Exception) {
            throw BackupValidationException("Backup could not be decrypted", error)
        }

        val entries = unzip(payload)
        val manifestBytes = entries[MANIFEST_PATH] ?: throw BackupValidationException("Backup manifest is missing")
        val checksumsBytes = entries[CHECKSUMS_PATH] ?: throw BackupValidationException("Backup checksums are missing")
        val manifest = decodeManifest(manifestBytes)
        if (manifest.accountId != expectedAccountId) throw BackupValidationException("Backup belongs to a different account")
        if (manifest.formatVersion != ARCHIVE_VERSION || manifest.encryptionVersion != ARCHIVE_VERSION) {
            throw BackupValidationException("Unsupported backup version")
        }

        val checksums = try {
            json.decodeFromString<BackupChecksums>(checksumsBytes.decodeToString()).files
        } catch (error: Exception) {
            throw BackupValidationException("Backup checksums are invalid", error)
        }
        val contentEntries = entries - CHECKSUMS_PATH
        if (checksums.keys != contentEntries.keys) throw BackupValidationException("Backup file inventory does not match checksums")
        checksums.forEach { (path, expectedHash) ->
            val bytes = contentEntries[path] ?: throw BackupValidationException("Backup entry is missing: $path")
            if (sha256(bytes) != expectedHash) throw BackupValidationException("Backup checksum failed: $path")
        }
        if (!contentEntries.keys.containsAll(requiredLogicalEntries)) {
            throw BackupValidationException("Required structured document data is missing")
        }

        return DecodedBackup(manifest, contentEntries - MANIFEST_PATH)
    }

    /**
     * Opens an encrypted archive without materialising the encrypted payload or binary assets in memory.
     * Structured Room snapshots remain bounded byte arrays because they are required for transactional restore.
     */
    fun open(
        archiveFile: File,
        encryptionKey: ByteArray,
        expectedAccountId: String,
        temporaryRoot: File,
    ): DecodedBackup {
        validateKey(encryptionKey)
        require(expectedAccountId.isNotBlank()) { "Expected account is required" }
        if (!archiveFile.isFile || archiveFile.length() > MAX_ARCHIVE_BYTES) {
            throw BackupValidationException("Backup archive is too large")
        }
        val staging = File(temporaryRoot, "archive-${System.nanoTime()}").canonicalFile
        if (!staging.mkdirs()) throw BackupValidationException("Backup staging storage is unavailable")
        try {
            val header = FileInputStream(archiveFile).use { raw ->
                DataInputStream(raw).use { input -> readHeader(input, archiveFile.length()) }
            }
            val payload = File(staging, "payload.zip")
            decryptToFile(archiveFile, payload, encryptionKey, header)
            val decoded = unzipToStaging(payload, staging, expectedAccountId)
            payload.delete()
            return decoded
        } catch (error: Exception) {
            staging.deleteRecursively()
            if (error is BackupValidationException) throw error
            throw BackupValidationException("Backup could not be decrypted", error)
        }
    }

    private fun decodeManifest(bytes: ByteArray): BackupManifest = try {
        json.decodeFromString<BackupManifest>(bytes.decodeToString())
    } catch (error: Exception) {
        throw BackupValidationException("Backup manifest is invalid", error)
    }

    private fun zip(entries: Map<String, ByteArray>): ByteArray = ByteArrayOutputStream().use { output ->
        ZipOutputStream(output).use { zip ->
            entries.toSortedMap().forEach { (path, bytes) ->
                validateRelativePath(path)
                zip.putNextEntry(ZipEntry(path))
                zip.write(bytes)
                zip.closeEntry()
            }
        }
        output.toByteArray()
    }

    private fun unzip(payload: ByteArray): Map<String, ByteArray> {
        val entries = linkedMapOf<String, ByteArray>()
        var uncompressedBytes = 0L
        ZipInputStream(ByteArrayInputStream(payload)).use { zip ->
            var entry = zip.nextEntry
            while (entry != null) {
                if (entries.size >= MAX_ENTRY_COUNT) throw BackupValidationException("Backup contains too many entries")
                val path = entry.name
                try {
                    validateRelativePath(path)
                } catch (error: IllegalArgumentException) {
                    throw BackupValidationException("Backup contains an unsafe path", error)
                }
                if (entry.isDirectory || entries.containsKey(path)) throw BackupValidationException("Backup contains an invalid entry")
                if (entry.size > MAX_ENTRY_BYTES) throw BackupValidationException("Backup entry is too large")
                val remaining = MAX_UNCOMPRESSED_BYTES - uncompressedBytes
                if (remaining <= 0) throw BackupValidationException("Backup content is too large")
                val bytes = readBounded(zip, minOf(MAX_ENTRY_BYTES.toLong(), remaining))
                uncompressedBytes += bytes.size
                entries[path] = bytes
                zip.closeEntry()
                entry = zip.nextEntry
            }
        }
        return entries
    }

    private fun decryptToFile(
        archiveFile: File,
        payloadFile: File,
        encryptionKey: ByteArray,
        header: ArchiveHeader,
    ) {
        try {
            FileInputStream(archiveFile).use { raw ->
                DataInputStream(raw).use { input ->
                    readHeader(input, archiveFile.length())
                    val cipher = Cipher.getInstance("AES/GCM/NoPadding")
                    cipher.init(Cipher.DECRYPT_MODE, SecretKeySpec(encryptionKey, "AES"), GCMParameterSpec(GCM_TAG_BITS, header.nonce))
                    cipher.updateAAD(headerAad(header.keyVersion))
                    LimitedInputStream(input, header.encryptedSize.toLong()).use { encrypted ->
                        CipherInputStream(encrypted, cipher).use { decrypted ->
                            FileOutputStream(payloadFile).use { output -> copyBounded(decrypted, output, MAX_ARCHIVE_BYTES.toLong()) }
                        }
                        if (encrypted.remaining != 0L) throw BackupValidationException("Backup payload is incomplete")
                    }
                }
            }
        } catch (error: BackupValidationException) {
            throw error
        } catch (error: AEADBadTagException) {
            throw BackupValidationException("Backup authentication failed", error)
        } catch (error: Exception) {
            throw BackupValidationException("Backup could not be decrypted", error)
        }
    }

    private fun unzipToStaging(payloadFile: File, stagingRoot: File, expectedAccountId: String): DecodedBackup {
        val entries = linkedMapOf<String, ByteArray>()
        val assets = linkedMapOf<String, File>()
        val hashes = linkedMapOf<String, String>()
        var uncompressedBytes = 0L
        val assetsRoot = File(stagingRoot, "assets").canonicalFile
        if (!assetsRoot.mkdirs()) throw BackupValidationException("Backup asset staging is unavailable")
        ZipInputStream(FileInputStream(payloadFile)).use { zip ->
            var entry = zip.nextEntry
            while (entry != null) {
                if (hashes.size >= MAX_ENTRY_COUNT) throw BackupValidationException("Backup contains too many entries")
                val path = entry.name
                try {
                    validateRelativePath(path)
                } catch (error: IllegalArgumentException) {
                    throw BackupValidationException("Backup contains an unsafe path", error)
                }
                if (entry.isDirectory || hashes.containsKey(path)) throw BackupValidationException("Backup contains an invalid entry")
                if (entry.size > MAX_ENTRY_BYTES) throw BackupValidationException("Backup entry is too large")
                val remaining = MAX_UNCOMPRESSED_BYTES - uncompressedBytes
                if (remaining <= 0) throw BackupValidationException("Backup content is too large")
                val digest = MessageDigest.getInstance("SHA-256")
                val size = if (path.startsWith("assets/")) {
                    val target = File(assetsRoot, path.removePrefix("assets/")).canonicalFile
                    if (!target.toPath().startsWith(assetsRoot.toPath())) throw BackupValidationException("Backup asset path is unsafe")
                    target.parentFile?.mkdirs()
                    FileOutputStream(target).use { output -> copyBounded(zip, output, minOf(MAX_ENTRY_BYTES.toLong(), remaining), digest) }
                        .also { assets[path] = target }
                } else {
                    val bytes = readBoundedWithDigest(zip, minOf(MAX_ENTRY_BYTES.toLong(), remaining), digest)
                    entries[path] = bytes
                    bytes.size.toLong()
                }
                uncompressedBytes += size
                hashes[path] = digest.digest().joinToString("") { "%02x".format(it) }
                zip.closeEntry()
                entry = zip.nextEntry
            }
        }
        val manifestBytes = entries[MANIFEST_PATH] ?: throw BackupValidationException("Backup manifest is missing")
        val checksumsBytes = entries.remove(CHECKSUMS_PATH) ?: throw BackupValidationException("Backup checksums are missing")
        val manifest = decodeManifest(manifestBytes)
        if (manifest.accountId != expectedAccountId) throw BackupValidationException("Backup belongs to a different account")
        if (manifest.formatVersion != ARCHIVE_VERSION || manifest.encryptionVersion != ARCHIVE_VERSION) {
            throw BackupValidationException("Unsupported backup version")
        }
        val checksums = try {
            json.decodeFromString<BackupChecksums>(checksumsBytes.decodeToString()).files
        } catch (error: Exception) {
            throw BackupValidationException("Backup checksums are invalid", error)
        }
        val contentHashes = hashes - CHECKSUMS_PATH
        if (checksums.keys != contentHashes.keys || checksums.any { (path, value) -> contentHashes[path] != value }) {
            throw BackupValidationException("Backup file inventory does not match checksums")
        }
        if (!entries.keys.containsAll(requiredLogicalEntries)) {
            throw BackupValidationException("Required structured document data is missing")
        }
        return DecodedBackup(manifest, entries - MANIFEST_PATH, assets, stagingRoot)
    }

    private fun readBounded(input: ZipInputStream, limit: Long): ByteArray {
        val output = ByteArrayOutputStream()
        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
        var total = 0L
        while (true) {
            val read = input.read(buffer)
            if (read < 0) break
            total += read
            if (total > limit) throw BackupValidationException("Backup entry is too large")
            output.write(buffer, 0, read)
        }
        return output.toByteArray()
    }

    private fun readBoundedWithDigest(input: ZipInputStream, limit: Long, digest: MessageDigest): ByteArray {
        val output = ByteArrayOutputStream()
        copyBounded(input, output, limit, digest)
        return output.toByteArray()
    }

    private fun copyBounded(input: java.io.InputStream, output: java.io.OutputStream, limit: Long, digest: MessageDigest? = null): Long {
        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
        var total = 0L
        while (true) {
            val count = input.read(buffer)
            if (count < 0) break
            total += count
            if (total > limit) throw BackupValidationException("Backup entry is too large")
            output.write(buffer, 0, count)
            digest?.update(buffer, 0, count)
        }
        return total
    }

    private fun readHeader(input: DataInputStream, archiveLength: Long): ArchiveHeader {
        val actualMagic = ByteArray(magic.size).also(input::readFully)
        if (!actualMagic.contentEquals(magic)) throw BackupValidationException("Invalid Tijario backup header")
        if (input.readInt() != ARCHIVE_VERSION) throw BackupValidationException("Unsupported backup format version")
        val keyVersion = input.readInt().also { if (it <= 0) throw BackupValidationException("Backup key version is invalid") }
        val nonce = ByteArray(NONCE_SIZE).also(input::readFully)
        val encryptedSize = input.readInt()
        if (encryptedSize <= 0 || archiveLength != HEADER_BYTES + encryptedSize.toLong()) {
            throw BackupValidationException("Invalid encrypted payload length")
        }
        return ArchiveHeader(keyVersion, nonce, encryptedSize)
    }

    private data class ArchiveHeader(val keyVersion: Int, val nonce: ByteArray, val encryptedSize: Int)

    private class LimitedInputStream(input: java.io.InputStream, initialRemaining: Long) : FilterInputStream(input) {
        var remaining = initialRemaining
            private set

        override fun read(): Int {
            if (remaining <= 0) return -1
            val value = super.read()
            if (value >= 0) remaining--
            return value
        }

        override fun read(buffer: ByteArray, offset: Int, length: Int): Int {
            if (remaining <= 0) return -1
            val read = super.read(buffer, offset, minOf(length.toLong(), remaining).toInt())
            if (read > 0) remaining -= read.toLong()
            return read
        }
    }

    private fun validateRelativePath(path: String) {
        require(path.isNotBlank() && !path.startsWith('/') && '\\' !in path) { "Invalid backup path" }
        require(path.split('/').none { it.isBlank() || it == "." || it == ".." }) { "Invalid backup path" }
    }

    private fun validateKey(key: ByteArray) {
        require(key.size in setOf(16, 24, 32)) { "AES key must be 128, 192, or 256 bits" }
    }

    private fun headerAad(keyVersion: Int): ByteArray = ByteArrayOutputStream().use { output ->
        DataOutputStream(output).use { data ->
            data.write(magic)
            data.writeInt(ARCHIVE_VERSION)
            data.writeInt(keyVersion)
        }
        output.toByteArray()
    }

    private fun sha256(bytes: ByteArray): String =
        MessageDigest.getInstance("SHA-256").digest(bytes).joinToString("") { "%02x".format(it) }
}

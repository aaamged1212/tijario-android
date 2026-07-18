package app.tijario.features.backup

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import javax.crypto.AEADBadTagException
import javax.crypto.Cipher
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
)

class BackupValidationException(message: String, cause: Throwable? = null) : Exception(message, cause)

object BackupArchiveCodec {
    private const val ARCHIVE_VERSION = 1
    private const val NONCE_SIZE = 12
    private const val GCM_TAG_BITS = 128
    private const val MANIFEST_PATH = "manifest.json"
    private const val CHECKSUMS_PATH = "checksums.json"
    private val magic = "TIJARIO1".encodeToByteArray()
    private val json = Json { encodeDefaults = true; ignoreUnknownKeys = false }
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
            output.toByteArray()
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

    fun open(
        archive: ByteArray,
        encryptionKey: ByteArray,
        expectedAccountId: String,
    ): DecodedBackup {
        validateKey(encryptionKey)
        require(expectedAccountId.isNotBlank()) { "Expected account is required" }

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
        ZipInputStream(ByteArrayInputStream(payload)).use { zip ->
            var entry = zip.nextEntry
            while (entry != null) {
                val path = entry.name
                try {
                    validateRelativePath(path)
                } catch (error: IllegalArgumentException) {
                    throw BackupValidationException("Backup contains an unsafe path", error)
                }
                if (entry.isDirectory || entries.containsKey(path)) throw BackupValidationException("Backup contains an invalid entry")
                entries[path] = zip.readBytes()
                zip.closeEntry()
                entry = zip.nextEntry
            }
        }
        return entries
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

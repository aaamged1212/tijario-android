package app.tijario.features.backup

import java.io.File
import java.io.InputStream
import java.security.MessageDigest
import java.util.UUID

/** Copies an untrusted archive to private storage before it is inspected or restored. */
internal object BackupArchiveInputStager {
    data class StagedArchive(
        val file: File,
        val checksum: String,
        val sizeBytes: Long,
    ) {
        fun delete() {
            file.delete()
            file.parentFile?.takeIf { it.name == "restore-input" }?.delete()
        }
    }

    fun copyToPrivateFile(
        input: InputStream,
        filesRoot: File,
        userId: String,
        maxBytes: Long = BackupArchiveCodec.MAX_ARCHIVE_BYTES.toLong(),
    ): StagedArchive {
        require(userId.isNotBlank()) { "Restore account is required" }
        val root = File(filesRoot, "users/$userId/restore-input").canonicalFile
        if (!root.mkdirs() && !root.isDirectory) {
            throw BackupValidationException("Restore temporary storage is unavailable")
        }
        val temporary = File(root, ".${UUID.randomUUID()}.tijario.tmp")
        val digest = MessageDigest.getInstance("SHA-256")
        var total = 0L
        try {
            temporary.outputStream().use { output ->
                val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                while (true) {
                    val count = input.read(buffer)
                    if (count < 0) break
                    total += count
                    if (total > maxBytes) throw BackupValidationException("Backup archive is too large")
                    output.write(buffer, 0, count)
                    digest.update(buffer, 0, count)
                }
                output.flush()
            }
            if (total == 0L) throw BackupValidationException("Backup archive is empty")
            return StagedArchive(
                file = temporary,
                checksum = digest.digest().joinToString("") { "%02x".format(it) },
                sizeBytes = total,
            )
        } catch (error: Exception) {
            temporary.delete()
            if (error is BackupValidationException) throw error
            throw BackupValidationException("Backup archive could not be read", error)
        }
    }
}

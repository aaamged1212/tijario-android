package app.tijario.features.backup

import java.io.File
import java.io.FileOutputStream
import java.nio.file.StandardCopyOption
import java.security.MessageDigest

data class StoredBackupFile(
    val file: File,
    val sizeBytes: Long,
    val checksum: String,
)

object AtomicBackupFileStore {
    private val safeName = Regex("Tijario-Backup-[0-9]{8}-[0-9]{4}-[A-Za-z0-9-]+\\.tijario")

    fun writeVerified(
        directory: File,
        fileName: String,
        archive: ByteArray,
        verify: (ByteArray) -> Unit,
    ): StoredBackupFile {
        require(safeName.matches(fileName)) { "Backup file name is invalid" }
        require(archive.isNotEmpty()) { "Backup archive is empty" }
        if (!directory.exists() && !directory.mkdirs()) throw BackupValidationException("Backup directory could not be created")
        val canonicalDirectory = directory.canonicalFile
        val finalFile = File(canonicalDirectory, fileName).canonicalFile
        if (finalFile.parentFile != canonicalDirectory) throw BackupValidationException("Backup destination is unsafe")
        val temporaryFile = File(canonicalDirectory, ".$fileName.tmp")

        try {
            FileOutputStream(temporaryFile).use { output ->
                output.write(archive)
                output.fd.sync()
            }
            val persisted = temporaryFile.readBytes()
            verify(persisted)
            val checksum = sha256(persisted)
            try {
                java.nio.file.Files.move(
                    temporaryFile.toPath(),
                    finalFile.toPath(),
                    StandardCopyOption.ATOMIC_MOVE,
                    StandardCopyOption.REPLACE_EXISTING,
                )
            } catch (_: java.nio.file.AtomicMoveNotSupportedException) {
                java.nio.file.Files.move(
                    temporaryFile.toPath(),
                    finalFile.toPath(),
                    StandardCopyOption.REPLACE_EXISTING,
                )
            }
            return StoredBackupFile(finalFile, finalFile.length(), checksum)
        } catch (error: Exception) {
            temporaryFile.delete()
            if (error is BackupValidationException) throw error
            throw BackupValidationException("Backup file could not be finalized", error)
        }
    }

    private fun sha256(bytes: ByteArray): String =
        MessageDigest.getInstance("SHA-256").digest(bytes).joinToString("") { "%02x".format(it) }
}

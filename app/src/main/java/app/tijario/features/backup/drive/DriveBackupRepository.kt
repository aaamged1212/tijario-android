package app.tijario.features.backup.drive

import app.tijario.data.local.BackupRecordEntity
import app.tijario.data.local.TijarioDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.File
import java.security.MessageDigest

data class DriveFolderIds(val rootId: String, val backupsId: String)

class DriveFolderRepository(private val client: DriveBackupClient) {
    suspend fun resolve(): DriveFolderIds = folderMutex.withLock {
        val root = client.findFolder(TIJARIO_DRIVE_FOLDER, null)
            ?: client.createFolder(TIJARIO_DRIVE_FOLDER, null)
        val backups = client.findFolder(TIJARIO_BACKUPS_FOLDER, root)
            ?: client.createFolder(TIJARIO_BACKUPS_FOLDER, root)
        return DriveFolderIds(root, backups)
    }

    private companion object {
        val folderMutex = Mutex()
    }
}

class DriveBackupRepository(
    private val database: TijarioDatabase,
    private val filesRoot: File,
    private val client: DriveBackupClient,
) {
    suspend fun connectionState(): DriveConnectionState = client.connectionState()

    suspend fun upload(userId: String, backupId: String): BackupRecordEntity = withContext(Dispatchers.IO) {
        val record = database.tijarioDao().getBackupRecords(userId).firstOrNull { it.id == backupId }
            ?: throw DriveBackupException.Permanent("Local backup was not found")
        val file = safeLocalFile(record)
        verifyLocalChecksum(file, record.checksum)
        val folders = DriveFolderRepository(client).resolve()
        val existing = client.findBackup(folders.backupsId, userId, record.id)
        val remote = existing ?: client.uploadBackup(
            folders.backupsId,
            file,
            DriveUploadMetadata(userId, record.id, requireNotNull(record.checksum), record.createdAt),
        )
        if (remote.accountId != userId || remote.checksum != record.checksum) {
            throw DriveBackupException.IntegrityFailure()
        }
        record.copy(
            status = "DRIVE_UPLOADED",
            uploadedAt = System.currentTimeMillis(),
            driveFileId = remote.id,
            lastError = null,
        ).also { database.tijarioDao().upsertBackupRecord(it) }
    }

    suspend fun list(userId: String): List<DriveBackupFile> {
        val folders = DriveFolderRepository(client).resolve()
        return client.listBackups(folders.backupsId, userId)
    }

    suspend fun download(userId: String, remote: DriveBackupFile, destination: File): File = withContext(Dispatchers.IO) {
        if (remote.accountId != userId) throw DriveBackupException.AccountMismatch()
        val temporary = File(destination.parentFile, ".${destination.name}.download")
        runCatching {
            client.downloadBackup(remote.id, temporary)
            if (sha256(temporary) != remote.checksum) throw DriveBackupException.IntegrityFailure()
            destination.parentFile?.mkdirs()
            if (destination.exists() && !destination.delete()) error("Could not replace downloaded backup")
            if (!temporary.renameTo(destination)) {
                temporary.copyTo(destination, overwrite = true)
                temporary.delete()
            }
            destination
        }.getOrElse {
            temporary.delete()
            throw it
        }
    }

    suspend fun prune(userId: String, keep: Int) {
        if (keep < 1) return
        list(userId).drop(keep).forEach { client.deleteFile(it.id) }
    }

    suspend fun delete(userId: String, remote: DriveBackupFile) {
        if (remote.accountId != userId) throw DriveBackupException.AccountMismatch()
        val backups = list(userId)
        val current = backups.firstOrNull { it.id == remote.id }
            ?: throw DriveBackupException.Permanent("Drive backup was not found")
        if (backups.firstOrNull()?.id == current.id) {
            throw DriveBackupException.Permanent("The newest Drive backup is retained")
        }
        client.deleteFile(current.id)
    }

    private fun safeLocalFile(record: BackupRecordEntity): File {
        val root = filesRoot.canonicalFile
        val file = File(root, record.localRelativePath).canonicalFile
        if (!file.toPath().startsWith(root.toPath()) || !file.isFile) {
            throw DriveBackupException.Permanent("Local backup path is invalid")
        }
        return file
    }

    private fun verifyLocalChecksum(file: File, expected: String?) {
        if (expected.isNullOrBlank() || sha256(file) != expected) throw DriveBackupException.IntegrityFailure()
    }

    private fun sha256(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")
        file.inputStream().use { input ->
            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
            while (true) {
                val count = input.read(buffer)
                if (count < 0) break
                digest.update(buffer, 0, count)
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }
}

object DriveBackupRuntime {
    fun client(context: android.content.Context, userId: String): DriveBackupClient =
        BackupDriveContainer.runtime(context, userId).client
}

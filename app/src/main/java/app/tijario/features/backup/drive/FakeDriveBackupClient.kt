package app.tijario.features.backup.drive

import java.io.File
import java.security.MessageDigest
import java.util.UUID

class FakeDriveBackupClient(
    private var state: DriveConnectionState = DriveConnectionState.Connected("fake-drive-account"),
) : DriveBackupClient {
    private data class Folder(val id: String, val name: String, val parentId: String?)
    private data class Stored(val metadata: DriveBackupFile, val bytes: ByteArray, val folderId: String)

    private val folders = linkedMapOf<String, Folder>()
    private val files = linkedMapOf<String, Stored>()

    fun setConnectionState(value: DriveConnectionState) {
        state = value
    }

    override suspend fun connectionState() = state

    override suspend fun findFolder(name: String, parentId: String?): String? =
        folders.values.firstOrNull { it.name == name && it.parentId == parentId }?.id

    override suspend fun createFolder(name: String, parentId: String?): String {
        requireConnected()
        return findFolder(name, parentId) ?: UUID.randomUUID().toString().also {
            folders[it] = Folder(it, name, parentId)
        }
    }

    override suspend fun findBackup(folderId: String, accountId: String, backupId: String): DriveBackupFile? =
        files.values.firstOrNull {
            it.folderId == folderId && it.metadata.accountId == accountId && it.metadata.backupId == backupId
        }?.metadata

    override suspend fun uploadBackup(folderId: String, file: File, metadata: DriveUploadMetadata): DriveBackupFile {
        requireConnected(metadata.accountId)
        require(folders.containsKey(folderId)) { "Drive folder does not exist" }
        val bytes = file.readBytes()
        val checksum = sha256(bytes)
        if (checksum != metadata.checksum) throw DriveBackupException.IntegrityFailure()
        val existing = findBackup(folderId, metadata.accountId, metadata.backupId)
        if (existing != null) return existing
        val id = UUID.randomUUID().toString()
        val result = DriveBackupFile(
            id = id,
            name = file.name,
            sizeBytes = bytes.size.toLong(),
            checksum = checksum,
            accountId = metadata.accountId,
            backupId = metadata.backupId,
            createdAt = metadata.createdAt,
        )
        files[id] = Stored(result, bytes, folderId)
        return result
    }

    override suspend fun listBackups(folderId: String, accountId: String): List<DriveBackupFile> {
        requireConnected(accountId)
        return files.values.filter { it.folderId == folderId && it.metadata.accountId == accountId }
            .map(Stored::metadata)
            .sortedByDescending(DriveBackupFile::createdAt)
    }

    override suspend fun downloadBackup(fileId: String, destination: File) {
        requireConnected()
        val stored = files[fileId] ?: throw DriveBackupException.Permanent("Drive backup was not found")
        destination.parentFile?.mkdirs()
        destination.writeBytes(stored.bytes)
    }

    override suspend fun deleteFile(fileId: String) {
        requireConnected()
        files.remove(fileId)
    }

    private fun requireConnected(expectedAccountId: String? = null) {
        when (val current = state) {
            DriveConnectionState.NotConfigured -> throw DriveBackupException.NotConfigured()
            DriveConnectionState.Disconnected -> throw DriveBackupException.NotConnected()
            is DriveConnectionState.Connected -> if (expectedAccountId != null && current.accountId != expectedAccountId) {
                throw DriveBackupException.AccountMismatch()
            }
        }
    }

    private fun sha256(bytes: ByteArray): String = MessageDigest.getInstance("SHA-256")
        .digest(bytes)
        .joinToString("") { "%02x".format(it) }
}

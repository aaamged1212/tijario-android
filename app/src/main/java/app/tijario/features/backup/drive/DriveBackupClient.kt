package app.tijario.features.backup.drive

import java.io.File

const val TIJARIO_DRIVE_FOLDER = "Tijario | تجاريو"
const val TIJARIO_BACKUPS_FOLDER = "Backups | النسخ الاحتياطية"

data class DriveBackupFile(
    val id: String,
    val name: String,
    val sizeBytes: Long,
    val checksum: String,
    val accountId: String,
    val backupId: String,
    val createdAt: Long,
)

data class DriveUploadMetadata(
    val accountId: String,
    val backupId: String,
    val checksum: String,
    val createdAt: Long,
)

sealed class DriveConnectionState {
    data object NotConfigured : DriveConnectionState()
    data object Disconnected : DriveConnectionState()
    data object AuthorizationRequired : DriveConnectionState()
    data object Authorizing : DriveConnectionState()
    data object ReauthorizationRequired : DriveConnectionState()
    data object InvalidConfiguration : DriveConnectionState()
    data object TemporarilyUnavailable : DriveConnectionState()
    data class Connected(val accountId: String, val accountEmail: String? = null) : DriveConnectionState()
}

sealed class DriveBackupException(message: String, cause: Throwable? = null) : Exception(message, cause) {
    class NotConfigured : DriveBackupException("Google Drive backup is not configured")
    class NotConnected : DriveBackupException("Google Drive account is not connected")
    class AccountMismatch : DriveBackupException("Google Drive account does not match the Tijario account")
    class IntegrityFailure : DriveBackupException("Downloaded backup checksum does not match")
    class ReauthorizationRequired : DriveBackupException("Google Drive authorization is required")
    class PermissionDenied : DriveBackupException("Google Drive permission was denied")
    class InvalidRequest : DriveBackupException("Google Drive rejected the connection request")
    class Retryable(message: String, cause: Throwable? = null) : DriveBackupException(message, cause)
    class Permanent(message: String, cause: Throwable? = null) : DriveBackupException(message, cause)
}

interface DriveBackupClient {
    suspend fun connectionState(): DriveConnectionState
    suspend fun findFolder(name: String, parentId: String?): String?
    suspend fun createFolder(name: String, parentId: String?): String
    suspend fun findBackup(folderId: String, accountId: String, backupId: String): DriveBackupFile?
    suspend fun uploadBackup(
        folderId: String,
        file: File,
        metadata: DriveUploadMetadata,
        onProgress: suspend (bytesTransferred: Long, totalBytes: Long) -> Unit = { _, _ -> },
    ): DriveBackupFile
    suspend fun listBackups(folderId: String, accountId: String): List<DriveBackupFile>
    suspend fun downloadBackup(
        fileId: String,
        destination: File,
        onProgress: suspend (bytesTransferred: Long, totalBytes: Long) -> Unit = { _, _ -> },
    )
    suspend fun deleteFile(fileId: String)
    fun openFolderUrl(folderId: String): String = "https://drive.google.com/drive/folders/$folderId"
}

object UnavailableDriveBackupClient : DriveBackupClient {
    override suspend fun connectionState() = DriveConnectionState.NotConfigured
    override suspend fun findFolder(name: String, parentId: String?) = throw DriveBackupException.NotConfigured()
    override suspend fun createFolder(name: String, parentId: String?) = throw DriveBackupException.NotConfigured()
    override suspend fun findBackup(folderId: String, accountId: String, backupId: String) = throw DriveBackupException.NotConfigured()
    override suspend fun uploadBackup(folderId: String, file: File, metadata: DriveUploadMetadata, onProgress: suspend (Long, Long) -> Unit) = throw DriveBackupException.NotConfigured()
    override suspend fun listBackups(folderId: String, accountId: String) = throw DriveBackupException.NotConfigured()
    override suspend fun downloadBackup(fileId: String, destination: File, onProgress: suspend (Long, Long) -> Unit) = throw DriveBackupException.NotConfigured()
    override suspend fun deleteFile(fileId: String) = throw DriveBackupException.NotConfigured()
}

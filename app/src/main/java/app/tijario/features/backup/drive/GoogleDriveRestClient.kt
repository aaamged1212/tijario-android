package app.tijario.features.backup.drive

import java.io.File

data class DriveRestFile(
    val id: String,
    val name: String,
    val mimeType: String,
    val parents: List<String> = emptyList(),
    val sizeBytes: Long = 0,
    val checksum: String? = null,
    val createdAt: Long = 0,
    val appProperties: Map<String, String> = emptyMap(),
)

/**
 * Streaming HTTP boundary for Google Drive REST v3. The OAuth module supplies
 * the access token and the implementation streams files without loading an
 * archive into memory.
 */
interface DriveRestTransport {
    suspend fun list(accessToken: String, query: String): List<DriveRestFile>
    suspend fun createFolder(accessToken: String, name: String, parentId: String?): DriveRestFile
    suspend fun uploadFile(
        accessToken: String,
        parentId: String,
        file: File,
        mimeType: String,
        appProperties: Map<String, String>,
    ): DriveRestFile
    suspend fun downloadFile(accessToken: String, fileId: String, destination: File)
    suspend fun deleteFile(accessToken: String, fileId: String)
}

class GoogleDriveRestClient(
    private val tokenProvider: suspend () -> String?,
    private val driveAccountIdProvider: suspend () -> String?,
    private val transport: DriveRestTransport,
) : DriveBackupClient {
    override suspend fun connectionState(): DriveConnectionState {
        val token = tokenProvider()
        val accountId = driveAccountIdProvider()
        return if (token.isNullOrBlank() || accountId.isNullOrBlank()) {
            DriveConnectionState.Disconnected
        } else {
            DriveConnectionState.Connected(accountId)
        }
    }

    override suspend fun findFolder(name: String, parentId: String?): String? =
        transport.list(token(), folderQuery(name, parentId)).firstOrNull()?.id

    override suspend fun createFolder(name: String, parentId: String?): String =
        transport.createFolder(token(), name, parentId).id

    override suspend fun findBackup(folderId: String, accountId: String, backupId: String): DriveBackupFile? =
        transport.list(token(), backupQuery(folderId, accountId, backupId)).firstOrNull()?.toBackupFile()

    override suspend fun uploadBackup(
        folderId: String,
        file: File,
        metadata: DriveUploadMetadata,
    ): DriveBackupFile {
        requireConnectedDriveAccount()
        return transport.uploadFile(
            accessToken = token(),
            parentId = folderId,
            file = file,
            mimeType = BACKUP_MIME_TYPE,
            appProperties = mapOf(
                "tijarioAccountId" to metadata.accountId,
                "tijarioBackupId" to metadata.backupId,
                "tijarioChecksum" to metadata.checksum,
                "tijarioCreatedAt" to metadata.createdAt.toString(),
            ),
        ).toBackupFile()
    }

    override suspend fun listBackups(folderId: String, accountId: String): List<DriveBackupFile> {
        requireConnectedDriveAccount()
        return transport.list(token(), accountBackupQuery(folderId, accountId))
            .map { it.toBackupFile() }
            .sortedByDescending(DriveBackupFile::createdAt)
    }

    override suspend fun downloadBackup(fileId: String, destination: File) =
        transport.downloadFile(token(), fileId, destination)

    override suspend fun deleteFile(fileId: String) = transport.deleteFile(token(), fileId)

    private suspend fun token(): String = tokenProvider()?.takeIf(String::isNotBlank)
        ?: throw DriveBackupException.NotConnected()

    private suspend fun requireConnectedDriveAccount() {
        driveAccountIdProvider()?.takeIf(String::isNotBlank)
            ?: throw DriveBackupException.NotConnected()
    }

    private fun DriveRestFile.toBackupFile(): DriveBackupFile {
        val accountId = appProperties["tijarioAccountId"] ?: throw DriveBackupException.Permanent("Drive backup account metadata is missing")
        val backupId = appProperties["tijarioBackupId"] ?: throw DriveBackupException.Permanent("Drive backup identity metadata is missing")
        val checksumValue = appProperties["tijarioChecksum"] ?: checksum
            ?: throw DriveBackupException.Permanent("Drive backup checksum metadata is missing")
        return DriveBackupFile(
            id = id,
            name = name,
            sizeBytes = sizeBytes,
            checksum = checksumValue,
            accountId = accountId,
            backupId = backupId,
            createdAt = appProperties["tijarioCreatedAt"]?.toLongOrNull() ?: createdAt,
        )
    }

    private fun folderQuery(name: String, parentId: String?): String = buildList {
        add("trashed = false")
        add("mimeType = '$FOLDER_MIME_TYPE'")
        add("name = '${escape(name)}'")
        if (parentId != null) add("'${escape(parentId)}' in parents")
    }.joinToString(" and ")

    private fun backupQuery(folderId: String, accountId: String, backupId: String): String =
        accountBackupQuery(folderId, accountId) + " and appProperties has { key='tijarioBackupId' and value='${escape(backupId)}' }"

    private fun accountBackupQuery(folderId: String, accountId: String): String =
        "trashed = false and '${escape(folderId)}' in parents" +
            " and appProperties has { key='tijarioAccountId' and value='${escape(accountId)}' }"

    private fun escape(value: String) = value.replace("\\", "\\\\").replace("'", "\\'")

    companion object {
        const val BACKUP_MIME_TYPE = "application/vnd.tijario.backup"
        const val FOLDER_MIME_TYPE = "application/vnd.google-apps.folder"
    }
}

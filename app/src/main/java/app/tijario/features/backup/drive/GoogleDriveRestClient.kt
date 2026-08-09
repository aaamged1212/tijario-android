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

data class DriveCurrentUser(
    val permissionId: String,
    val emailAddress: String?,
)

/**
 * Streaming HTTP boundary for Google Drive REST v3. The OAuth module supplies
 * the access token and the implementation streams files without loading an
 * archive into memory.
 */
interface DriveRestTransport {
    suspend fun getCurrentUser(accessToken: String): DriveCurrentUser
    suspend fun list(accessToken: String, query: String): List<DriveRestFile>
    suspend fun createFolder(accessToken: String, name: String, parentId: String?): DriveRestFile
    suspend fun uploadFile(
        accessToken: String,
        parentId: String,
        file: File,
        mimeType: String,
        appProperties: Map<String, String>,
        onProgress: suspend (bytesTransferred: Long, totalBytes: Long) -> Unit = { _, _ -> },
    ): DriveRestFile
    suspend fun downloadFile(
        accessToken: String,
        fileId: String,
        destination: File,
        onProgress: suspend (bytesTransferred: Long, totalBytes: Long) -> Unit = { _, _ -> },
    )
    suspend fun deleteFile(accessToken: String, fileId: String)
}

class GoogleDriveRestClient(
    private val tokenProvider: suspend () -> String?,
    private val driveAccountIdProvider: suspend () -> String?,
    private val transport: DriveRestTransport,
    private val onAuthorizationInvalid: suspend () -> Unit = {},
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

    override suspend fun findFolder(name: String, parentId: String?): String? = request {
        transport.list(token(), folderQuery(name, parentId)).firstOrNull()?.id
    }

    override suspend fun createFolder(name: String, parentId: String?): String = request {
        transport.createFolder(token(), name, parentId).id
    }

    override suspend fun findBackup(folderId: String, accountId: String, backupId: String): DriveBackupFile? = request {
        transport.list(token(), backupQuery(folderId, accountId, backupId)).firstOrNull()?.toBackupFile()
    }

    override suspend fun uploadBackup(
        folderId: String,
        file: File,
        metadata: DriveUploadMetadata,
        onProgress: suspend (Long, Long) -> Unit,
    ): DriveBackupFile {
        requireConnectedDriveAccount()
        val uploaded = request { transport.uploadFile(
            accessToken = token(),
            parentId = folderId,
            file = file,
            mimeType = BACKUP_MIME_TYPE,
            onProgress = onProgress,
            appProperties = mapOf(
                "tijario_account_id" to metadata.accountId,
                "tijario_backup_id" to metadata.backupId,
                "checksum_sha256" to metadata.checksum,
                "created_at" to metadata.createdAt.toString(),
                "format_version" to "1",
            ),
        ) }
        if (uploaded.hasVerifiableBackupMetadata()) return uploaded.toBackupFile()

        // Resumable uploads can return a partial File resource after creating the remote file.
        val resolved = request { transport.list(token(), backupQuery(folderId, metadata.accountId, metadata.backupId)) }
            .firstOrNull()
            ?.takeIf { it.hasVerifiableBackupMetadata() }
            ?: throw DriveBackupException.Retryable("Uploaded backup metadata is not available yet")
        return resolved.toBackupFile()
    }

    override suspend fun listBackups(folderId: String, accountId: String): List<DriveBackupFile> {
        requireConnectedDriveAccount()
        return request { transport.list(token(), accountBackupQuery(folderId, accountId)) }
            .map { it.toBackupFile() }
            .sortedByDescending(DriveBackupFile::createdAt)
    }

    override suspend fun downloadBackup(fileId: String, destination: File, onProgress: suspend (Long, Long) -> Unit) = request {
        transport.downloadFile(token(), fileId, destination, onProgress)
    }

    override suspend fun deleteFile(fileId: String) = request { transport.deleteFile(token(), fileId) }

    private suspend fun token(): String = tokenProvider()?.takeIf(String::isNotBlank)
        ?: throw DriveBackupException.NotConnected()

    private suspend fun requireConnectedDriveAccount() {
        driveAccountIdProvider()?.takeIf(String::isNotBlank)
            ?: throw DriveBackupException.NotConnected()
    }

    private fun DriveRestFile.toBackupFile(): DriveBackupFile {
        val accountId = appProperties["tijario_account_id"] ?: throw DriveBackupException.Permanent("Drive backup account metadata is missing")
        val backupId = appProperties["tijario_backup_id"] ?: throw DriveBackupException.Permanent("Drive backup identity metadata is missing")
        val checksumValue = appProperties["checksum_sha256"] ?: checksum
            ?: throw DriveBackupException.Permanent("Drive backup checksum metadata is missing")
        return DriveBackupFile(
            id = id,
            name = name,
            sizeBytes = sizeBytes,
            checksum = checksumValue,
            accountId = accountId,
            backupId = backupId,
            createdAt = appProperties["created_at"]?.toLongOrNull() ?: createdAt,
        )
    }

    private fun DriveRestFile.hasVerifiableBackupMetadata(): Boolean =
        sizeBytes > 0L &&
            !appProperties["tijario_account_id"].isNullOrBlank() &&
            !appProperties["tijario_backup_id"].isNullOrBlank() &&
            !(appProperties["checksum_sha256"] ?: checksum).isNullOrBlank()

    private fun folderQuery(name: String, parentId: String?): String = buildList {
        add("trashed = false")
        add("mimeType = '$FOLDER_MIME_TYPE'")
        add("name = '${escape(name)}'")
        if (parentId != null) add("'${escape(parentId)}' in parents")
    }.joinToString(" and ")

    private fun backupQuery(folderId: String, accountId: String, backupId: String): String =
        accountBackupQuery(folderId, accountId) + " and appProperties has { key='tijario_backup_id' and value='${escape(backupId)}' }"

    private fun accountBackupQuery(folderId: String, accountId: String): String =
        "trashed = false and '${escape(folderId)}' in parents" +
            " and appProperties has { key='tijario_account_id' and value='${escape(accountId)}' }"

    private suspend fun <T> request(block: suspend () -> T): T = try {
        block()
    } catch (_: DriveHttpException.Unauthorized) {
        onAuthorizationInvalid()
        throw DriveBackupException.ReauthorizationRequired()
    } catch (_: DriveHttpException.NotConfigured) {
        throw DriveBackupException.NotConfigured()
    } catch (_: DriveHttpException.PermissionDenied) {
        throw DriveBackupException.PermissionDenied()
    } catch (_: DriveHttpException.BadRequest) {
        throw DriveBackupException.InvalidRequest()
    } catch (_: DriveHttpException.NotFound) {
        throw DriveBackupException.Permanent("Drive resource was not found")
    }

    private fun escape(value: String) = value.replace("\\", "\\\\").replace("'", "\\'")

    companion object {
        const val BACKUP_MIME_TYPE = "application/vnd.tijario.backup"
        const val FOLDER_MIME_TYPE = "application/vnd.google-apps.folder"
    }
}

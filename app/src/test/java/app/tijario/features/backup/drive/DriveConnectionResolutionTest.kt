package app.tijario.features.backup.drive

import java.io.File
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DriveConnectionResolutionTest {
    @Test
    fun validDriveFileAuthorizationResolvesPermissionIdWithoutGoogleProfileFields() = runBlocking {
        val metadata = resolveDriveConnectionMetadata(
            transport = CurrentUserTransport(DriveCurrentUser("permission-id-123", null)),
            session = DriveAuthorizationSession("short-lived-token"),
        )

        assertEquals("permission-id-123", metadata.accountId)
        assertNull(metadata.accountEmail)
    }

    @Test(expected = DriveBackupException.InvalidRequest::class)
    fun missingDrivePermissionIdDoesNotPersistConnectionMetadata(): Unit = runBlocking {
        resolveDriveConnectionMetadata(
            transport = CurrentUserTransport(DriveCurrentUser("", "seller@example.com")),
            session = DriveAuthorizationSession("short-lived-token"),
        )
    }

    @Test
    fun resolvedIdentityCanResolveTheTijarioFolders() = runBlocking {
        val metadata = resolveDriveConnectionMetadata(
            transport = CurrentUserTransport(DriveCurrentUser("permission-id-123", null)),
            session = DriveAuthorizationSession("short-lived-token"),
        )
        val folders = DriveFolderRepository(FakeDriveBackupClient(DriveConnectionState.Connected(metadata.accountId))).resolve()

        assertTrue(folders.rootId.isNotBlank())
        assertTrue(folders.backupsId.isNotBlank())
    }

    private class CurrentUserTransport(
        private val currentUser: DriveCurrentUser,
    ) : DriveRestTransport {
        override suspend fun getCurrentUser(accessToken: String): DriveCurrentUser = currentUser
        override suspend fun list(accessToken: String, query: String): List<DriveRestFile> = emptyList()
        override suspend fun createFolder(accessToken: String, name: String, parentId: String?) =
            DriveRestFile("folder", name, GoogleDriveRestClient.FOLDER_MIME_TYPE)
        override suspend fun uploadFile(
            accessToken: String,
            parentId: String,
            file: File,
            mimeType: String,
            appProperties: Map<String, String>,
        ) = DriveRestFile("file", file.name, mimeType)
        override suspend fun downloadFile(accessToken: String, fileId: String, destination: File) = Unit
        override suspend fun deleteFile(accessToken: String, fileId: String) = Unit
    }
}

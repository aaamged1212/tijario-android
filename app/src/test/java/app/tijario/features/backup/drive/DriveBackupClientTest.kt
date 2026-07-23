package app.tijario.features.backup.drive

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertSame
import org.junit.Test
import java.io.File
import java.security.MessageDigest

class DriveBackupClientTest {
    @Test
    fun folderNamesUseTheApprovedArabicVisibleContract() {
        assertEquals("Tijario | تجاريو", TIJARIO_DRIVE_FOLDER)
        assertEquals("Backups | النسخ الاحتياطية", TIJARIO_BACKUPS_FOLDER)
    }

    @Test
    fun folderResolutionCreatesAndReusesTijarioFolders() = runBlocking {
        val client = FakeDriveBackupClient(DriveConnectionState.Connected("account-a"))
        val repository = DriveFolderRepository(client)
        val first = repository.resolve()
        val second = repository.resolve()
        assertEquals(first, second)
        assertNotEquals(first.rootId, first.backupsId)
    }

    @Test
    fun uploadIsDeduplicatedByAccountAndBackupIdentity() = runBlocking {
        val client = FakeDriveBackupClient(DriveConnectionState.Connected("user-1"))
        val folder = DriveFolderRepository(client).resolve().backupsId
        val file = temporaryArchive("encrypted archive")
        val metadata = DriveUploadMetadata("user-1", "backup-1", sha256(file.readBytes()), 42L)
        val first = client.uploadBackup(folder, file, metadata)
        val second = client.uploadBackup(folder, file, metadata)
        assertEquals(first.id, second.id)
        assertEquals(1, client.listBackups(folder, "user-1").size)
    }

    @Test
    fun downloadReturnsExactEncryptedArchive() = runBlocking {
        val client = FakeDriveBackupClient(DriveConnectionState.Connected("user-1"))
        val folder = DriveFolderRepository(client).resolve().backupsId
        val source = temporaryArchive("ciphertext")
        val remote = client.uploadBackup(
            folder,
            source,
            DriveUploadMetadata("user-1", "backup-1", sha256(source.readBytes()), 42L),
        )
        val destination = File.createTempFile("drive-download", ".tijario")
        client.downloadBackup(remote.id, destination)
        assertArrayEquals(source.readBytes(), destination.readBytes())
    }

    @Test(expected = DriveBackupException.AccountMismatch::class)
    fun connectedDriveAccountCannotUploadAnotherAccountsBackup(): Unit = runBlocking {
        val client = FakeDriveBackupClient(DriveConnectionState.Connected("user-1"))
        val folder = DriveFolderRepository(client).resolve().backupsId
        val file = temporaryArchive("ciphertext")
        client.uploadBackup(
            folder,
            file,
            DriveUploadMetadata("user-2", "backup-1", sha256(file.readBytes()), 42L),
        )
    }

    @Test
    fun unavailableClientHasExplicitNotConfiguredState() = runBlocking {
        assertSame(DriveConnectionState.NotConfigured, UnavailableDriveBackupClient.connectionState())
    }

    @Test
    fun googleAccountIdentityIsSeparateFromTijarioArchiveOwnership() = runBlocking {
        val transport = RecordingDriveTransport()
        val client = GoogleDriveRestClient(
            tokenProvider = { "oauth-token" },
            driveAccountIdProvider = { "google-account-123" },
            transport = transport,
        )
        val file = temporaryArchive("ciphertext")

        val remote = client.uploadBackup(
            folderId = "backups-folder",
            file = file,
            metadata = DriveUploadMetadata(
                accountId = "tijario-user-uuid",
                backupId = "backup-1",
                checksum = sha256(file.readBytes()),
                createdAt = 42L,
            ),
        )

        assertEquals("tijario-user-uuid", remote.accountId)
        assertEquals("google-account-123", (client.connectionState() as DriveConnectionState.Connected).accountId)
        assertEquals("tijario-user-uuid", transport.lastUploadProperties["tijario_account_id"])
        assertEquals("backup-1", transport.lastUploadProperties["tijario_backup_id"])
    }

    private class RecordingDriveTransport : DriveRestTransport {
        var lastUploadProperties: Map<String, String> = emptyMap()
        override suspend fun list(accessToken: String, query: String): List<DriveRestFile> = emptyList()
        override suspend fun createFolder(accessToken: String, name: String, parentId: String?) =
            DriveRestFile(id = "folder", name = name, mimeType = GoogleDriveRestClient.FOLDER_MIME_TYPE)

        override suspend fun uploadFile(
            accessToken: String,
            parentId: String,
            file: File,
            mimeType: String,
            appProperties: Map<String, String>,
        ) = DriveRestFile(
            id = "remote-1",
            name = file.name,
            mimeType = mimeType,
            sizeBytes = file.length(),
            appProperties = appProperties.also { lastUploadProperties = it },
        )

        override suspend fun downloadFile(accessToken: String, fileId: String, destination: File) = Unit
        override suspend fun deleteFile(accessToken: String, fileId: String) = Unit
    }

    private fun temporaryArchive(value: String): File = File.createTempFile("drive-backup", ".tijario").apply {
        writeText(value)
        deleteOnExit()
    }

    private fun sha256(bytes: ByteArray): String = MessageDigest.getInstance("SHA-256")
        .digest(bytes)
        .joinToString("") { "%02x".format(it) }
}

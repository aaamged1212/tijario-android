package app.tijario.features.backup.drive

import app.tijario.features.backup.BackupArchiveCodec
import app.tijario.features.backup.BackupManifest
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
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

    @Test
    fun encryptedDocumentArchiveSurvivesFakeDriveUploadDownloadAndRestoreValidation() = runBlocking {
        val key = ByteArray(32) { (it + 1).toByte() }
        val archive = BackupArchiveCodec.create(
            manifest = BackupManifest(
                formatVersion = 1,
                roomDatabaseVersion = 18,
                applicationVersion = "test",
                minimumApplicationVersion = "test",
                accountId = "user-1",
                installationId = "device-1",
                backupSequence = 1,
                createdAtEpochMillis = 1L,
                encryptionVersion = 1,
                keyVersion = 1,
                recordCounts = mapOf("documents" to 1),
                documentCount = 1,
                documentCreationEventCount = 1,
                pdfIncludedCount = 0,
                pdfMissingCount = 0,
                pdfFailedGenerationCount = 0,
            ),
            logicalEntries = mapOf(
                "data/documents.json" to "[]".encodeToByteArray(),
                "data/document-items.json" to "[]".encodeToByteArray(),
                "data/document-creation-events.json" to "[]".encodeToByteArray(),
            ),
            encryptionKey = key,
        )
        val client = FakeDriveBackupClient(DriveConnectionState.Connected("user-1"))
        val folder = DriveFolderRepository(client).resolve().backupsId
        val source = temporaryArchive("placeholder").apply { writeBytes(archive) }
        val remote = client.uploadBackup(folder, source, DriveUploadMetadata("user-1", "backup-1", sha256(archive), 1L))
        val downloaded = File.createTempFile("drive-round-trip", ".tijario")

        client.downloadBackup(remote.id, downloaded)
        val decoded = BackupArchiveCodec.open(downloaded.readBytes(), key, "user-1")

        assertEquals("user-1", decoded.manifest.accountId)
        assertTrue(decoded.entries.containsKey("data/documents.json"))
        assertTrue(decoded.entries.containsKey("data/document-items.json"))
    }

    @Test
    fun uploadAndDownloadReportProgressFromZeroToOneHundredPercent() = runBlocking {
        val client = FakeDriveBackupClient(DriveConnectionState.Connected("user-1"))
        val folder = DriveFolderRepository(client).resolve().backupsId
        val source = temporaryArchive("progress archive")
        val uploadProgress = mutableListOf<Int>()
        val remote = client.uploadBackup(
            folder,
            source,
            DriveUploadMetadata("user-1", "backup-progress", sha256(source.readBytes()), 42L),
        ) { transferred, total -> uploadProgress += ((transferred * 100L) / total).toInt() }
        val downloadProgress = mutableListOf<Int>()
        val destination = File.createTempFile("drive-progress", ".tijario")
        client.downloadBackup(remote.id, destination) { transferred, total -> downloadProgress += ((transferred * 100L) / total).toInt() }
        assertTrue(uploadProgress.first() == 0 && uploadProgress.last() == 100)
        assertTrue(downloadProgress.first() == 0 && downloadProgress.last() == 100)
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

    @Test
    fun firstUploadResolvesPartialCreateResponseWithoutManualRetry() = runBlocking {
        val transport = RecordingDriveTransport(partialUploadResponse = true)
        val client = GoogleDriveRestClient(
            tokenProvider = { "oauth-token" },
            driveAccountIdProvider = { "google-account-123" },
            transport = transport,
        )
        val file = temporaryArchive("first upload")
        val checksum = sha256(file.readBytes())

        val remote = client.uploadBackup(
            folderId = "backups-folder",
            file = file,
            metadata = DriveUploadMetadata("user-1", "backup-1", checksum, 42L),
        )

        assertEquals(1, transport.uploadCalls)
        assertEquals(1, transport.listCalls)
        assertEquals(file.length(), remote.sizeBytes)
        assertEquals(checksum, remote.checksum)
        assertEquals("user-1", remote.accountId)
        assertEquals("backup-1", remote.backupId)
    }

    @Test
    fun partialCreateResponseRetriesAutomaticallyUntilDriveListsTheFile() = runBlocking {
        val transport = RecordingDriveTransport(
            partialUploadResponse = true,
            exposePersistedOnList = false,
        )
        val client = GoogleDriveRestClient(
            tokenProvider = { "oauth-token" },
            driveAccountIdProvider = { "google-account-123" },
            transport = transport,
        )
        val file = temporaryArchive("delayed metadata")

        val error = runCatching {
            client.uploadBackup(
                folderId = "backups-folder",
                file = file,
                metadata = DriveUploadMetadata("user-1", "backup-2", sha256(file.readBytes()), 43L),
            )
        }.exceptionOrNull()

        assertTrue(error is DriveBackupException.Retryable)
        assertEquals(1, transport.uploadCalls)
        assertEquals(1, transport.listCalls)
    }

    private class RecordingDriveTransport(
        private val partialUploadResponse: Boolean = false,
        private val exposePersistedOnList: Boolean = true,
    ) : DriveRestTransport {
        var lastUploadProperties: Map<String, String> = emptyMap()
        var uploadCalls = 0
        var listCalls = 0
        private var persisted: DriveRestFile? = null

        override suspend fun getCurrentUser(accessToken: String) = DriveCurrentUser("google-account-123", null)

        override suspend fun list(accessToken: String, query: String): List<DriveRestFile> {
            listCalls += 1
            return if (exposePersistedOnList) listOfNotNull(persisted) else emptyList()
        }

        override suspend fun createFolder(accessToken: String, name: String, parentId: String?) =
            DriveRestFile(id = "folder", name = name, mimeType = GoogleDriveRestClient.FOLDER_MIME_TYPE)

        override suspend fun uploadFile(
            accessToken: String,
            parentId: String,
            file: File,
            mimeType: String,
            appProperties: Map<String, String>,
            onProgress: suspend (Long, Long) -> Unit,
        ): DriveRestFile {
            uploadCalls += 1
            lastUploadProperties = appProperties
            persisted = DriveRestFile(
                id = "remote-1",
                name = file.name,
                mimeType = mimeType,
                sizeBytes = file.length(),
                appProperties = appProperties,
            )
            return if (partialUploadResponse) {
                requireNotNull(persisted).copy(sizeBytes = 0L, appProperties = emptyMap())
            } else {
                requireNotNull(persisted)
            }
        }

        override suspend fun downloadFile(accessToken: String, fileId: String, destination: File, onProgress: suspend (Long, Long) -> Unit) = Unit
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

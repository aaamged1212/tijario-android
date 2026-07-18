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

    private fun temporaryArchive(value: String): File = File.createTempFile("drive-backup", ".tijario").apply {
        writeText(value)
        deleteOnExit()
    }

    private fun sha256(bytes: ByteArray): String = MessageDigest.getInstance("SHA-256")
        .digest(bytes)
        .joinToString("") { "%02x".format(it) }
}

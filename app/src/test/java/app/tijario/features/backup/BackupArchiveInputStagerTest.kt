package app.tijario.features.backup

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.File

class BackupArchiveInputStagerTest {
    private val key = ByteArray(32) { (it + 1).toByte() }

    @Test
    fun inputIsCopiedWithBoundedSizeAndDeletedAfterUse() {
        val root = temporaryRoot()
        val staged = BackupArchiveInputStager.copyToPrivateFile(
            input = ByteArrayInputStream("encrypted-backup".encodeToByteArray()),
            filesRoot = root,
            userId = "account-1",
            maxBytes = 64,
        )

        assertEquals(16, staged.sizeBytes)
        assertTrue(staged.file.isFile)
        staged.delete()
        assertFalse(staged.file.exists())
        root.deleteRecursively()
    }

    @Test
    fun oversizedInputIsRejectedWithoutLeavingTemporaryArchive() {
        val root = temporaryRoot()

        assertThrows(BackupValidationException::class.java) {
            BackupArchiveInputStager.copyToPrivateFile(
                input = ByteArrayInputStream(ByteArray(65)),
                filesRoot = root,
                userId = "account-1",
                maxBytes = 64,
            )
        }

        assertTrue(root.walkTopDown().none { it.name.endsWith(".tmp") })
        root.deleteRecursively()
    }

    @Test
    fun fileArchiveStagesBinaryAssetsInsteadOfReturningThemInMemory() {
        val archive = BackupArchiveCodec.create(
            manifest = manifest(),
            logicalEntries = mapOf(
                "data/documents.json" to "[]".encodeToByteArray(),
                "data/document-items.json" to "[]".encodeToByteArray(),
                "data/document-creation-events.json" to "[]".encodeToByteArray(),
                "assets/document-pdfs/INV-00001.pdf" to ByteArray(1024) { 7 },
            ),
            encryptionKey = key,
        )
        val root = temporaryRoot()
        val archiveFile = File(root, "backup.tijario").apply { writeBytes(archive) }
        val decoded = BackupArchiveCodec.open(archiveFile, key, "account-1", File(root, "restore"))

        assertTrue(decoded.entries.containsKey("data/documents.json"))
        assertFalse(decoded.entries.containsKey("assets/document-pdfs/INV-00001.pdf"))
        assertTrue(decoded.stagedAssetFiles.getValue("assets/document-pdfs/INV-00001.pdf").isFile)

        decoded.discardStaging()
        root.deleteRecursively()
    }

    private fun temporaryRoot(): File =
        File(System.getProperty("java.io.tmpdir"), "tijario-backup-test-${System.nanoTime()}").apply { mkdirs() }

    private fun manifest() = BackupManifest(
        formatVersion = 1,
        roomDatabaseVersion = 17,
        applicationVersion = "1.0",
        minimumApplicationVersion = "1.0",
        accountId = "account-1",
        installationId = "installation-1",
        backupSequence = 1,
        createdAtEpochMillis = 1,
        encryptionVersion = 1,
        keyVersion = 1,
        recordCounts = emptyMap(),
        documentCount = 0,
        documentCreationEventCount = 0,
        pdfIncludedCount = 1,
        pdfMissingCount = 0,
        pdfFailedGenerationCount = 0,
    )
}

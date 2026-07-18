package app.tijario.features.backup

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class BackupArchiveCodecTest {
    private val key = ByteArray(32) { index -> (index + 1).toByte() }
    private val requiredEntries = mapOf(
        "data/documents.json" to "[]".encodeToByteArray(),
        "data/document-items.json" to "[]".encodeToByteArray(),
        "data/document-creation-events.json" to "[]".encodeToByteArray(),
        "assets/document-pdfs/INV-00001.pdf" to "%PDF-test".encodeToByteArray(),
    )

    @Test
    fun encryptedLogicalArchive_roundTripsOffline() {
        val archive = BackupArchiveCodec.create(manifest(), requiredEntries, key)

        assertEquals(1, BackupArchiveCodec.peekKeyVersion(archive))
        val decoded = BackupArchiveCodec.open(archive, key, "account-1")

        assertEquals("account-1", decoded.manifest.accountId)
        assertArrayEquals(requiredEntries.getValue("data/documents.json"), decoded.entries.getValue("data/documents.json"))
        assertArrayEquals(requiredEntries.getValue("assets/document-pdfs/INV-00001.pdf"), decoded.entries.getValue("assets/document-pdfs/INV-00001.pdf"))
    }

    @Test
    fun keyVersionCannotBeReadFromMalformedArchive() {
        assertThrows(BackupValidationException::class.java) {
            BackupArchiveCodec.peekKeyVersion("not-a-backup".encodeToByteArray())
        }
    }

    @Test
    fun restoreRejectsArchiveForDifferentAccount() {
        val archive = BackupArchiveCodec.create(manifest(), requiredEntries, key)

        val error = assertThrows(BackupValidationException::class.java) {
            BackupArchiveCodec.open(archive, key, "account-2")
        }

        assertEquals("Backup belongs to a different account", error.message)
    }

    @Test
    fun authenticatedEncryptionRejectsTampering() {
        val archive = BackupArchiveCodec.create(manifest(), requiredEntries, key).copyOf()
        archive[archive.lastIndex] = (archive.last().toInt() xor 0x01).toByte()

        assertThrows(BackupValidationException::class.java) {
            BackupArchiveCodec.open(archive, key, "account-1")
        }
    }

    @Test
    fun createRequiresStructuredDocumentsAndItems() {
        assertThrows(IllegalArgumentException::class.java) {
            BackupArchiveCodec.create(manifest(), mapOf("data/documents.json" to "[]".encodeToByteArray()), key)
        }
    }

    @Test
    fun createRejectsPathTraversal() {
        assertThrows(IllegalArgumentException::class.java) {
            BackupArchiveCodec.create(manifest(), requiredEntries + ("../secret" to byteArrayOf(1)), key)
        }
    }

    @Test
    fun createRejectsExcessiveEntryCount() {
        val excessive = buildMap {
            putAll(requiredEntries)
            repeat(BackupArchiveCodec.MAX_ENTRY_COUNT) { index ->
                put("assets/product-images/$index.jpg", byteArrayOf(1))
            }
        }

        assertThrows(IllegalArgumentException::class.java) {
            BackupArchiveCodec.create(manifest(), excessive, key)
        }
    }

    private fun manifest() = BackupManifest(
        formatVersion = 1,
        roomDatabaseVersion = 16,
        applicationVersion = "1.0",
        minimumApplicationVersion = "1.0",
        accountId = "account-1",
        installationId = "installation-1",
        backupSequence = 1,
        createdAtEpochMillis = 1,
        encryptionVersion = 1,
        keyVersion = 1,
        recordCounts = mapOf("documents" to 1),
        documentCount = 1,
        documentCreationEventCount = 1,
        pdfIncludedCount = 1,
        pdfMissingCount = 0,
        pdfFailedGenerationCount = 0,
    )
}

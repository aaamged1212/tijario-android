package app.tijario.features.backup

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import java.nio.file.Files

class BackupAssetRestorerTest {
    @Test
    fun stagedAssetsApplyAndRollbackWithoutLosingPreviousFiles() {
        val root = Files.createTempDirectory("tijario-restore").toFile()
        val existing = root.resolve("documents/pdfs/user-1/document-1.pdf").apply {
            requireNotNull(parentFile).mkdirs()
            writeBytes("old".encodeToByteArray())
        }
        val staged = BackupAssetRestorer(root).stage("user-1", entries("new"))

        val applied = staged.apply()
        assertArrayEquals("new".encodeToByteArray(), existing.readBytes())

        applied.rollback()
        assertArrayEquals("old".encodeToByteArray(), existing.readBytes())
        assertFalse(root.resolve("users/user-1/restore-staging").walkTopDown().any { it.isFile })
        root.deleteRecursively()
    }

    @Test
    fun assetForUnknownDocumentIsRejectedBeforeWrites() {
        val root = Files.createTempDirectory("tijario-restore").toFile()
        val entries = entries("pdf").toMutableMap().apply {
            remove("assets/document-pdfs/document-1.pdf")
            put("assets/document-pdfs/document-2.pdf", "pdf".encodeToByteArray())
        }

        assertThrows(BackupValidationException::class.java) {
            BackupAssetRestorer(root).stage("user-1", entries)
        }

        assertFalse(root.resolve("documents/pdfs/user-1/document-2.pdf").exists())
        root.deleteRecursively()
    }

    @Test
    fun successfulApplyCanBeCompleted() {
        val root = Files.createTempDirectory("tijario-restore").toFile()

        BackupAssetRestorer(root).stage("user-1", entries("pdf")).apply().complete()

        assertTrue(root.resolve("documents/pdfs/user-1/document-1.pdf").isFile)
        assertFalse(root.resolve("users/user-1/restore-staging").walkTopDown().any { it.isFile })
        root.deleteRecursively()
    }

    private fun entries(pdf: String): Map<String, ByteArray> {
        val documents = LogicalTableSnapshot(
            table = "documents_cache",
            columns = listOf("id", "user_id"),
            rows = listOf(listOf(LogicalBackupValue("text", "document-1"), LogicalBackupValue("text", "user-1"))),
        )
        val products = LogicalTableSnapshot(
            table = "products_cache",
            columns = listOf("id", "user_id"),
            rows = emptyList(),
        )
        return mapOf(
            "data/documents.json" to LogicalBackupSnapshotCodec.encode(documents),
            "data/products.json" to LogicalBackupSnapshotCodec.encode(products),
            "assets/document-pdfs/document-1.pdf" to pdf.encodeToByteArray(),
        )
    }
}

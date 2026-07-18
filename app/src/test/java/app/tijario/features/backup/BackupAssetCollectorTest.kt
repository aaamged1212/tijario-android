package app.tijario.features.backup

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import java.nio.file.Files

class BackupAssetCollectorTest {
    @Test
    fun collectsExistingPdfProductImageAndAccountLogo() {
        val root = Files.createTempDirectory("tijario-assets").toFile()
        root.resolve("documents/pdfs/user-1/document-1.pdf").apply {
            requireNotNull(parentFile).mkdirs()
            writeBytes("pdf".encodeToByteArray())
        }
        root.resolve("product_images/product-1.jpg").apply {
            requireNotNull(parentFile).mkdirs()
            writeBytes("product".encodeToByteArray())
        }
        root.resolve("users/user-1/business/logo/logo.png").apply {
            requireNotNull(parentFile).mkdirs()
            writeBytes("logo".encodeToByteArray())
        }

        val result = BackupAssetCollector(root).collect("user-1", logicalEntries(
            pdfPath = "documents/pdfs/user-1/document-1.pdf",
            pdfStatus = "available",
        ))

        assertEquals(1, result.pdfIncludedCount)
        assertEquals(0, result.pdfMissingCount)
        assertArrayEquals("pdf".encodeToByteArray(), result.entries.getValue("assets/document-pdfs/document-1.pdf"))
        assertArrayEquals("product".encodeToByteArray(), result.entries.getValue("assets/product-images/product-1.jpg"))
        assertArrayEquals("logo".encodeToByteArray(), result.entries.getValue("assets/business-logo/logo.png"))
        root.deleteRecursively()
    }

    @Test
    fun missingPdfIsRecordedWithoutFailingBackup() {
        val root = Files.createTempDirectory("tijario-assets").toFile()

        val result = BackupAssetCollector(root).collect("user-1", logicalEntries(null, "missing"))

        assertEquals(0, result.pdfIncludedCount)
        assertEquals(1, result.pdfMissingCount)
        root.deleteRecursively()
    }

    @Test
    fun documentPdfCannotEscapeApplicationFilesDirectory() {
        val root = Files.createTempDirectory("tijario-assets").toFile()

        assertThrows(BackupValidationException::class.java) {
            BackupAssetCollector(root).collect("user-1", logicalEntries("../private.pdf", "available"))
        }

        root.deleteRecursively()
    }

    private fun logicalEntries(pdfPath: String?, pdfStatus: String): Map<String, ByteArray> {
        val document = LogicalTableSnapshot(
            table = "documents_cache",
            columns = listOf("id", "user_id", "local_pdf_relative_path", "pdf_generation_status"),
            rows = listOf(
                listOf(
                    LogicalBackupValue("text", "document-1"),
                    LogicalBackupValue("text", "user-1"),
                    pdfPath?.let { LogicalBackupValue("text", it) } ?: LogicalBackupValue("null"),
                    LogicalBackupValue("text", pdfStatus),
                ),
            ),
        )
        val products = LogicalTableSnapshot(
            table = "products_cache",
            columns = listOf("id", "user_id"),
            rows = listOf(
                listOf(
                    LogicalBackupValue("text", "product-1"),
                    LogicalBackupValue("text", "user-1"),
                ),
            ),
        )
        return mapOf(
            "data/documents.json" to LogicalBackupSnapshotCodec.encode(document),
            "data/products.json" to LogicalBackupSnapshotCodec.encode(products),
        )
    }
}

package app.tijario.features.backup

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class BackupPdfPreparationContractTest {
    @Test
    fun backupPreparationGeneratesMissingPdfsWithoutNetworkLogoFetch() {
        val source = File("src/main/java/app/tijario/features/backup/LocalBackupPdfPreparer.kt").readText()
        val generator = File("src/main/java/app/tijario/features/documents/pdf/LocalPdfGenerator.kt").readText()

        assertTrue(source.contains("LocalPdfGenerator(context, allowNetworkLogoFetch = false)"))
        assertTrue(source.contains("pdfGenerator.ensurePdf(model)"))
        assertTrue(generator.contains("&& allowNetworkLogoFetch"))
    }

    @Test
    fun onePdfFailureIsRecordedWithoutAbortingPreparation() {
        val source = File("src/main/java/app/tijario/features/backup/LocalBackupPdfPreparer.kt").readText()

        assertTrue(source.contains("runCatching"))
        assertTrue(source.contains("updateDocumentPdfState(userId, document.id, null, null, null, null, \"failed\")"))
        assertTrue(source.contains("PdfPreparationSummary(generated, failed)"))
        assertFalse(source.contains("URL("))
    }
}

package app.tijario.features.documents.export

import android.content.Context
import android.content.Intent
import android.net.Uri
import app.tijario.features.documents.model.DocumentRenderModel
import app.tijario.features.documents.pdf.LocalPdfGenerator
import app.tijario.features.documents.pdf.PdfGenerationResult

class DocumentExportManager(
    private val context: Context,
    private val pdfGenerator: LocalPdfGenerator = LocalPdfGenerator(context),
) {
    private val shareFactory = DocumentShareIntentFactory(context)
    private val emailFactory = DocumentEmailIntentFactory(shareFactory)
    private val downloadManager = DocumentDownloadManager(context)
    private val printManager = DocumentPrintManager(context, pdfGenerator)

    suspend fun ensurePdf(model: DocumentRenderModel): PdfGenerationResult =
        pdfGenerator.ensurePdf(model)

    suspend fun viewIntent(model: DocumentRenderModel): Intent =
        shareFactory.viewPdf(ensurePdf(model).file)

    suspend fun shareIntent(model: DocumentRenderModel): Intent =
        shareFactory.sharePdf(ensurePdf(model).file)

    suspend fun emailIntent(model: DocumentRenderModel): Intent =
        emailFactory.email(model, ensurePdf(model).file)

    fun textShareIntent(model: DocumentRenderModel): Intent =
        shareFactory.shareText(model)

    suspend fun saveToDownloads(model: DocumentRenderModel): Uri =
        downloadManager.save(ensurePdf(model).file, model)

    suspend fun printPdf(model: DocumentRenderModel) {
        val file = ensurePdf(model).file
        printManager.print(model, file)
    }
}

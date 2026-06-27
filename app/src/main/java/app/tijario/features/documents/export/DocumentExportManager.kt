package app.tijario.features.documents.export

import android.content.Context
import android.content.Intent
import android.net.Uri
import app.tijario.features.documents.model.DocumentRenderModel
import app.tijario.features.documents.pdf.LocalPdfGenerator
import app.tijario.features.documents.pdf.PdfCacheManager
import app.tijario.features.documents.pdf.PdfGenerationResult
import java.io.File

class DocumentExportManager(
    private val context: Context,
    private val pdfGenerator: LocalPdfGenerator = LocalPdfGenerator(context),
) {
    private val shareFactory = DocumentShareIntentFactory(context)
    private val emailFactory = DocumentEmailIntentFactory(shareFactory)
    private val downloadManager = DocumentDownloadManager(context)
    private val printManager = DocumentPrintManager(context, pdfGenerator)
    private val cacheManager = PdfCacheManager(context)
    private val namedExportDir = File(context.cacheDir, "documents/named").apply { mkdirs() }

    suspend fun ensurePdf(model: DocumentRenderModel): PdfGenerationResult {
        val file = cacheManager.resolve(model)
        if (!cacheManager.isValid(file)) {
            val docId = model.documentId ?: ""
            val quotaResult = app.tijario.features.documents.policy.DocumentQuotaPolicy(context)
                .checkQuotaAndFinalize(docId)
            if (quotaResult.isFailure) {
                val exception = quotaResult.exceptionOrNull()
                if (exception?.message == "QUOTA_LIMIT_EXCEEDED") {
                    throw IllegalStateException("QUOTA_LIMIT_EXCEEDED")
                }
            }
        }
        return pdfGenerator.ensurePdf(model)
    }

    suspend fun viewIntent(model: DocumentRenderModel): Intent =
        shareFactory.viewPdf(namedPdf(model))

    suspend fun shareIntent(model: DocumentRenderModel): Intent =
        shareFactory.sharePdf(namedPdf(model))

    suspend fun emailIntent(model: DocumentRenderModel): Intent =
        emailFactory.email(model, namedPdf(model))

    fun textShareIntent(model: DocumentRenderModel): Intent =
        shareFactory.shareText(model)

    suspend fun saveToDownloads(model: DocumentRenderModel): Uri =
        downloadManager.save(ensurePdf(model).file, model)

    suspend fun printPdf(model: DocumentRenderModel) {
        val file = ensurePdf(model).file
        printManager.print(model, file)
    }

    private suspend fun namedPdf(model: DocumentRenderModel): File {
        val source = ensurePdf(model).file
        val target = namedExportDir.resolve(cacheManager.displayName(model))
        source.copyTo(target, overwrite = true)
        return target
    }
}

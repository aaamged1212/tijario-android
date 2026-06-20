package app.tijario.features.documents.export

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import app.tijario.config.AppLanguage
import app.tijario.data.model.DocumentType
import app.tijario.features.documents.model.DocumentRenderModel
import app.tijario.features.documents.template.DocumentFormatting
import java.io.File

class DocumentShareIntentFactory(
    private val context: Context,
) {
    fun contentUri(file: File): Uri =
        FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)

    fun viewPdf(file: File): Intent =
        Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(contentUri(file), "application/pdf")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

    fun sharePdf(file: File): Intent =
        Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, contentUri(file))
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

    fun shareText(model: DocumentRenderModel): Intent =
        Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, textSummary(model))
        }

    fun textSummary(model: DocumentRenderModel): String {
        val type = when (model.documentType) {
            DocumentType.Invoice -> if (model.language == AppLanguage.AR) "فاتورة" else "Invoice"
            DocumentType.Quote -> if (model.language == AppLanguage.AR) "عرض سعر" else "Quotation"
        }
        return listOf(
            "$type ${model.documentNumber}",
            model.customer.name,
            DocumentFormatting.money(model.totals.total, model.totals.currency, model.language),
        ).joinToString("\n")
    }
}

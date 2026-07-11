package app.tijario.features.documents.export

import android.content.Intent
import app.tijario.config.AppLanguage
import app.tijario.data.model.DocumentType
import app.tijario.features.documents.model.DocumentRenderModel
import java.io.File

class DocumentEmailIntentFactory(
    private val shareFactory: DocumentShareIntentFactory,
) {
    fun email(model: DocumentRenderModel, file: File): Intent {
        val subjectType = when (model.documentType) {
            DocumentType.Invoice -> if (model.language == AppLanguage.AR) "فاتورة" else "Invoice"
            DocumentType.Quote -> if (model.language == AppLanguage.AR) "عرض سعر" else "Quotation"
        }
        val body = if (model.language == AppLanguage.AR) {
            "مرفق مستند تجاريو بصيغة PDF."
        } else {
            "Attached is the Tijario PDF document."
        }
        return Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_SUBJECT, "$subjectType ${model.documentNumber}")
            putExtra(Intent.EXTRA_TEXT, body)
            putExtra(Intent.EXTRA_STREAM, shareFactory.contentUri(file))
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }
}

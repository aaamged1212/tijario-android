package app.tijario.features.documents.export

import android.content.Context
import android.print.PrintManager
import app.tijario.data.model.DocumentType
import app.tijario.features.documents.model.DocumentRenderModel
import app.tijario.features.documents.pdf.LocalPdfGenerator
import java.io.File

class DocumentPrintManager(
    private val context: Context,
    private val pdfGenerator: LocalPdfGenerator,
) {
    fun print(model: DocumentRenderModel, pdfFile: File) {
        val printManager = context.getSystemService(Context.PRINT_SERVICE) as PrintManager
        val name = if (model.documentType == DocumentType.Invoice) "Tijario Invoice" else "Tijario Quote"
        printManager.print(name, pdfGenerator.printAdapter(pdfFile), null)
    }
}

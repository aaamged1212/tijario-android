package app.tijario.features.documents.pdf

import app.tijario.features.documents.model.DocumentRenderModel

object PdfCacheKeyFactory {
    private const val PDF_RENDER_ENGINE_VERSION = "pdfv3"

    fun key(model: DocumentRenderModel): String {
        val identity = model.documentId ?: "draft"
        val revision = model.updatedAt ?: model.issueDate
        return listOf(
            PDF_RENDER_ENGINE_VERSION,
            PdfFileNameSanitizer.sanitize(identity),
            PdfFileNameSanitizer.sanitize(revision),
            PdfFileNameSanitizer.sanitize(model.templateId),
            "tv${model.templateVersion}",
            model.language.name.lowercase(),
            "fv${model.formattingVersion}",
        ).joinToString("-")
    }
}

package app.tijario.features.documents.pdf

import android.content.Context
import app.tijario.features.documents.model.DocumentRenderModel
import java.io.File

class PdfCacheManager(
    context: Context,
) {
    private val cacheDir = File(context.cacheDir, "documents").apply { mkdirs() }

    fun resolve(model: DocumentRenderModel): File =
        File(cacheDir, "${PdfCacheKeyFactory.key(model)}.pdf")

    fun displayName(model: DocumentRenderModel): String {
        return "${PdfFileNameSanitizer.sanitize(model.documentNumber)}.pdf"
    }

    fun isValid(file: File): Boolean =
        file.exists() && file.length() > 4 && file.inputStream().use { input ->
            val signature = ByteArray(4)
            input.read(signature) == 4 && signature.decodeToString() == "%PDF"
        }

    fun cleanup(maxFiles: Int = 24) {
        val files = cacheDir.listFiles { file -> file.extension.equals("pdf", ignoreCase = true) }
            ?.sortedByDescending { it.lastModified() }
            .orEmpty()
        files.drop(maxFiles).forEach { file ->
            runCatching { file.delete() }
        }
    }
}

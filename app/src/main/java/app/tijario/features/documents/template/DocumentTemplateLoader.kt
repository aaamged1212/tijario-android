package app.tijario.features.documents.template

import android.content.Context
import java.io.File

interface DocumentTemplateLoader {
    fun loadText(path: String): String
}

class AndroidAssetDocumentTemplateLoader(
    private val context: Context,
) : DocumentTemplateLoader {
    override fun loadText(path: String): String =
        context.assets.open(path).bufferedReader(Charsets.UTF_8).use { it.readText() }
}

class FileSystemDocumentTemplateLoader(
    private val root: File,
) : DocumentTemplateLoader {
    override fun loadText(path: String): String =
        File(root, path.replace('/', File.separatorChar)).readText(Charsets.UTF_8)
}

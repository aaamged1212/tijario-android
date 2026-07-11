package app.tijario.features.documents.pdf

import java.io.File

data class PdfGenerationResult(
    val file: File,
    val reusedCache: Boolean,
)

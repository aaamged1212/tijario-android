package app.tijario.features.documents.preview

data class DocumentPreviewState(
    val templateId: String,
    val isRendering: Boolean = false,
    val errorMessage: String? = null,
)

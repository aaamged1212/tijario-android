package app.tijario.features.documents.preview

import androidx.lifecycle.ViewModel
import app.tijario.features.documents.template.DocumentTemplateRegistry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class DocumentPreviewViewModel : ViewModel() {
    private val _state = MutableStateFlow(DocumentPreviewState(DocumentTemplateRegistry.defaultTemplateId))
    val state: StateFlow<DocumentPreviewState> = _state

    fun selectTemplate(templateId: String) {
        _state.value = _state.value.copy(templateId = DocumentTemplateRegistry.normalizeId(templateId))
    }
}

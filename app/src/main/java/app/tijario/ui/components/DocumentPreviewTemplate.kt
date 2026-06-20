package app.tijario.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import app.tijario.config.AppLanguage
import app.tijario.data.model.BusinessSettings
import app.tijario.data.model.DocumentType
import app.tijario.features.documents.mapper.TijarioDocumentMapper
import app.tijario.features.documents.preview.DocumentPreviewWebView
import app.tijario.features.documents.template.DocumentTemplateRegistry
import app.tijario.ui.state.DocumentFormState

@Composable
fun ModernDocumentPreview(
    documentType: DocumentType,
    form: DocumentFormState,
    businessSettings: BusinessSettings?,
    customerCity: String?,
    templateId: String = DocumentTemplateRegistry.defaultTemplateId,
    modifier: Modifier = Modifier,
) {
    val model = remember(documentType, form, businessSettings, customerCity, templateId) {
        TijarioDocumentMapper.fromDraft(
            documentType = documentType,
            form = form,
            businessSettings = businessSettings,
            customerCity = customerCity,
            language = AppLanguage.AR,
            templateId = templateId,
        )
    }
    DocumentPreviewWebView(model = model, modifier = modifier)
}

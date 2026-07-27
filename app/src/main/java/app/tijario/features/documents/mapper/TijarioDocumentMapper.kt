package app.tijario.features.documents.mapper

import app.tijario.config.AppLanguage
import app.tijario.data.model.BusinessSettings
import app.tijario.data.model.CompleteDocument
import app.tijario.data.model.DocumentType
import app.tijario.features.documents.model.DocumentRenderModel
import app.tijario.features.documents.template.DocumentTemplateRegistry
import app.tijario.ui.state.DocumentFormState

object TijarioDocumentMapper {
    fun fromDraft(
        documentType: DocumentType,
        form: DocumentFormState,
        businessSettings: BusinessSettings?,
        customerCity: String?,
        language: AppLanguage = AppLanguage.AR,
        templateId: String = DocumentTemplateRegistry.defaultTemplateId,
        showTijarioBranding: Boolean = true,
    ): DocumentRenderModel =
        DraftDocumentRenderMapper.map(documentType, form, businessSettings, customerCity, language, templateId, showTijarioBranding)

    fun fromSaved(
        document: CompleteDocument,
        businessSettings: BusinessSettings?,
        language: AppLanguage = AppLanguage.AR,
        templateId: String? = null,
        metadata: app.tijario.data.local.LocalDocumentMetadataEntity? = null,
        showTijarioBranding: Boolean = true,
    ): DocumentRenderModel {
        val resolvedTemplateId = DocumentTemplateRegistry.normalizeId(
            templateId?.takeIf { it.isNotBlank() }
                ?: document.templateId?.takeIf { it.isNotBlank() }
                ?: DocumentTemplateRegistry.defaultTemplateId,
        )
        return SavedDocumentRenderMapper.map(
            document,
            businessSettings,
            language,
            resolvedTemplateId,
            metadata,
            showTijarioBranding,
        )
    }
}

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
    ): DocumentRenderModel =
        DraftDocumentRenderMapper.map(documentType, form, businessSettings, customerCity, language, templateId)

    fun fromSaved(
        document: CompleteDocument,
        businessSettings: BusinessSettings?,
        language: AppLanguage = AppLanguage.AR,
        templateId: String = DocumentTemplateRegistry.defaultTemplateId,
        metadata: app.tijario.data.local.LocalDocumentMetadataEntity? = null,
    ): DocumentRenderModel =
        SavedDocumentRenderMapper.map(document, businessSettings, language, templateId, metadata)
}

package app.tijario.features.documents.ui

import android.content.Context
import app.tijario.features.documents.template.DocumentTemplateRegistry

class DocumentTemplatePreferences(
    context: Context,
) {
    private val prefs = context.getSharedPreferences("tijario_document_templates", Context.MODE_PRIVATE)

    fun getDefaultTemplateId(): String =
        DocumentTemplateRegistry.normalizeId(prefs.getString(KEY_DEFAULT_TEMPLATE, DocumentTemplateRegistry.defaultTemplateId))

    fun setDefaultTemplateId(templateId: String) {
        prefs.edit().putString(KEY_DEFAULT_TEMPLATE, DocumentTemplateRegistry.normalizeId(templateId)).apply()
    }

    private companion object {
        const val KEY_DEFAULT_TEMPLATE = "default_template_id"
    }
}

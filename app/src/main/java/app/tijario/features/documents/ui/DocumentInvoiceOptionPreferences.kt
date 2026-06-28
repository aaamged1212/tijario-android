package app.tijario.features.documents.ui

import android.content.Context

private const val PREFS_NAME = "tijario_document_invoice_options"
private const val KEY_PAYMENT_METHOD = "payment_method"
private const val KEY_TERMS_TITLE = "terms_title"
private const val KEY_TERMS_CONTENT = "terms_content"
private const val KEY_SIGNATURE_NAME = "signature_name"
private const val KEY_SIGNATURE_DATA = "signature_data"

data class DocumentInvoiceOptionDefaults(
    val paymentMethod: String = "",
    val termsTitle: String = "",
    val termsContent: String = "",
    val signatureName: String = "",
    val signatureData: String = "",
)

class DocumentInvoiceOptionPreferences(context: Context) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getDefaults(): DocumentInvoiceOptionDefaults = DocumentInvoiceOptionDefaults(
        paymentMethod = prefs.getString(KEY_PAYMENT_METHOD, "").orEmpty(),
        termsTitle = prefs.getString(KEY_TERMS_TITLE, "").orEmpty(),
        termsContent = prefs.getString(KEY_TERMS_CONTENT, "").orEmpty(),
        signatureName = prefs.getString(KEY_SIGNATURE_NAME, "").orEmpty(),
        signatureData = prefs.getString(KEY_SIGNATURE_DATA, "").orEmpty(),
    )

    fun setPaymentMethod(name: String) {
        prefs.edit().putString(KEY_PAYMENT_METHOD, name).apply()
    }

    fun setTerms(title: String, content: String) {
        prefs.edit()
            .putString(KEY_TERMS_TITLE, title)
            .putString(KEY_TERMS_CONTENT, content)
            .apply()
    }

    fun setSignature(name: String, data: String) {
        prefs.edit()
            .putString(KEY_SIGNATURE_NAME, name)
            .putString(KEY_SIGNATURE_DATA, data)
            .apply()
    }
}

package app.tijario.features.documents.ui

import android.content.Context

private const val PREFS_NAME = "tijario_document_invoice_options"
private const val KEY_PAYMENT_METHOD = "payment_method"
private const val KEY_TERMS_TITLE = "terms_title"
private const val KEY_TERMS_CONTENT = "terms_content"
private const val KEY_SIGNATURE_NAME = "signature_name"
private const val KEY_SIGNATURE_DATA = "signature_data"
private const val KEY_TAX_NAME = "tax_name"
private const val KEY_TAX_RATE = "tax_rate"
private const val KEY_DOCUMENT_TITLE_PREFIX = "document_title_"
private const val KEY_DOCUMENT_NUMBER_PREFIX = "document_number_"
private const val KEY_DISCOUNT_PRESETS = "discount_presets"
private const val KEY_EXTRA_FEES_PRESETS = "extra_fees_presets"

data class AmountPreset(
    val amount: String,
    val reason: String,
    val type: String = "fixed",
)

data class DocumentInvoiceOptionDefaults(
    val paymentMethod: String = "",
    val termsTitle: String = "",
    val termsContent: String = "",
    val signatureName: String = "",
    val signatureData: String = "",
    val taxName: String = "",
    val taxRate: String = "",
)

class DocumentInvoiceOptionPreferences(context: Context) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getDefaults(): DocumentInvoiceOptionDefaults = DocumentInvoiceOptionDefaults(
        paymentMethod = prefs.getString(KEY_PAYMENT_METHOD, "").orEmpty(),
        termsTitle = prefs.getString(KEY_TERMS_TITLE, "").orEmpty(),
        termsContent = prefs.getString(KEY_TERMS_CONTENT, "").orEmpty(),
        signatureName = prefs.getString(KEY_SIGNATURE_NAME, "").orEmpty(),
        signatureData = prefs.getString(KEY_SIGNATURE_DATA, "").orEmpty(),
        taxName = prefs.getString(KEY_TAX_NAME, "").orEmpty(),
        taxRate = prefs.getString(KEY_TAX_RATE, "").orEmpty(),
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

    fun setTax(name: String, rate: String) {
        prefs.edit()
            .putString(KEY_TAX_NAME, name)
            .putString(KEY_TAX_RATE, rate)
            .apply()
    }

    fun getDocumentTitleOverride(documentId: String): String? =
        prefs.getString(KEY_DOCUMENT_TITLE_PREFIX + documentId, null)?.takeIf { it.isNotBlank() }

    fun setDocumentTitleOverride(documentId: String, title: String?) {
        prefs.edit().apply {
            val key = KEY_DOCUMENT_TITLE_PREFIX + documentId
            if (title.isNullOrBlank()) remove(key) else putString(key, title)
        }.apply()
    }

    fun getDocumentNumberOverride(documentId: String): String? =
        prefs.getString(KEY_DOCUMENT_NUMBER_PREFIX + documentId, null)?.takeIf { it.isNotBlank() }

    fun setDocumentNumberOverride(documentId: String, number: String?) {
        prefs.edit().apply {
            val key = KEY_DOCUMENT_NUMBER_PREFIX + documentId
            if (number.isNullOrBlank()) remove(key) else putString(key, number)
        }.apply()
    }

    fun getDiscountPresets(): List<AmountPreset> = readPresets(KEY_DISCOUNT_PRESETS)

    fun getExtraFeesPresets(): List<AmountPreset> = readPresets(KEY_EXTRA_FEES_PRESETS)

    fun addDiscountPreset(amount: String, reason: String, type: String) {
        addPreset(KEY_DISCOUNT_PRESETS, AmountPreset(amount, reason, type))
    }

    fun addExtraFeesPreset(amount: String, reason: String) {
        addPreset(KEY_EXTRA_FEES_PRESETS, AmountPreset(amount, reason))
    }

    private fun addPreset(key: String, preset: AmountPreset) {
        if (preset.amount.isBlank() && preset.reason.isBlank()) return
        val next = (listOf(preset) + readPresets(key))
            .distinctBy { "${it.amount}|${it.reason}|${it.type}" }
            .take(8)
        prefs.edit().putString(key, next.joinToString("\n") { "${it.amount}|${it.reason}|${it.type}" }).apply()
    }

    private fun readPresets(key: String): List<AmountPreset> =
        prefs.getString(key, "").orEmpty()
            .lineSequence()
            .mapNotNull { row ->
                val parts = row.split("|", limit = 3)
                if (parts.size < 2) null else AmountPreset(
                    amount = parts[0],
                    reason = parts[1],
                    type = parts.getOrElse(2) { "fixed" },
                )
            }
            .toList()
}

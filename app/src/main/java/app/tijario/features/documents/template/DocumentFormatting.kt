package app.tijario.features.documents.template

import app.tijario.config.AppLanguage
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.NumberFormat
import java.util.Locale

object DocumentFormatting {
    fun locale(language: AppLanguage): Locale =
        if (language == AppLanguage.AR) Locale("ar", "SA") else Locale.US

    fun money(value: BigDecimal, currency: String, language: AppLanguage): String {
        val formatted = NumberFormat.getNumberInstance(locale(language)).apply {
            minimumFractionDigits = 2
            maximumFractionDigits = 2
        }.format(value.setScale(2, RoundingMode.HALF_UP))
        return "$formatted $currency"
    }

    fun quantity(value: Int, language: AppLanguage): String =
        NumberFormat.getIntegerInstance(locale(language)).format(value)

    fun status(value: String?, language: AppLanguage): String? =
        when (value?.lowercase(Locale.US)) {
            null, "" -> null
            "draft" -> if (language == AppLanguage.AR) "مسودة" else "Draft"
            "sent" -> if (language == AppLanguage.AR) "مرسل" else "Sent"
            "accepted" -> if (language == AppLanguage.AR) "مقبول" else "Accepted"
            "rejected" -> if (language == AppLanguage.AR) "مرفوض" else "Rejected"
            "cancelled", "canceled" -> if (language == AppLanguage.AR) "ملغي" else "Cancelled"
            "paid" -> if (language == AppLanguage.AR) "مدفوع" else "Paid"
            "unpaid" -> if (language == AppLanguage.AR) "غير مدفوع" else "Unpaid"
            "overdue" -> if (language == AppLanguage.AR) "متأخر" else "Overdue"
            else -> value.replace('_', ' ')
        }

    fun initials(value: String): String =
        value.trim()
            .split(Regex("\\s+"))
            .filter { it.isNotBlank() }
            .take(2)
            .joinToString("") { it.take(1) }
            .ifBlank { "T" }
            .uppercase(locale = Locale.US)
}

package app.tijario.domain

import app.tijario.config.AppLanguage

object PaymentStatusMapper {
    fun getStatusText(status: String?, language: AppLanguage = AppLanguage.AR): String {
        val normalized = status?.lowercase() ?: "unpaid"
        return when (normalized) {
            "paid" -> if (language == AppLanguage.AR) "مدفوعة" else "Paid"
            "unpaid" -> if (language == AppLanguage.AR) "غير مدفوعة" else "Unpaid"
            "partial", "partially_paid" -> if (language == AppLanguage.AR) "دفع جزئي" else "Partially paid"
            else -> if (language == AppLanguage.AR) "حالة غير معروفة" else "Unknown"
        }
    }

    fun getStatusColors(status: String?): Pair<Long, Long> {
        val normalized = status?.lowercase() ?: "unpaid"
        return when (normalized) {
            "paid" -> Pair(0xFFDCFCE7L, 0xFF15803DL)
            "unpaid" -> Pair(0xFFFEE2E2L, 0xFF991B1BL)
            "partial", "partially_paid" -> Pair(0xFFFEF3C7L, 0xFFB45309L)
            else -> Pair(0xFFF1F5F9L, 0xFF475569L)
        }
    }
}

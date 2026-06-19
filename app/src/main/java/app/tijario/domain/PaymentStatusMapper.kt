package app.tijario.domain

object PaymentStatusMapper {
    fun getStatusText(status: String?): String {
        if (status == null) return "غير مدفوعة" // fallback or default unpaid
        return when (status.lowercase()) {
            "paid" -> "مدفوعة"
            "unpaid" -> "غير مدفوعة"
            else -> "حالة غير معروفة"
        }
    }

    fun getStatusColors(status: String?): Pair<Long, Long> {
        val normalized = status?.lowercase() ?: "unpaid"
        return when (normalized) {
            "paid" -> Pair(0xFFDCFCE7L, 0xFF15803DL)
            "unpaid" -> Pair(0xFFFEE2E2L, 0xFF991B1BL)
            else -> Pair(0xFFF1F5F9L, 0xFF475569L)
        }
    }
}

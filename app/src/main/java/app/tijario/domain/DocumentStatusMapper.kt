package app.tijario.domain

object DocumentStatusMapper {
    fun getStatusText(status: String): String {
        return when (status.lowercase()) {
            "draft" -> "مسودة"
            "sent" -> "مرسلة"
            "accepted" -> "مقبولة"
            "cancelled" -> "ملغاة"
            else -> "حالة غير معروفة"
        }
    }

    // Return hex Long values directly to remain pure JVM testable
    fun getStatusColors(status: String): Pair<Long, Long> {
        return when (status.lowercase()) {
            "draft" -> Pair(0xFFFEF3C7L, 0xFFB45309L)
            "sent" -> Pair(0xFFDBEAFEL, 0xFF1E40AFL)
            "accepted" -> Pair(0xFFD1FAE5L, 0xFF065F46L)
            "cancelled" -> Pair(0xFFFEE2E2L, 0xFF991B1BL)
            else -> Pair(0xFFF1F5F9L, 0xFF475569L)
        }
    }
}

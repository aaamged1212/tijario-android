package app.tijario.domain

object Validation {
    fun required(value: String, fieldName: String): String? =
        if (value.trim().isEmpty()) "$fieldName مطلوب" else null

    fun email(value: String): String? =
        when {
            value.trim().isEmpty() -> "البريد الإلكتروني مطلوب"
            !value.contains("@") || !value.contains(".") -> "أدخل بريد إلكتروني صحيح"
            else -> null
        }

    fun password(value: String): String? =
        when {
            value.isEmpty() -> "كلمة المرور مطلوبة"
            value.length < 6 -> "كلمة المرور يجب ألا تقل عن 6 أحرف"
            else -> null
        }

    fun whatsapp(value: String): String? =
        when {
            value.trim().isEmpty() -> "رقم واتساب مطلوب"
            value.trim().length < 7 -> "أدخل رقم واتساب صحيح"
            else -> null
        }

    fun positiveInt(value: String, fieldName: String): String? =
        value.toIntOrNull()?.takeIf { it > 0 }?.let { null } ?: "$fieldName يجب أن يكون رقما أكبر من صفر"

    fun nonNegativeMoney(value: String, fieldName: String): String? =
        value.toDoubleOrNull()?.takeIf { it >= 0.0 }?.let { null } ?: "$fieldName يجب أن يكون رقما غير سالب"
}

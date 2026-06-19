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
        if (value.trim().isEmpty()) null
        else if (parsePositiveInt(value) != null) null
        else "$fieldName يجب أن يكون رقمًا أكبر من صفر"

    fun nonNegativeMoney(value: String, fieldName: String): String? =
        if (value.trim().isEmpty()) null
        else if (parseNonNegativeMoney(value) != null) null
        else "$fieldName يجب أن يكون رقمًا غير سالب"

    fun parsePositiveInt(value: String): Int? {
        val number = parseNumber(value) ?: return null
        return number.toInt().takeIf { number > 0.0 && number % 1.0 == 0.0 }
    }

    fun parseNonNegativeMoney(value: String): Double? =
        parseNumber(value)?.takeIf { it >= 0.0 }

    fun normalizedMoneyString(value: String): String =
        parseNonNegativeMoney(value)?.let {
            if (it % 1.0 == 0.0) it.toLong().toString() else it.toString()
        } ?: value.trim()

    private fun parseNumber(value: String): Double? {
        val normalized = normalizeNumber(value)
        if (normalized.isBlank()) return null
        return normalized.toDoubleOrNull()
    }

    private fun normalizeNumber(value: String): String {
        val raw = buildString {
            value.trim().forEach { char ->
                append(
                    when (char) {
                        in '0'..'9' -> char
                        in '٠'..'٩' -> '0' + (char.code - '٠'.code)
                        in '۰'..'۹' -> '0' + (char.code - '۰'.code)
                        '٫' -> '.'
                        '٬' -> ','
                        else -> char
                    }
                )
            }
        }
            .replace("\\s".toRegex(), "")
            .replace("[^0-9,.-]".toRegex(), "")

        if (raw.count { it == ',' } == 1 && !raw.contains('.')) {
            val digitsAfterComma = raw.substringAfter(',').count { it.isDigit() }
            return if (digitsAfterComma in 1..2) raw.replace(',', '.') else raw.replace(",", "")
        }

        return raw.replace(",", "")
    }
}

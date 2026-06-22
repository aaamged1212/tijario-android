package app.tijario.domain

object Validation {
    fun required(value: String, fieldNameKey: String, lang: app.tijario.config.AppLanguage): String? {
        if (value.trim().isEmpty()) {
            val fieldName = app.tijario.config.Localization.getString(fieldNameKey, lang)
            val template = app.tijario.config.Localization.getString("validation_required", lang)
            return String.format(template, fieldName)
        }
        return null
    }

    fun email(value: String, lang: app.tijario.config.AppLanguage): String? =
        when {
            value.trim().isEmpty() -> app.tijario.config.Localization.getString("validation_email_required", lang)
            !value.contains("@") || !value.contains(".") -> app.tijario.config.Localization.getString("validation_email_invalid", lang)
            else -> null
        }

    fun password(value: String, lang: app.tijario.config.AppLanguage): String? =
        when {
            value.isEmpty() -> app.tijario.config.Localization.getString("validation_password_required", lang)
            value.length < 6 -> app.tijario.config.Localization.getString("validation_password_min_length", lang)
            else -> null
        }

    fun whatsapp(value: String, lang: app.tijario.config.AppLanguage): String? =
        when {
            value.trim().isEmpty() -> app.tijario.config.Localization.getString("validation_whatsapp_required", lang)
            value.trim().length < 7 -> app.tijario.config.Localization.getString("validation_whatsapp_invalid", lang)
            else -> null
        }

    fun positiveInt(value: String, fieldNameKey: String, lang: app.tijario.config.AppLanguage): String? {
        if (value.trim().isEmpty()) return null
        if (parsePositiveInt(value) != null) return null
        val fieldName = app.tijario.config.Localization.getString(fieldNameKey, lang)
        val template = app.tijario.config.Localization.getString("validation_positive_int", lang)
        return String.format(template, fieldName)
    }

    fun nonNegativeInt(value: String, fieldNameKey: String, lang: app.tijario.config.AppLanguage): String? {
        if (value.trim().isEmpty()) return null
        if (parseNonNegativeInt(value) != null) return null
        val fieldName = app.tijario.config.Localization.getString(fieldNameKey, lang)
        val template = app.tijario.config.Localization.getString("validation_non_negative_int", lang)
        return String.format(template, fieldName)
    }

    fun nonNegativeMoney(value: String, fieldNameKey: String, lang: app.tijario.config.AppLanguage): String? {
        if (value.trim().isEmpty()) return null
        if (parseNonNegativeMoney(value) != null) return null
        val fieldName = app.tijario.config.Localization.getString(fieldNameKey, lang)
        val template = app.tijario.config.Localization.getString("validation_non_negative_money", lang)
        return String.format(template, fieldName)
    }

    fun parsePositiveInt(value: String): Int? {
        val number = parseNumber(value) ?: return null
        return number.toInt().takeIf { number > 0.0 && number % 1.0 == 0.0 }
    }

    fun parseNonNegativeInt(value: String): Int? {
        val number = parseNumber(value) ?: return null
        return number.toInt().takeIf { number >= 0.0 && number % 1.0 == 0.0 }
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

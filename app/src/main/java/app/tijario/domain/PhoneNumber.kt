package app.tijario.domain

import app.tijario.config.AppLanguage

data class DialCodeOption(
    val countryCode: String,
    val dialCode: String,
    val nameAr: String,
    val nameEn: String,
    val flag: String = "",
) {
    fun label(language: AppLanguage): String =
        listOf(flag, if (language == AppLanguage.AR) nameAr else nameEn, dialCode)
            .filter { it.isNotBlank() }
            .joinToString(" ")
}

val MvpDialCodeOptions: List<DialCodeOption>
    get() = CountryCatalog.dialCodeOptions

fun normalizePhoneWithDialCode(dialCode: String, localNumber: String): String {
    val dialDigits = normalizePhoneDigits(dialCode).filter(Char::isDigit)
    val input = normalizePhoneDigits(localNumber).trim()
    val inputDigits = input.filter(Char::isDigit)
    if (dialDigits.isBlank() || inputDigits.isBlank()) return ""

    if (input.startsWith('+') && MvpDialCodeOptions.any {
            inputDigits.startsWith(it.dialCode.filter(Char::isDigit))
        }
    ) return "+$inputDigits"
    if (inputDigits.startsWith(dialDigits)) return "+$inputDigits"

    return "+$dialDigits${inputDigits.trimStart('0')}"
}

data class PhoneNumberParts(
    val dialCode: String,
    val localNumber: String,
)

fun splitPhoneNumber(
    value: String,
    options: List<DialCodeOption> = MvpDialCodeOptions,
): PhoneNumberParts {
    val normalized = normalizePhoneDigits(value)
    val digits = normalized.filter(Char::isDigit)
    val option = options
        .sortedByDescending { it.dialCode.length }
        .firstOrNull { digits.startsWith(it.dialCode.filter(Char::isDigit)) }
        ?: options.first()
    val dialDigits = option.dialCode.filter(Char::isDigit)
    return PhoneNumberParts(
        dialCode = option.dialCode,
        localNumber = digits.removePrefix(dialDigits),
    )
}

fun isValidE164Phone(value: String): Boolean =
    Regex("^\\+[1-9]\\d{6,14}$").matches(normalizePhoneDigits(value))

private fun normalizePhoneDigits(value: String): String = buildString(value.length) {
    value.forEach { char ->
        append(
            when (char) {
                in '٠'..'٩' -> '0' + (char.code - '٠'.code)
                in '۰'..'۹' -> '0' + (char.code - '۰'.code)
                else -> char
            }
        )
    }
}

package app.tijario.domain

import app.tijario.config.AppLanguage

data class DialCodeOption(
    val countryCode: String,
    val dialCode: String,
    val nameAr: String,
    val nameEn: String,
) {
    fun label(language: AppLanguage): String =
        "${if (language == AppLanguage.AR) nameAr else nameEn} $dialCode"
}

val MvpDialCodeOptions = listOf(
    DialCodeOption("YE", "+967", "اليمن", "Yemen"),
    DialCodeOption("SA", "+966", "السعودية", "Saudi Arabia"),
    DialCodeOption("AE", "+971", "الإمارات", "UAE"),
    DialCodeOption("OM", "+968", "عمان", "Oman"),
    DialCodeOption("QA", "+974", "قطر", "Qatar"),
    DialCodeOption("KW", "+965", "الكويت", "Kuwait"),
    DialCodeOption("BH", "+973", "البحرين", "Bahrain"),
    DialCodeOption("EG", "+20", "مصر", "Egypt"),
    DialCodeOption("JO", "+962", "الأردن", "Jordan"),
)

fun normalizePhoneWithDialCode(dialCode: String, localNumber: String): String {
    val normalizedDialCode = "+${dialCode.filter(Char::isDigit)}"
    val normalizedLocalNumber = localNumber
        .replace(Regex("[\\s-]"), "")
        .trimStart('+')
        .filter(Char::isDigit)

    if (normalizedDialCode == "+" || normalizedLocalNumber.isBlank()) {
        return ""
    }

    return normalizedDialCode + normalizedLocalNumber
}

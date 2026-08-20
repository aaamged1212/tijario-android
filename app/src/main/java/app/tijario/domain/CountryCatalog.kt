package app.tijario.domain

import app.tijario.config.AppLanguage
import java.util.Currency
import java.util.Locale

private fun localeForCountry(countryCode: String): Locale =
    Locale.Builder().setRegion(countryCode).build()

data class CountryOption(
    val countryCode: String,
    val dialCode: String?,
    val storageName: String,
) {
    val flag: String
        get() = countryCode
            .uppercase(Locale.ROOT)
            .takeIf { it.length == 2 && it.all(Char::isLetter) }
            ?.map { char -> String(Character.toChars(0x1F1E6 + (char - 'A'))) }
            ?.joinToString("")
            .orEmpty()

    fun name(language: AppLanguage): String {
        if (countryCode.uppercase(Locale.ROOT) == "US") {
            return if (language == AppLanguage.AR) "الولايات المتحدة الأمريكية" else "United States"
        }
        val locale = if (language == AppLanguage.AR) Locale.forLanguageTag("ar") else Locale.ENGLISH
        return localeForCountry(countryCode).getDisplayCountry(locale).takeIf { it.isNotBlank() } ?: storageName
    }

    fun label(language: AppLanguage): String = listOf(flag, name(language)).filter { it.isNotBlank() }.joinToString(" ")
}

data class CurrencyOption(
    val code: String,
    val countryCode: String,
)

/**
 * A local, deterministic country and currency catalog. It keeps business settings
 * independent of the device locale while presenting translated names and flags.
 */
object CountryCatalog {
    private val callingCodes: Map<String, String> =
        """
        AD:+376,AE:+971,AF:+93,AG:+1,AI:+1,AL:+355,AM:+374,AO:+244,AR:+54,AS:+1,
        AT:+43,AU:+61,AW:+297,AX:+358,AZ:+994,BA:+387,BB:+1,BD:+880,BE:+32,BF:+226,
        BG:+359,BH:+973,BI:+257,BJ:+229,BL:+590,BM:+1,BN:+673,BO:+591,BQ:+599,
        BR:+55,BS:+1,BT:+975,BW:+267,BY:+375,BZ:+501,CA:+1,CC:+61,CD:+243,CF:+236,
        CG:+242,CH:+41,CI:+225,CK:+682,CL:+56,CM:+237,CN:+86,CO:+57,CR:+506,CU:+53,
        CV:+238,CW:+599,CX:+61,CY:+357,CZ:+420,DE:+49,DJ:+253,DK:+45,DM:+1,DO:+1,
        DZ:+213,EC:+593,EE:+372,EG:+20,EH:+212,ER:+291,ES:+34,ET:+251,FI:+358,FJ:+679,
        FK:+500,FM:+691,FO:+298,FR:+33,GA:+241,GB:+44,GD:+1,GE:+995,GF:+594,GG:+44,
        GH:+233,GI:+350,GL:+299,GM:+220,GN:+224,GP:+590,GQ:+240,GR:+30,GS:+500,GT:+502,
        GU:+1,GW:+245,GY:+592,HK:+852,HN:+504,HR:+385,HT:+509,HU:+36,ID:+62,IE:+353,
        IL:+972,IM:+44,IN:+91,IO:+246,IQ:+964,IR:+98,IS:+354,IT:+39,JE:+44,JM:+1,
        JO:+962,JP:+81,KE:+254,KG:+996,KH:+855,KI:+686,KM:+269,KN:+1,KP:+850,KR:+82,
        KW:+965,KY:+1,KZ:+7,LA:+856,LB:+961,LC:+1,LI:+423,LK:+94,LR:+231,LS:+266,
        LT:+370,LU:+352,LV:+371,LY:+218,MA:+212,MC:+377,MD:+373,ME:+382,MF:+590,MG:+261,
        MH:+692,MK:+389,ML:+223,MM:+95,MN:+976,MO:+853,MP:+1,MQ:+596,MR:+222,MS:+1,
        MT:+356,MU:+230,MV:+960,MW:+265,MX:+52,MY:+60,MZ:+258,NA:+264,NC:+687,NE:+227,
        NF:+672,NG:+234,NI:+505,NL:+31,NO:+47,NP:+977,NR:+674,NU:+683,NZ:+64,OM:+968,
        PA:+507,PE:+51,PF:+689,PG:+675,PH:+63,PK:+92,PL:+48,PM:+508,PN:+64,PR:+1,
        PS:+970,PT:+351,PW:+680,PY:+595,QA:+974,RE:+262,RO:+40,RS:+381,RU:+7,RW:+250,
        SA:+966,SB:+677,SC:+248,SD:+249,SE:+46,SG:+65,SH:+290,SI:+386,SJ:+47,SK:+421,
        SL:+232,SM:+378,SN:+221,SO:+252,SR:+597,SS:+211,ST:+239,SV:+503,SX:+1,SY:+963,
        SZ:+268,TC:+1,TD:+235,TG:+228,TH:+66,TJ:+992,TK:+690,TL:+670,TM:+993,TN:+216,
        TO:+676,TR:+90,TT:+1,TV:+688,TW:+886,TZ:+255,UA:+380,UG:+256,US:+1,UY:+598,
        UZ:+998,VA:+39,VC:+1,VE:+58,VG:+1,VI:+1,VN:+84,VU:+678,WF:+681,WS:+685,XK:+383,
        YE:+967,YT:+262,ZA:+27,ZM:+260,ZW:+263
        """.trimIndent()
            .replace("\n", "")
            .split(',')
            .associate { entry ->
                val (countryCode, dialCode) = entry.trim().split(':', limit = 2)
                countryCode to dialCode
            }

    val allCountries: List<CountryOption> =
        (Locale.getISOCountries().asList() + "XK")
            .distinct()
            .map { countryCode ->
                CountryOption(
                    countryCode = countryCode,
                    dialCode = callingCodes[countryCode],
                    storageName = if (countryCode == "XK") "Kosovo" else localeForCountry(countryCode).getDisplayCountry(Locale.ENGLISH),
                )
            }
            .filterNot { it.dialCode == "+1" && it.countryCode != "US" }
            .sortedBy { it.storageName }

    val dialCodeOptions: List<DialCodeOption> = allCountries
        .mapNotNull { country ->
            country.dialCode?.let { dialCode ->
                DialCodeOption(
                    countryCode = country.countryCode,
                    dialCode = dialCode,
                    nameAr = country.name(AppLanguage.AR),
                    nameEn = country.name(AppLanguage.EN),
                    flag = country.flag,
                )
            }
        }
        .sortedWith(compareBy<DialCodeOption> { it.nameEn }.thenBy { it.dialCode })

    fun defaultCountry(): CountryOption =
        find("US") ?: allCountries.first()

    fun detectCountry(context: android.content.Context): CountryOption {
        val telephonyManager = context.getSystemService(android.content.Context.TELEPHONY_SERVICE) as? android.telephony.TelephonyManager
        val simCountry = telephonyManager?.simCountryIso?.uppercase(Locale.ROOT)
        if (!simCountry.isNullOrEmpty()) {
            find(simCountry)?.let { return it }
        }
        val networkCountry = telephonyManager?.networkCountryIso?.uppercase(Locale.ROOT)
        if (!networkCountry.isNullOrEmpty()) {
            find(networkCountry)?.let { return it }
        }
        val localeCountry = context.resources.configuration.locales.takeIf { !it.isEmpty }?.get(0)?.country?.uppercase(Locale.ROOT)
        if (!localeCountry.isNullOrEmpty()) {
            find(localeCountry)?.let { return it }
        }
        val defaultLocaleCountry = Locale.getDefault().country.uppercase(Locale.ROOT)
        if (!defaultLocaleCountry.isNullOrEmpty()) {
            find(defaultLocaleCountry)?.let { return it }
        }
        return find("US") ?: allCountries.first()
    }

    fun detectCurrency(countryCode: String): String {
        return try {
            val locale = Locale("", countryCode)
            java.util.Currency.getInstance(locale).currencyCode
        } catch (e: Exception) {
            "USD"
        }
    }

    fun find(value: String?): CountryOption? {
        val normalized = value?.trim().orEmpty()
        if (normalized.isBlank()) return null
        return allCountries.firstOrNull { country ->
            country.countryCode.equals(normalized, ignoreCase = true) ||
                country.storageName.equals(normalized, ignoreCase = true) ||
                country.name(AppLanguage.EN).equals(normalized, ignoreCase = true) ||
                country.name(AppLanguage.AR) == normalized
        } ?: legacyAliases[normalized.lowercase(Locale.ROOT)]?.let(::find)
    }

    fun display(value: String?, language: AppLanguage): String =
        find(value)?.label(language) ?: value.orEmpty()

    fun dialCodeFor(value: String?): String = find(value)?.dialCode ?: "+966"

    fun countryForDialCode(dialCode: String): CountryOption? =
        allCountries.firstOrNull { it.dialCode == dialCode }

    private val legacyAliases = mapOf(
        "السعودية" to "SA",
        "اليمن" to "YE",
        "الإمارات" to "AE",
        "مصر" to "EG",
        "الكويت" to "KW",
        "قطر" to "QA",
        "عمان" to "OM",
        "البحرين" to "BH",
        "الأردن" to "JO",
        "لبنان" to "LB",
        "المغرب" to "MA",
        "تونس" to "TN",
        "الجزائر" to "DZ",
        "ليبيا" to "LY",
        "السودان" to "SD",
        "العراق" to "IQ",
        "سوريا" to "SY",
        "فلسطين" to "PS",
        "uae" to "AE",
        "palestine" to "PS",
        "czech republic" to "CZ",
        "south korea" to "KR",
        "north korea" to "KP",
        "russia" to "RU",
        "vatican" to "VA",
    )
}

object CurrencyCatalog {
    private val preferredOptions: List<CurrencyOption> = listOf(
        "AED" to "AE", "AUD" to "AU", "BHD" to "BH", "BRL" to "BR", "CAD" to "CA",
        "CHF" to "CH", "CNY" to "CN", "DKK" to "DK", "DZD" to "DZ", "EGP" to "EG",
        "EUR" to "DE", "GBP" to "GB", "IDR" to "ID", "INR" to "IN", "IQD" to "IQ",
        "JOD" to "JO", "JPY" to "JP", "KWD" to "KW", "LBP" to "LB", "LYD" to "LY",
        "MAD" to "MA", "MXN" to "MX", "MYR" to "MY", "NGN" to "NG", "NOK" to "NO",
        "NZD" to "NZ", "OMR" to "OM", "PKR" to "PK", "QAR" to "QA", "SAR" to "SA",
        "SDG" to "SD", "SEK" to "SE", "SYP" to "SY", "THB" to "TH", "TND" to "TN",
        "TRY" to "TR", "USD" to "US", "YER" to "YE", "ZAR" to "ZA",
    ).map { (code, countryCode) -> CurrencyOption(code, countryCode) }

    val options: List<CurrencyOption> = (
        preferredOptions + CountryCatalog.allCountries.mapNotNull { country ->
            runCatching {
                Currency.getInstance(localeForCountry(country.countryCode)).currencyCode
            }.getOrNull()
                ?.takeUnless { it == "XXX" }
                ?.let { code -> CurrencyOption(code, country.countryCode) }
        }
    )
        .distinctBy { it.code }
        .sortedBy { it.code }

    fun find(code: String?): CurrencyOption? = options.firstOrNull { it.code.equals(code, ignoreCase = true) }

    fun display(code: String?, language: AppLanguage): String {
        val currency = find(code) ?: return code.orEmpty()
        val country = CountryCatalog.find(currency.countryCode)
        return listOf(currency.code, country?.name(language), country?.flag)
            .filter { !it.isNullOrBlank() }
            .joinToString(" - ")
            .replace(" - ${country?.flag}", " ${country?.flag}")
    }
}

fun filterDialCodeOptions(
    query: String,
    language: AppLanguage,
    options: List<DialCodeOption> = CountryCatalog.dialCodeOptions,
): List<DialCodeOption> {
    val normalizedQuery = query.trim()
    if (normalizedQuery.isBlank()) return options
    return options.filter { option ->
        option.dialCode.contains(normalizedQuery, ignoreCase = true) ||
            option.countryCode.contains(normalizedQuery, ignoreCase = true) ||
            option.label(language).contains(normalizedQuery, ignoreCase = true)
    }
}

fun filterCountryOptions(
    query: String,
    language: AppLanguage,
    options: List<CountryOption> = CountryCatalog.allCountries,
): List<CountryOption> {
    val normalizedQuery = query.trim()
    if (normalizedQuery.isBlank()) return options
    return options.filter { country ->
        country.countryCode.contains(normalizedQuery, ignoreCase = true) ||
            country.storageName.contains(normalizedQuery, ignoreCase = true) ||
            country.name(language).contains(normalizedQuery, ignoreCase = true) ||
            country.name(AppLanguage.AR).contains(normalizedQuery, ignoreCase = true) ||
            country.name(AppLanguage.EN).contains(normalizedQuery, ignoreCase = true) ||
            country.dialCode?.contains(normalizedQuery, ignoreCase = true) == true
    }
}

fun filterCurrencyOptions(
    query: String,
    language: AppLanguage,
    options: List<CurrencyOption> = CurrencyCatalog.options,
): List<CurrencyOption> {
    val normalizedQuery = query.trim()
    if (normalizedQuery.isBlank()) return options
    return options.filter { currency ->
        currency.code.contains(normalizedQuery, ignoreCase = true) ||
            CurrencyCatalog.display(currency.code, language)
                .contains(normalizedQuery, ignoreCase = true)
    }
}

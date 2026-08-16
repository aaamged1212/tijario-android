package app.tijario.domain

import app.tijario.config.AppLanguage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CountryCatalogTests {
    @Test
    fun catalogIncludesWorldCountriesAndMapsStoreCountryToDialCode() {
        assertTrue(CountryCatalog.allCountries.size >= 249)
        assertEquals("+966", CountryCatalog.dialCodeFor("Saudi Arabia"))
        assertEquals("+966", CountryCatalog.dialCodeFor("السعودية"))
        assertEquals("Saudi Arabia", CountryCatalog.countryForDialCode("+966")?.storageName)
    }

    @Test
    fun countryAndCurrencyLabelsContainLocalizedCountryAndFlag() {
        assertTrue(CurrencyCatalog.options.size >= 140)
        assertTrue(CountryCatalog.display("SA", AppLanguage.AR).contains("🇸🇦"))
        assertTrue(CurrencyCatalog.display("SAR", AppLanguage.EN).contains("SAR"))
        assertTrue(CurrencyCatalog.display("SAR", AppLanguage.EN).contains("🇸🇦"))
    }

    @Test
    fun dialCodeAndCurrencySearchSupportCodeCountryAndCallingCode() {
        assertTrue(filterDialCodeOptions("+966", AppLanguage.EN).any { it.countryCode == "SA" })
        assertTrue(filterDialCodeOptions("Saudi", AppLanguage.EN).any { it.countryCode == "SA" })
        assertTrue(filterCountryOptions("+966", AppLanguage.AR).any { it.countryCode == "SA" })
        assertTrue(filterCountryOptions("السعودية", AppLanguage.AR).any { it.countryCode == "SA" })
        assertTrue(filterCurrencyOptions("Saudi", AppLanguage.EN).any { it.code == "SAR" })
        assertTrue(filterCurrencyOptions("SAR", AppLanguage.AR).any { it.code == "SAR" })
    }
}

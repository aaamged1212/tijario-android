package app.tijario.config

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AppPreferencesDefaultsTest {
    @Test
    fun missingLanguagePreference_usesArabicSystemLanguage() {
        assertEquals(AppLanguage.AR, resolveInitialLanguage(null, "ar"))
    }

    @Test
    fun missingLanguagePreference_usesEnglishForOtherSystemLanguages() {
        assertEquals(AppLanguage.EN, resolveInitialLanguage(null, "en"))
        assertEquals(AppLanguage.EN, resolveInitialLanguage(null, "fr"))
    }

    @Test
    fun explicitLanguagePreference_overridesSystemLanguage() {
        assertEquals(AppLanguage.EN, resolveInitialLanguage("EN", "ar"))
        assertEquals(AppLanguage.AR, resolveInitialLanguage("AR", "en"))
    }

    @Test
    fun missingThemePreference_usesSystemTheme() {
        assertTrue(resolveInitialDarkMode(null, true))
        assertFalse(resolveInitialDarkMode(null, false))
    }

    @Test
    fun explicitThemePreference_overridesSystemTheme() {
        assertFalse(resolveInitialDarkMode(false, true))
        assertTrue(resolveInitialDarkMode(true, false))
    }

    @Test
    fun missingLanguagePreference_keepsSystemMode() {
        assertEquals(AppLanguageMode.SYSTEM, resolveLanguageMode(null))
        assertEquals(AppLanguageMode.ARABIC, resolveLanguageMode("AR"))
        assertEquals(AppLanguageMode.ENGLISH, resolveLanguageMode("EN"))
    }

    @Test
    fun themeMode_isBackwardCompatibleWithLegacyBooleanPreference() {
        assertEquals(AppThemeMode.SYSTEM, resolveThemeMode(null, null))
        assertEquals(AppThemeMode.DARK, resolveThemeMode(null, true))
        assertEquals(AppThemeMode.LIGHT, resolveThemeMode(null, false))
        assertEquals(AppThemeMode.SYSTEM, resolveThemeMode("SYSTEM", true))
    }
}

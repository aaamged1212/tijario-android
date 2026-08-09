package app.tijario.config

import org.junit.Assert.assertFalse
import org.junit.Test

class UiRecoveryLocalizationTests {
    @Test
    fun uiRecoveryKeysAreLocalized() {
        listOf(
            "tab_invoices",
            "tab_quotes",
            "latest_quote",
            "call",
            "whatsapp",
            "cant_open_phone",
            "preview_expand",
            "btn_close",
        ).forEach { key ->
            assertFalse(Localization.getString(key, AppLanguage.AR) == key)
            assertFalse(Localization.getString(key, AppLanguage.EN) == key)
        }
    }
}

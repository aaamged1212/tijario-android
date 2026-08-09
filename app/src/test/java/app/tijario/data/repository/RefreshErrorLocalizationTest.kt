package app.tijario.data.repository

import app.tijario.config.AppLanguage
import app.tijario.config.Localization
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class RefreshErrorLocalizationTest {
    @Test
    fun unknownRefreshErrorsUseTheArabicSafeFallback() {
        val raw = "database timeout at internal-host.example"
        val mapped = localizedRefreshError(IllegalStateException(raw), AppLanguage.AR)

        assertEquals(Localization.getString("doc_error_unexpected", AppLanguage.AR), mapped)
        assertFalse(mapped.contains(raw))
    }

    @Test
    fun knownRefreshErrorsRemainLocalizedInEnglish() {
        val mapped = localizedRefreshError(
            IllegalStateException("DOCUMENT_LIMIT_REACHED"),
            AppLanguage.EN,
        )

        assertEquals(Localization.getString("doc_error_limit_reached", AppLanguage.EN), mapped)
    }
}

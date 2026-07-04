package app.tijario.domain

import app.tijario.config.AppLanguage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class LocalizedErrorMapperTests {
    @Test
    fun arabicMessagesHideRawDocumentLimitCodes() {
        val limitMessage = LocalizedErrorMapper.map("DOCUMENT_LIMIT_REACHED", "quota limit exceeded", AppLanguage.AR)
        val quotaMessage = LocalizedErrorMapper.map("QUOTA_LIMIT_EXCEEDED", null, AppLanguage.AR)

        assertEquals(limitMessage, quotaMessage)
        assertFalse(limitMessage.contains("DOCUMENT_LIMIT_REACHED"))
        assertFalse(limitMessage.contains("quota limit exceeded"))
    }

    @Test
    fun englishMessagesPreferKnownMappings() {
        assertEquals(
            "Select a customer first.",
            LocalizedErrorMapper.map("select_customer_first", null, AppLanguage.EN),
        )
        assertEquals(
            "This template is not available on your current plan.",
            LocalizedErrorMapper.map("TEMPLATE_NOT_ALLOWED", null, AppLanguage.EN),
        )
        assertEquals(
            "Session expired. Please log in again.",
            LocalizedErrorMapper.map("unauthorized", null, AppLanguage.EN),
        )
    }
}

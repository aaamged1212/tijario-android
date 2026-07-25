package app.tijario.domain

import app.tijario.config.AppLanguage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Test

class LocalizedErrorMapperTests {
    @Test
    fun accountInitializationCodesUseLocalizedMessagesInsteadOfRawCodes() {
        val message = LocalizedErrorMapper.map("ENTITLEMENT_INITIALIZATION_REQUIRED", null, AppLanguage.AR)

        assertNotEquals("ENTITLEMENT_INITIALIZATION_REQUIRED", message)
        assertFalse(message.contains("ENTITLEMENT"))
    }

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

    @Test
    fun arabicMessagesDoNotExposeRawKnownCodes() {
        val message = LocalizedErrorMapper.map("DOCUMENT_LIMIT_REACHED", "quota limit exceeded", AppLanguage.AR)

        assertFalse(message.contains("DOCUMENT_LIMIT_REACHED"))
        assertFalse(message.contains("quota limit exceeded"))
        assertEquals(message, LocalizedErrorMapper.map("document_limit_reached", null, AppLanguage.AR))
    }

    @Test
    fun newDocumentSyncCodesResolveToLocalizedStrings() {
        assertEquals(
            "تعذر تحميل بنود المستند. حاول المزامنة مرة أخرى.",
            LocalizedErrorMapper.map("MISSING_DOCUMENT_ITEMS", null, AppLanguage.AR),
        )
        assertEquals(
            "Sync failed. Try again.",
            LocalizedErrorMapper.map("SYNC_FAILED", null, AppLanguage.EN),
        )
        assertEquals(
            "This action is blocked by your current plan.",
            LocalizedErrorMapper.map("BLOCKED_BY_PLAN", null, AppLanguage.EN),
        )
        assertEquals(
            "Could not save the document on the server. Try again.",
            LocalizedErrorMapper.map("SERVER_SAVE_FAILED", null, AppLanguage.EN),
        )
        assertEquals(
            "تعذر حفظ المستند على الخادم. حاول مرة أخرى.",
            LocalizedErrorMapper.map("server_save_failed", null, AppLanguage.AR),
        )
        assertEquals(
            "A server error occurred. Try again.",
            LocalizedErrorMapper.map("invalid_api_response", "unexpected body", AppLanguage.EN),
        )
        assertEquals(
            "Enter the document details correctly.",
            LocalizedErrorMapper.map("invalid_document_type", null, AppLanguage.EN),
        )
    }
}

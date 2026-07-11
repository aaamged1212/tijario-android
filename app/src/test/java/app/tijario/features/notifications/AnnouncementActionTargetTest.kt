package app.tijario.features.notifications

import app.tijario.config.AppLanguage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AnnouncementActionTargetTest {

    @Test
    fun arabicLanguage_usesArabicActionLabel() {
        val announcement = announcement(
            actionLabelAr = "عرض التفاصيل",
            actionLabelEn = "View details",
            deepLink = "tijario://announcements/abc12345",
        )

        val action = announcement.actionUiState(AppLanguage.AR)

        assertEquals("عرض التفاصيل", action?.label)
        assertEquals("tijario://announcements/abc12345", action?.target)
    }

    @Test
    fun englishLanguage_usesEnglishActionLabel() {
        val announcement = announcement(
            actionLabelAr = "عرض التفاصيل",
            actionLabelEn = "Open offer",
            deepLink = "https://tijario.site/offers/abc12345",
        )

        val action = announcement.actionUiState(AppLanguage.EN)

        assertEquals("Open offer", action?.label)
        assertEquals("https://tijario.site/offers/abc12345", action?.target)
    }

    @Test
    fun externalHttpsLink_isAllowed() {
        val announcement = announcement(
            actionLabelAr = "افتح الرابط",
            actionLabelEn = "Open link",
            deepLink = "https://example.com/deal",
        )

        val action = announcement.actionUiState(AppLanguage.EN)

        assertEquals("Open link", action?.label)
        assertEquals("https://example.com/deal", action?.target)
    }

    @Test
    fun invalidTarget_hidesActionButton() {
        val announcement = announcement(
            actionLabelAr = "عرض التفاصيل",
            actionLabelEn = "View details",
            deepLink = "javascript:alert(1)",
        )

        assertNull(announcement.actionUiState(AppLanguage.AR))
        assertNull(announcement.actionUiState(AppLanguage.EN))
    }

    @Test
    fun blankActionLabel_hidesActionButton() {
        val announcement = announcement(
            actionLabelAr = "   ",
            actionLabelEn = null,
            deepLink = "tijario://announcements/abc12345",
        )

        assertNull(announcement.actionUiState(AppLanguage.AR))
        assertNull(announcement.actionUiState(AppLanguage.EN))
    }

    private fun announcement(
        actionLabelAr: String?,
        actionLabelEn: String?,
        deepLink: String?,
    ) = Announcement(
        id = "abc12345",
        titleAr = "تنبيه",
        bodyAr = "نص عربي",
        titleEn = "Notice",
        bodyEn = "English text",
        actionLabelAr = actionLabelAr,
        actionLabelEn = actionLabelEn,
        deepLink = deepLink,
        priority = 1,
        publishedAt = "2026-06-29T00:00:00Z",
        expiresAt = null,
        isRead = false,
        isSeen = false,
        isDismissed = false,
    )
}

package app.tijario.data.remote

import org.junit.Assert.assertEquals
import org.junit.Test

class AdminNotificationCampaignContractTest {
    @Test
    fun `targeted campaign retains only the chosen account id`() {
        val request = AdminNotificationCampaignRequest(
            titleAr = "تحديث",
            bodyAr = "تم تفعيل ميزتك.",
            titleEn = "Update",
            bodyEn = "Your feature is active.",
            audience = "selected",
            targetUserIds = listOf("user-1"),
        )

        assertEquals("selected", request.audience)
        assertEquals(listOf("user-1"), request.targetUserIds)
    }
}

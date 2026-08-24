package app.tijario.ui.screens

import org.junit.Assert.assertTrue
import org.junit.Test

class AdminDashboardErrorMessageTest {
    @Test
    fun `surfaces a safe plan error instead of the generic action message`() {
        val message = adminErrorMessage(IllegalStateException("plan_invalid"), isArabic = true, loading = false)

        assertTrue(message.contains("الخطة"))
    }

    @Test
    fun `reports a pending server schema update without exposing a database error`() {
        val message = adminErrorMessage(IllegalStateException("admin_schema_unavailable"), isArabic = false, loading = false)

        assertTrue(message.contains("server"))
    }
}

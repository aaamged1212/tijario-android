package app.tijario.ui.screens

import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FeedbackAndDashboardUiContractTest {
    private val coreScreens = File("src/main/java/app/tijario/ui/screens/CoreScreens.kt").readText()
    private val settingsScreens = File("src/main/java/app/tijario/ui/screens/SettingsScreens.kt").readText()
    private val localization = File("src/main/java/app/tijario/config/Localization.kt").readText()
    private val repository = File("src/main/java/app/tijario/data/repository/TijarioRepository.kt").readText()

    @Test
    fun feedbackScreenUsesTijarioCopyAndThemeAwareImagePicker() {
        val feedbackScreen = settingsScreens.substringAfter("fun FeedbackScreen")

        assertTrue(feedbackScreen.contains("t(\"feedback_title\")"))
        assertTrue(feedbackScreen.contains("t(\"feedback_description\")"))
        assertTrue(feedbackScreen.contains("t(\"feedback_add_image\")"))
        assertTrue(feedbackScreen.contains("color = MaterialTheme.colorScheme.surfaceVariant"))
        assertTrue(feedbackScreen.contains("tint = MaterialTheme.colorScheme.primary"))
        assertTrue(feedbackScreen.contains("color = MaterialTheme.colorScheme.onSurfaceVariant"))
        assertFalse(feedbackScreen.contains("تحسين مساعد"))
        assertFalse(feedbackScreen.contains("Color(0xFF0F2537)"))
    }

    @Test
    fun feedbackUsesAuthenticatedMobileApiInsteadOfDirectPostgrestInsert() {
        val submitFeedback = repository
            .substringAfter("suspend fun submitUserFeedback")
            .substringBefore("internal fun buildProductSyncPayload")
        val apiClient = File("src/main/java/app/tijario/data/remote/BackendApiClient.kt").readText()
        val encoder = File("src/main/java/app/tijario/data/remote/FeedbackImageEncoder.kt").readText()

        assertTrue(submitFeedback.contains("backendApiClient.submitMobileFeedback"))
        assertFalse(submitFeedback.contains("from(\"user_feedbacks\")"))
        assertTrue(apiClient.contains("api/mobile/feedback"))
        assertTrue(encoder.contains("maxEncodedBytes = 512 * 1024"))
        assertTrue(encoder.contains("mimeType = \"image/jpeg\""))
    }

    @Test
    fun quickActionsRespectTheAppsSelectedThemeInsteadOfSystemTheme() {
        val quickAction = coreScreens.substringAfter("private fun QuickActionItem").substringBefore("private fun QuickActionButton")

        assertTrue(quickAction.contains("val isDark = AppRuntimeState.isDarkMode"))
        assertFalse(quickAction.contains("isSystemInDarkTheme()"))
    }

    @Test
    fun customerStatsUseShortLabelsBesideIconsAndNumbersBelow() {
        val customersScreen = coreScreens.substringAfter("fun CustomersScreen").substringBefore("fun ProductsScreen")
        val statLabel = coreScreens.substringAfter("private fun CustomerStatLabel").substringBefore("fun ProductsScreen")

        assertTrue(localization.contains("\"total_customers\" to mapOf(AppLanguage.AR to \"الكل\", AppLanguage.EN to \"All\")"))
        assertTrue(localization.contains("\"active_customers\" to mapOf(AppLanguage.AR to \"النشطون\", AppLanguage.EN to \"Active\")"))
        assertTrue(localization.contains("\"new_customers\" to mapOf(AppLanguage.AR to \"الجدد\", AppLanguage.EN to \"New\")"))
        assertTrue(localization.contains("\"most_active_customers\" to mapOf(AppLanguage.AR to \"الأفضل\", AppLanguage.EN to \"Top\")"))
        assertTrue(customersScreen.contains("CustomerStatLabel(Icons.Filled.People"))
        assertTrue(customersScreen.contains("Text(customers.size.toString(), fontSize = 16.sp"))
        assertTrue(statLabel.contains("Row("))
        assertTrue(statLabel.contains("Text(label, fontSize = 10.sp"))
    }
}

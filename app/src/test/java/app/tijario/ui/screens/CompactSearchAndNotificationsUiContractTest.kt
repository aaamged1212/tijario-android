package app.tijario.ui.screens

import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CompactSearchAndNotificationsUiContractTest {
    @Test
    fun customerProductAndDocumentFiltersOpenFromCompactButtons() {
        val source = File("src/main/java/app/tijario/ui/screens/CoreScreens.kt").readText()
        val customerSection = source.substringAfter("fun CustomersScreen").substringBefore("fun ProductsScreen")
        val productSection = source.substringAfter("fun ProductsScreen").substringBefore("fun DocumentsScreen")
        val documentSection = source.substringAfter("fun DocumentsScreen")

        assertTrue(customerSection.contains("CompactFilterButton("))
        assertTrue(productSection.contains("CompactFilterButton("))
        assertTrue(documentSection.contains("CompactFilterButton("))
        assertTrue(customerSection.contains("TijarioSearchField("))
        assertTrue(productSection.contains("TijarioSearchField("))
        assertTrue(documentSection.contains("TijarioSearchField("))
        assertFalse(customerSection.contains("// Horizontal Filter Chips Row"))
        assertFalse(documentSection.contains("// Filtering Chips under Tabs"))
    }

    @Test
    fun notificationsSupportPullRefreshAndUseRefreshCopy() {
        val source = File("src/main/java/app/tijario/features/notifications/NotificationsScreens.kt").readText()
        val notificationsScreen = source.substringAfter("fun NotificationsScreen").substringBefore("fun StartupAnnouncementDialog")

        assertTrue(notificationsScreen.contains("PullToRefreshBox("))
        assertTrue(notificationsScreen.contains("onRefresh = { viewModel.refresh(force = true) }"))
        assertTrue(notificationsScreen.contains("Text(t(\"refresh\"))"))
        assertFalse(notificationsScreen.contains("Text(t(\"retry\"))"))
    }
}

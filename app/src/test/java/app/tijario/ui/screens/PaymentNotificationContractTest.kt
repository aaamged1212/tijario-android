package app.tijario.ui.screens

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

class PaymentNotificationContractTest {
    private val contracts = File("src/main/java/app/tijario/data/remote/ApiContracts.kt").readText()
    private val paymentScreen = File("src/main/java/app/tijario/ui/screens/YemenPaymentMethodsScreen.kt").readText()
    private val notificationModels = File("src/main/java/app/tijario/features/notifications/NotificationModels.kt").readText()
    private val settingsScreen = File("src/main/java/app/tijario/ui/screens/SettingsScreens.kt").readText()

    @Test
    fun manualPaymentRequestCarriesTheSelectedIntervalAndApplicationLocale() {
        assertTrue(contracts.contains("@SerialName(\"billing_interval\") val billingInterval: String"))
        assertTrue(contracts.contains("val locale: String"))
        assertTrue(paymentScreen.contains("billingInterval = interval.lowercase()"))
        assertTrue(paymentScreen.contains("locale = if (isArabic) \"ar\" else \"en\""))
    }

    @Test
    fun targetPlanNoticesStayInTheLocalNotificationCacheWithoutGlobalReceipts() {
        assertTrue(contracts.contains("@SerialName(\"is_local\") val isLocal: Boolean"))
        assertTrue(notificationModels.contains("isLocal = isLocal"))
    }

    @Test
    fun subscriptionCardUsesTheServerPeriodEndAsItsRenewalDate() {
        assertTrue(contracts.contains("@SerialName(\"current_period_end\") val currentPeriodEnd"))
        assertTrue(settingsScreen.contains("formatRenewalDate(usage.renewalAt, language)"))
        assertTrue(settingsScreen.contains("Your plan renews on"))
    }
}

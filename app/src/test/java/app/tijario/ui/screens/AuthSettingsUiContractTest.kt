package app.tijario.ui.screens

import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AuthSettingsUiContractTest {
    private val authSource = File("src/main/java/app/tijario/ui/screens/AuthScreens.kt").readText()
    private val settingsSource = File("src/main/java/app/tijario/ui/screens/SettingsScreens.kt").readText()
    private val navigationSource = File("src/main/java/app/tijario/ui/TijarioApp.kt").readText()

    @Test
    fun authenticationScreens_useThemeAwareBackgroundAndAppearanceControls() {
        assertTrue(authSource.contains("private fun AuthScreenBackground"))
        assertTrue(authSource.contains("MaterialTheme.colorScheme.background"))
        assertTrue(authSource.contains("private fun AuthThemeToggle"))
        assertTrue(authSource.contains("AppPreferences.setDarkMode"))

        val loginAndAccountSetupSource = authSource.substringBefore("fun IntroWalkthroughScreen")
        assertFalse(loginAndAccountSetupSource.contains("Color(0xFF0F766E)"))
        assertFalse(loginAndAccountSetupSource.contains("Color(0xFF064E3B)"))
    }

    @Test
    fun settingsHome_showsCachedPlanAndProfileThenRoutesToDedicatedPaymentsScreen() {
        val settingsHome = settingsSource
            .substringAfter("fun SettingsHomeScreen")
            .substringBefore("private fun SettingsPlanBanner")
        assertTrue(settingsHome.contains("onPaymentsSubscriptions"))
        assertTrue(settingsHome.contains("Icons.Outlined.CreditCard"))
        assertTrue(settingsHome.contains("SettingsPlanBanner"))
        assertTrue(settingsHome.contains("SettingsProfileCard"))
        assertTrue(settingsHome.contains("planUsageState"))
        assertTrue(settingsSource.contains("Icons.AutoMirrored.Filled.KeyboardArrowRight"))
        assertFalse(settingsHome.contains("HorizontalDivider"))

        assertTrue(settingsSource.contains("fun PaymentsSubscriptionsScreen"))
        assertTrue(settingsSource.contains("fun PersonalProfileScreen"))
        assertTrue(settingsSource.contains("CurrentPlanUsageCard"))
        assertTrue(navigationSource.contains("composable(\"personal-profile\")"))
        assertTrue(navigationSource.contains("composable(\"payments-subscriptions\")"))
        assertTrue(navigationSource.contains("navController.navigateSingleTop(\"payments-subscriptions\")"))
        assertTrue(navigationSource.contains("SettingsHomeScreen(\n                        dataViewModel = dataViewModel"))
    }

    @Test
    fun appSettings_usesExplicitLanguagesAndSystemAwareThemeBottomSheets() {
        val appSettings = settingsSource
            .substringAfter("fun AppSettingsScreen")
            .substringBefore("private fun LocalNotificationSettingsSection")
        val languageSheet = appSettings
            .substringAfter("if (showLanguageSheet)")
            .substringBefore("if (showThemeSheet)")
        val languageOptions = languageSheet
            .substringAfter("options = listOf(")
            .substringBefore("onDismiss =")

        assertTrue(appSettings.contains("SettingsSelectionBottomSheet"))
        assertFalse(languageOptions.contains("AppLanguageMode.SYSTEM"))
        assertTrue(languageOptions.contains("AppLanguageMode.ARABIC"))
        assertTrue(languageOptions.contains("AppLanguageMode.ENGLISH"))
        assertTrue(appSettings.contains("AppThemeMode.SYSTEM"))
        assertTrue(appSettings.contains("Icons.Filled.Translate"))
        assertTrue(appSettings.contains("Icons.Filled.SettingsBrightness"))
        assertTrue(appSettings.contains("AppPreferences.setLanguageMode"))
        assertTrue(appSettings.contains("AppPreferences.setThemeMode"))
        assertFalse(appSettings.contains("AlertDialog"))
        assertFalse(appSettings.contains("Switch("))
    }
}

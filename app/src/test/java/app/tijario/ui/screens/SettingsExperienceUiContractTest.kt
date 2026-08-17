package app.tijario.ui.screens

import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SettingsExperienceUiContractTest {
    private val settingsSource = File("src/main/java/app/tijario/ui/screens/SettingsScreens.kt").readText()
    private val formsSource = File("src/main/java/app/tijario/ui/screens/FormScreens.kt").readText()
    private val componentsSource = File("src/main/java/app/tijario/ui/components/TijarioComponents.kt").readText()
    private val appSource = File("src/main/java/app/tijario/ui/TijarioApp.kt").readText()

    @Test
    fun businessCurrency_usesSearchableBottomSheetInsteadOfDropdown() {
        val businessSettings = formsSource
            .substringAfter("fun BusinessSettingsScreen")
            .substringBefore("val DocumentFormStateSaver")

        assertTrue(businessSettings.contains("var showCurrencyPicker"))
        assertTrue(businessSettings.contains("CurrencyPickerDialog("))
        assertTrue(businessSettings.contains("showCurrencyPicker = true"))
        assertTrue(formsSource.contains("filterCurrencyOptions(query, language)"))
        assertFalse(businessSettings.contains("activeDialog = \"currency\""))
        assertFalse(businessSettings.contains("SettingsDropdownField(\n                                label = t(\"currency\")"))
    }

    @Test
    fun businessInformationUsesDedicatedLabelsWithoutDuplicateHeaderEditAction() {
        val businessSettings = formsSource
            .substringAfter("fun BusinessSettingsScreen")
            .substringBefore("val DocumentFormStateSaver")
        val headerCard = businessSettings
            .substringAfter("// Header store card")
            .substringBefore("// Options card list")

        assertTrue(businessSettings.contains("title = { Text(t(\"business_information\")"))
        assertTrue(businessSettings.contains("title = t(\"business_name_title\")"))
        assertTrue(businessSettings.contains("title = t(\"business_phone_title\")"))
        assertTrue(businessSettings.contains("title = t(\"default_currency\")"))
        assertFalse(headerCard.contains("activeDialog = \"name\""))
        assertTrue(componentsSource.contains("verticalAlignment = Alignment.CenterVertically"))
    }

    @Test
    fun settingsOptions_areCompactAndSeparatedBySpace() {
        val settingsHome = settingsSource.substringBefore("fun PaymentsSubscriptionsScreen")
        val businessSettings = formsSource
            .substringAfter("fun BusinessSettingsScreen")
            .substringBefore("val DocumentFormStateSaver")

        assertTrue(settingsHome.contains("Arrangement.spacedBy(6.dp)"))
        assertTrue(settingsSource.contains("private fun CompactSettingsRow"))
        assertTrue(settingsSource.contains("modifier = Modifier.size(34.dp)"))
        assertTrue(businessSettings.contains("verticalArrangement = Arrangement.spacedBy(6.dp)"))
        assertFalse(businessSettings.contains("HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, modifier"))
    }

    @Test
    fun settingsHome_ordersSectionsAndSeparatesProfileFromAccountActions() {
        val settingsHome = settingsSource
            .substringAfter("fun SettingsHomeScreen")
            .substringBefore("private fun SettingsPlanBanner")
        val options = settingsHome.substringAfter("SettingsOption(Icons.Outlined.BusinessCenter")

        assertTrue(options.indexOf("account_settings") < options.indexOf("app_settings"))
        assertTrue(options.indexOf("app_settings") < options.indexOf("payments_subscriptions"))
        assertTrue(options.indexOf("payments_subscriptions") < options.indexOf("backup_restore"))
        assertTrue(settingsHome.contains("onPersonalProfile"))
        assertTrue(settingsSource.contains("fun PersonalProfileScreen"))
        assertTrue(settingsSource.contains("if (isEditingName) Icons.Filled.Check else Icons.Filled.Edit"))
        assertTrue(settingsSource.contains("delete_account_short_desc"))
    }

    @Test
    fun personalProfileRestoresEditablePhotoCardWithoutReplacingNameOrEmailFields() {
        val profile = settingsSource
            .substringAfter("fun PersonalProfileScreen")
            .substringBefore("fun AccountSettingsScreen")

        assertTrue(profile.contains("ActivityResultContracts.GetContent()"))
        assertTrue(profile.contains("photoPicker.launch(\"image/*\")"))
        assertTrue(profile.contains("edit_profile_photo"))
        assertTrue(profile.contains("profilePicFile.writeBytes"))
        assertTrue(profile.contains("if (isEditingName) Icons.Filled.Check else Icons.Filled.Edit"))
        assertTrue(profile.contains("label = { Text(t(\"email\")) }"))
    }

    @Test
    fun menuUsesHamburgerLabelAndLanguageAwareSlideTransition() {
        assertTrue(settingsSource.contains("title = { Text(t(\"menu\")"))
        assertTrue(appSource.contains("imageVector = Icons.Filled.Menu"))
        assertTrue(appSource.contains("contentDescription = t(\"menu\")"))
        assertTrue(appSource.contains("slideInHorizontally"))
        assertTrue(appSource.contains("AppRuntimeState.currentLanguage"))
    }

    @Test
    fun planBanner_showsUpgradeOnlyForFreeAndStarterPlans() {
        val planBanner = settingsSource
            .substringAfter("private fun SettingsPlanBanner")
            .substringBefore("private fun SettingsProfileCard")

        assertTrue(planBanner.contains("planCode == \"free\" || planCode == \"starter\""))
        assertTrue(planBanner.contains("if (canUpgrade)"))
        assertTrue(planBanner.contains("onUpgrade"))
        assertTrue(planBanner.contains("your_current_plan"))
        assertTrue(planBanner.contains("text = \"|\""))
        assertTrue(planBanner.contains("plan_upgrade_pitch"))
        assertTrue(planBanner.contains("fontSize = 16.sp"))
        assertFalse(planBanner.contains("Icons.AutoMirrored.Filled.KeyboardArrowRight"))
    }

    @Test
    fun appAppearanceUsesLargeUnboxedSystemAppearanceIcon() {
        val appSettings = settingsSource
            .substringAfter("fun AppSettingsScreen")
            .substringBefore("private fun LocalNotificationSettingsSection")

        assertTrue(appSettings.contains("SettingsSelectionOption(AppThemeMode.SYSTEM, t(\"theme_system\"), Icons.Filled.SettingsBrightness)"))
        assertTrue(appSettings.contains("showIconContainer = false"))
        assertTrue(appSettings.contains("iconSize = 28.dp"))
    }

    @Test
    fun pricingPlans_renderImmediatelyInHorizontalPagerWithOnlyProHighlighted() {
        val pricingSection = settingsSource
            .substringAfter("private fun PricingPlansSection")
            .substringBefore("private fun PricingPlanCard")

        assertTrue(settingsSource.contains("private fun fallbackPricingPlans"))
        assertTrue(pricingSection.contains("resolvedPricingPlans"))
        assertTrue(pricingSection.contains("HorizontalPager("))
        assertTrue(pricingSection.contains("SwipePricingPlanCard("))
        assertTrue(pricingSection.contains("val isPro = plan.code == \"pro\""))
        assertTrue(pricingSection.contains("width = if (isPro) 2.dp else 1.dp"))
        assertFalse(pricingSection.contains("if (!billingState.isLoading && plans.isEmpty())"))
        assertTrue(settingsSource.contains("val discountBadge"))
        assertFalse(settingsSource.contains("Text(save"))
        assertTrue(pricingSection.contains(".height(448.dp)"))
        assertTrue(pricingSection.contains("Daily or weekly automatic local backup"))
        assertTrue(pricingSection.contains("Google Drive backup and restore"))
    }
}

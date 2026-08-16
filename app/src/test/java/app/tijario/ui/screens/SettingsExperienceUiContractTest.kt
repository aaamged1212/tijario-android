package app.tijario.ui.screens

import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SettingsExperienceUiContractTest {
    private val settingsSource = File("src/main/java/app/tijario/ui/screens/SettingsScreens.kt").readText()
    private val formsSource = File("src/main/java/app/tijario/ui/screens/FormScreens.kt").readText()

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
    }
}

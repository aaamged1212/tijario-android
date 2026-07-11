package app.tijario.features.billing

import app.tijario.config.AppLanguage
import app.tijario.config.Localization
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BillingFoundationTests {
    @Test
    fun offerKeySeparatesMonthlyAndYearlyBasePlans() {
        assertEquals("pro:monthly", BillingCatalog.offerKey("PRO", BillingCatalog.INTERVAL_MONTHLY))
        assertEquals("pro:yearly", BillingCatalog.offerKey("pro", BillingCatalog.INTERVAL_YEARLY))
        assertTrue(BillingCatalog.supportedIntervals.contains(BillingCatalog.INTERVAL_MONTHLY))
        assertTrue(BillingCatalog.supportedIntervals.contains(BillingCatalog.INTERVAL_YEARLY))
    }

    @Test
    fun paidProductIdsUseOneGooglePlayProductPerPlan() {
        assertEquals("tijario_starter", BillingCatalog.productIdForPlan("starter"))
        assertEquals("tijario_pro", BillingCatalog.productIdForPlan("pro"))
        assertEquals("tijario_business", BillingCatalog.productIdForPlan("business"))
        assertFalse(BillingCatalog.paidProductIds.any { it.contains("yearly") || it.contains("monthly") })
    }

    @Test
    fun localizedBillingMessagesExistForArabicAndEnglish() {
        val keys = listOf(
            "billing_unavailable",
            "billing_product_unavailable",
            "billing_purchase_pending",
            "billing_verification_failed",
            "billing_ownership_conflict",
            "billing_restore_completed",
            "billing_catalog_unavailable",
        )

        keys.forEach { key ->
            assertFalse(Localization.getString(key, AppLanguage.AR).isBlank())
            assertFalse(Localization.getString(key, AppLanguage.EN).isBlank())
            assertFalse(Localization.getString(key, AppLanguage.AR) == key)
            assertFalse(Localization.getString(key, AppLanguage.EN) == key)
        }
    }

    @Test
    fun obfuscatedAccountIdDoesNotExposeRawUserId() {
        val userId = "c0047c1d-28be-454d-8ca3-b01a85d9783d"
        val obfuscated = BillingCatalog.obfuscatedAccountId(userId)
        assertEquals(64, obfuscated.length)
        assertFalse(obfuscated.contains(userId))
    }
}

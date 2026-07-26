package app.tijario.features.backup

import app.tijario.data.local.BackupSettingsEntity
import app.tijario.data.local.AccountEntitlementEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BackupPlanPolicyTest {
    private val settings = BackupSettingsEntity(
        userId = "user-1", frequency = "daily", wifiOnly = true, chargingOnly = false,
        driveEnabled = false, retentionDaily = 14, retentionWeekly = 8,
        retentionMonthly = 3, updatedAt = 1L,
    )

    @Test
    fun freeWeeklyPolicyRejectsDailyAndKeepsWeeklyAvailable() {
        val policy = BackupPlanPolicy("weekly", 0, 4, 0)

        assertTrue(policy.allows("weekly"))
        assertFalse(policy.allows("daily"))
        assertEquals("weekly", policy.apply(settings).frequency)
        assertEquals(4, policy.apply(settings).retentionWeekly)
    }

    @Test
    fun paidDailyPolicyAllowsDailyWithoutOverridingManualPreference() {
        val policy = BackupPlanPolicy("daily", 7, 4, 0)

        assertTrue(policy.allows("daily"))
        assertEquals("manual", policy.apply(settings.copy(frequency = "manual")).frequency)
        assertEquals(7, policy.apply(settings).retentionDaily)
    }

    @Test
    fun downgradeClampsFrequencyAndUsesSignedRetention() {
        val policy = BackupPlanPolicy("weekly", 0, 4, 0)
        val adjusted = policy.apply(settings)

        assertEquals("weekly", adjusted.frequency)
        assertEquals(0, adjusted.retentionDaily)
        assertEquals(4, adjusted.retentionWeekly)
        assertEquals(0, adjusted.retentionMonthly)
    }

    @Test
    fun policyUsesOnlyAStillValidPersistedSignedPayload() {
        val payload = """{
          "allowed_template_ids":[],"backup_frequency":"daily","backup_retention_daily":7,
          "backup_retention_monthly":3,"backup_retention_weekly":4,"data_mode":"local_drive",
          "document_limit":100,"document_limit_scope":"billing_cycle","documents_used":0,
          "entitlement_version":1,"expires_at":"2026-12-01T00:00:00Z","installation_id":"device",
          "issued_at":"2026-01-01T00:00:00Z","key_id":"key",
          "offline_credit_batch_size":10,"offline_entitlement_days":7,"plan_code":"starter",
          "remove_tijario_branding":false,"user_id":"user-1"
        }""".replace(Regex("\\s+"), "")
        val entitlement = AccountEntitlementEntity(
            userId = "user-1", planCode = "starter", dataMode = "local_drive",
            documentLimitScope = "billing_cycle", documentLimit = 100, documentsUsed = 0,
            customerLimit = null, productLimit = null, allowedTemplateIdsJson = "[]",
            removeTijarioBranding = false, entitlementVersion = 1, verifiedAt = 1,
            expiresAt = 1_800_000_000_000L, signedPayload = payload, signature = "signature",
        )

        assertEquals("daily", BackupPlanPolicy.from(entitlement, 1_700_000_000_000L).maximumFrequency)
        assertEquals("manual", BackupPlanPolicy.from(entitlement, 1_900_000_000_000L).maximumFrequency)
    }
}

package app.tijario.data.repository

import app.tijario.data.local.AccountEntitlementEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AccountDataModeTest {

    @Test
    fun unknownOrMissingModeRequiresEntitlementInitialization() {
        assertEquals(AccountDataMode.Uninitialized, AccountDataMode.from(null))
        assertEquals(AccountDataMode.Uninitialized, AccountDataMode.from("unexpected"))
        assertEquals(false, AccountDataMode.Uninitialized.allowsOperationalWrites)
    }

    @Test
    fun localDriveRequiresExplicitValue() {
        assertEquals(AccountDataMode.LocalDrive, AccountDataMode.from("local_drive"))
        assertEquals(true, AccountDataMode.LocalDrive.allowsOperationalWrites)
    }

    @Test
    fun operationalWritesRequireAKnownUnexpiredEntitlement() {
        val valid = entitlement("local_drive", 2_000L)

        assertTrue(hasValidOperationalEntitlement(valid, 1_000L))
        assertTrue(hasValidOperationalEntitlement(valid.copy(dataMode = "legacy_cloud"), 1_000L))
        assertFalse(hasValidOperationalEntitlement(null, 1_000L))
        assertFalse(hasValidOperationalEntitlement(valid.copy(dataMode = "unexpected"), 1_000L))
        assertFalse(hasValidOperationalEntitlement(valid.copy(expiresAt = 1_000L), 1_000L))
        assertFalse(hasValidOperationalEntitlement(valid.copy(expiresAt = null), 1_000L))
    }

    private fun entitlement(mode: String, expiresAt: Long) = AccountEntitlementEntity(
        userId = "user-1", planCode = "free", dataMode = mode,
        documentLimitScope = "lifetime", documentLimit = 5, documentsUsed = 0,
        customerLimit = null, productLimit = null, allowedTemplateIdsJson = "[]",
        removeTijarioBranding = false, entitlementVersion = 1, verifiedAt = 1,
        expiresAt = expiresAt, signedPayload = "payload", signature = "signature",
    )
}

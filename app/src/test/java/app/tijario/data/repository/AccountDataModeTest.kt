package app.tijario.data.repository

import org.junit.Assert.assertEquals
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
}

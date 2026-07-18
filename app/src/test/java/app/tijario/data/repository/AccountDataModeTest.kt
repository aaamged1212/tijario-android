package app.tijario.data.repository

import org.junit.Assert.assertEquals
import org.junit.Test

class AccountDataModeTest {

    @Test
    fun unknownOrMissingModePreservesLegacyCloudBehavior() {
        assertEquals(AccountDataMode.LegacyCloud, AccountDataMode.from(null))
        assertEquals(AccountDataMode.LegacyCloud, AccountDataMode.from("unexpected"))
    }

    @Test
    fun localDriveRequiresExplicitValue() {
        assertEquals(AccountDataMode.LocalDrive, AccountDataMode.from("local_drive"))
    }
}

package app.tijario.ui.state

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PlanUsageRefreshPolicyTest {
    @Test
    fun localDriveUsesCacheOnlyForNonForcedRefreshes() {
        assertTrue(shouldUseCachedLocalDrivePlanUsage(isLocalDriveAccount = true, force = false))
        assertFalse(shouldUseCachedLocalDrivePlanUsage(isLocalDriveAccount = true, force = true))
        assertFalse(shouldUseCachedLocalDrivePlanUsage(isLocalDriveAccount = false, force = false))
    }
}

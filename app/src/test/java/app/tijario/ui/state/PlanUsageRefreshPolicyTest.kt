package app.tijario.ui.state

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PlanUsageRefreshPolicyTest {
    @Test
    fun allAccountsUseCachedOperationalUsageForNonForcedRefreshes() {
        assertTrue(shouldUseCachedOperationalPlanUsage(force = false))
        assertFalse(shouldUseCachedOperationalPlanUsage(force = true))
    }
}

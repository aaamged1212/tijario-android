package app.tijario.data.repository

import org.junit.Assert.assertEquals
import org.junit.Test

class SyncRetryPolicyTests {
    @Test
    fun retriesUseExponentialBackoffAndStopAfterThreeAttempts() {
        assertEquals(10_000L, syncRetryDelayMs(1))
        assertEquals(20_000L, syncRetryDelayMs(2))
        assertEquals("PENDING", syncStatusAfterFailure(2))
        assertEquals("failed_non_retryable", syncStatusAfterFailure(3))
    }
}

package app.tijario.ui

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SettingsNavigationRecoveryTest {
    @Test
    fun failedBackStackPop_usesMainRouteFallback() {
        assertTrue(shouldFallbackToMainAfterBack(popSucceeded = false))
    }

    @Test
    fun normalBackStackPop_doesNotReplaceTheCurrentRoute() {
        assertFalse(shouldFallbackToMainAfterBack(popSucceeded = true))
    }
}

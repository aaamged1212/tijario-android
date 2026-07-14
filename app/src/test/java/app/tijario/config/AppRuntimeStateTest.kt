package app.tijario.config

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AppRuntimeStateTest {
    @Test
    fun authDeepLinkTargetIsUpdatedAndConsumedOnce() {
        AppRuntimeState.updateAuthDeepLinkTarget("/login")

        assertEquals("/login", AppRuntimeState.authDeepLinkTarget)

        AppRuntimeState.consumeAuthDeepLinkTarget()

        assertNull(AppRuntimeState.authDeepLinkTarget)
    }
}

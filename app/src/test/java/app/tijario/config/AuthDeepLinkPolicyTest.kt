package app.tijario.config

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.io.File

class AuthDeepLinkPolicyTest {
    @Test
    fun acceptsOnlyKnownCallbackOriginsAndInternalTargets() {
        assertEquals("/app/dashboard", AuthDeepLinkPolicy.resolveTarget("tijario://auth/callback?next=/app/dashboard"))
        assertEquals("/login", AuthDeepLinkPolicy.resolveTarget("com.tijario.app://auth/callback"))
        assertEquals("/login", AuthDeepLinkPolicy.resolveTarget("https://tijario.site/auth/callback?next=//example.invalid"))
        assertEquals("/login", AuthDeepLinkPolicy.resolveTarget("https://www.tijario.site/auth/callback?next=%2Flogin"))
    }

    @Test
    fun rejectsMalformedOrUntrustedCallbacks() {
        assertNull(AuthDeepLinkPolicy.resolveTarget("https://example.invalid/auth/callback?next=/login"))
        assertNull(AuthDeepLinkPolicy.resolveTarget("http://tijario.site/auth/callback?next=/login"))
        assertNull(AuthDeepLinkPolicy.resolveTarget("tijario://auth/other?next=/login"))
        assertNull(AuthDeepLinkPolicy.resolveTarget("javascript:alert(1)"))
    }

    @Test
    fun manifestDeclaresOnlyTheVerifiedTijarioCallbackHosts() {
        val manifest = File("src/main/AndroidManifest.xml").readText()

        assertEquals(true, manifest.contains("android:autoVerify=\"true\""))
        assertEquals(true, manifest.contains("android:host=\"tijario.site\""))
        assertEquals(true, manifest.contains("android:host=\"www.tijario.site\""))
        assertEquals(true, manifest.contains("android:pathPrefix=\"/auth/callback\""))
    }

    @Test
    fun manifestEnablesPredictiveBackForModernAndroid() {
        val manifest = File("src/main/AndroidManifest.xml").readText()

        assertEquals(true, manifest.contains("android:enableOnBackInvokedCallback=\"true\""))
    }
}

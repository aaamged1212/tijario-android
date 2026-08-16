package app.tijario.ui

import java.io.File
import java.nio.ByteBuffer
import java.security.MessageDigest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SplashScreenUiContractTest {
    private val styles = File("src/main/res/values/styles.xml").readText()
    private val splashDrawable = File("src/main/res/drawable/tijario_splash_icon.xml").readText()
    private val appSource = File("src/main/java/app/tijario/ui/TijarioApp.kt").readText()
    private val logoFile = File("src/main/res/drawable-nodpi/tijario_splash_logo.png")

    @Test
    fun systemSplash_usesTransparentSafeLogoOnDarkBackground() {
        assertTrue(styles.contains("parent=\"Theme.SplashScreen\""))
        assertTrue(styles.contains("@drawable/tijario_splash_icon"))
        assertFalse(styles.contains("windowSplashScreenIconBackgroundColor"))
        assertFalse(styles.contains("Theme.SplashScreen.IconBackground"))
        assertFalse(styles.contains("windowSplashScreenAnimatedIcon\">@drawable/logo_app"))
        assertTrue(splashDrawable.contains("@dimen/tijario_splash_logo_size"))
        assertTrue(splashDrawable.contains("@drawable/tijario_splash_logo"))
    }

    @Test
    fun suppliedLogo_isTheExactApprovedTransparentAsset() {
        val bytes = logoFile.readBytes()
        val width = ByteBuffer.wrap(bytes, 16, 4).int
        val height = ByteBuffer.wrap(bytes, 20, 4).int
        val sha256 = MessageDigest.getInstance("SHA-256")
            .digest(bytes)
            .joinToString(separator = "") { byte -> "%02X".format(byte) }

        assertEquals(2_000, width)
        assertEquals(2_000, height)
        assertEquals(
            "F354ADEC51CB5F45FC473D7D14083185F4153A22DD72F281370064A67D7CE8B8",
            sha256,
        )
    }

    @Test
    fun composeSplash_usesTheSameUnclippedBrandAsset() {
        val splashSource = appSource.substringAfter("fun SplashScreen()")

        assertTrue(splashSource.contains("R.drawable.tijario_splash_logo"))
        assertTrue(splashSource.contains("contentScale = ContentScale.Fit"))
        assertTrue(splashSource.contains("Color(0xFF0B1220)"))
        assertFalse(splashSource.contains("color = Color(0xFFF8FAFC)"))
        assertFalse(splashSource.contains("R.drawable.logo_app"))
    }
}

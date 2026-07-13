package app.tijario.config

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class AppPreferencesTest {

    @Test
    fun installationId_isStableAndUuidFormatted() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val first = AppPreferences.getInstallationId(context)
        val second = AppPreferences.getInstallationId(context)

        assertEquals(first, second)
        assertTrue(runCatching { UUID.fromString(first) }.isSuccess)
        assertNotEquals(android.os.Build.MODEL + "_" + android.os.Build.ID, first)
    }
}

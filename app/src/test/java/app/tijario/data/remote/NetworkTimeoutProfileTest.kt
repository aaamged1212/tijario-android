package app.tijario.data.remote

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test
import java.io.File

class NetworkTimeoutProfileTest {
    @Test
    fun apiRequestsUseBoundedInteractiveTimeouts() {
        val profile = NetworkTimeoutProfile.Api

        assertEquals(45_000L, profile.requestTimeoutMillis)
        assertEquals(15_000L, profile.connectTimeoutMillis)
        assertEquals(45_000L, profile.socketTimeoutMillis)
    }

    @Test
    fun driveTransfersUseLongerBoundedTimeouts() {
        val profile = NetworkTimeoutProfile.DriveTransfer

        assertEquals(300_000L, profile.requestTimeoutMillis)
        assertEquals(30_000L, profile.connectTimeoutMillis)
        assertEquals(300_000L, profile.socketTimeoutMillis)
    }

    @Test
    fun documentDebugLogsDoNotIncludeServerMessages() {
        val source = File("src/main/java/app/tijario/data/remote/BackendApiClient.kt").readText()

        assertFalse(source.contains("message=${'$'}{fallback.message}"))
        assertFalse(source.contains("message=${'$'}{result.message}"))
    }
}

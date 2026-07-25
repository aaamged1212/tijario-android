package app.tijario.data.repository

import app.tijario.data.model.UserPlanUsage
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AccountInitializationCoordinatorTest {
    @Test
    fun concurrentRequestsForOneInstallationShareOneEntitlementOperation() = runBlocking {
        val coordinator = AccountInitializationCoordinator()
        val started = CompletableDeferred<Unit>()
        val release = CompletableDeferred<Unit>()
        var calls = 0

        val first = async {
            coordinator.initialize("user-1", "install-1") {
                calls += 1
                started.complete(Unit)
                release.await()
                usage()
            }
        }
        started.await()
        val second = async { coordinator.initialize("user-1", "install-1") { usage() } }

        release.complete(Unit)

        assertTrue(first.await().isSuccess)
        assertTrue(second.await().isSuccess)
        assertEquals(1, calls)
    }

    @Test
    fun failureClearsInFlightRequestSoAnExplicitRetryCanRun() = runBlocking {
        val coordinator = AccountInitializationCoordinator()
        var calls = 0

        val failed = coordinator.initialize("user-1", "install-1") {
            calls += 1
            throw IllegalStateException("NETWORK_UNAVAILABLE")
        }
        val retried = coordinator.initialize("user-1", "install-1") {
            calls += 1
            usage()
        }

        assertTrue(failed.isFailure)
        assertTrue(retried.isSuccess)
        assertEquals(2, calls)
    }

    private fun usage() = UserPlanUsage(
        planCode = "free",
        planName = "Free",
        periodMonth = "2026-07-01",
        documentsUsed = 0,
        documentsLimit = 5,
        aiUsed = 0,
        aiLimit = 0,
    )
}

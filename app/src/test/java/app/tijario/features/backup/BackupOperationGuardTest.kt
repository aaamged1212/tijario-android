package app.tijario.features.backup

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class BackupOperationGuardTest {
    @Test
    fun sameAccountOperationsAreSerialized() = runBlocking {
        val firstEntered = CompletableDeferred<Unit>()
        val releaseFirst = CompletableDeferred<Unit>()
        val order = mutableListOf<String>()

        val first = async {
            BackupOperationGuard.withAccountLock("account-1") {
                order += "first-start"
                firstEntered.complete(Unit)
                releaseFirst.await()
                order += "first-end"
            }
        }
        firstEntered.await()
        val second = async {
            BackupOperationGuard.withAccountLock("account-1") {
                order += "second"
            }
        }
        delay(20)
        assertEquals(listOf("first-start"), order)
        releaseFirst.complete(Unit)
        first.await()
        second.await()

        assertEquals(listOf("first-start", "first-end", "second"), order)
    }
}

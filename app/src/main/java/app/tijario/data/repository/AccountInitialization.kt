package app.tijario.data.repository

import app.tijario.data.model.UserPlanUsage
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/** Prevents duplicate entitlement bootstrap requests for one account installation. */
internal class AccountInitializationCoordinator {
    private val mutex = Mutex()
    private var inFlight: InFlight? = null

    suspend fun initialize(
        userId: String,
        installationId: String,
        operation: suspend () -> UserPlanUsage,
    ): Result<UserPlanUsage> {
        val key = "$userId:$installationId"
        var ownsRequest = false
        val deferred = mutex.withLock {
            inFlight?.takeIf { it.key == key }?.result ?: CompletableDeferred<Result<UserPlanUsage>>().also {
                inFlight = InFlight(key, it)
                ownsRequest = true
            }
        }

        if (!ownsRequest) return deferred.await()

        try {
            val result = Result.success(operation())
            deferred.complete(result)
            return result
        } catch (error: CancellationException) {
            deferred.cancel(error)
            throw error
        } catch (error: Throwable) {
            val result = Result.failure<UserPlanUsage>(error)
            deferred.complete(result)
            return result
        } finally {
            mutex.withLock {
                if (inFlight?.result === deferred) inFlight = null
            }
        }
    }

    private data class InFlight(
        val key: String,
        val result: CompletableDeferred<Result<UserPlanUsage>>,
    )
}

internal class AccountInitializationException(
    val code: String,
    cause: Throwable? = null,
) : IllegalStateException(code, cause)

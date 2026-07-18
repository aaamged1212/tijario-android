package app.tijario.features.backup

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.ConcurrentHashMap

internal object BackupOperationGuard {
    private val accountLocks = ConcurrentHashMap<String, Mutex>()

    suspend fun <T> withAccountLock(userId: String, operation: suspend () -> T): T {
        require(userId.isNotBlank()) { "Backup account is required" }
        return accountLocks.getOrPut(userId) { Mutex() }.withLock { operation() }
    }
}

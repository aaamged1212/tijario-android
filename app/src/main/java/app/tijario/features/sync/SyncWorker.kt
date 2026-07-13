package app.tijario.features.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import app.tijario.data.AppContainer

class SyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val userId = inputData.getString("userId")?.takeIf { it.isNotBlank() }
            ?: return Result.failure()
        if (runAttemptCount >= MAX_WORKER_ATTEMPTS) return Result.failure()

        val repository = AppContainer.repository(applicationContext)
        return try {
            val syncResult = repository.sync(userId)
            if (syncResult.isSuccess) {
                Result.success()
            } else {
                resultFor(syncResult.exceptionOrNull())
            }
        } catch (error: Exception) {
            resultFor(error)
        }
    }

    private fun resultFor(error: Throwable?): Result {
        val code = error?.message.orEmpty()
        return when {
            code.contains("SESSION_EXPIRED", ignoreCase = true) -> Result.failure()
            code.contains("authentication_required", ignoreCase = true) -> Result.failure()
            code.contains("failed_non_retryable", ignoreCase = true) -> Result.failure()
            runAttemptCount + 1 >= MAX_WORKER_ATTEMPTS -> Result.failure()
            else -> Result.retry()
        }
    }

    private companion object {
        const val MAX_WORKER_ATTEMPTS = 3
    }
}

package app.tijario.features.notifications

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import app.tijario.config.Supabase
import app.tijario.data.local.TijarioDatabase

class NotificationReceiptSyncWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val userId = inputData.getString("userId") ?: return Result.failure()
        val repository = NotificationsRepository(
            context = applicationContext,
            database = TijarioDatabase.getInstance(applicationContext),
            backendApiClient = Supabase.apiClient,
        )

        return if (repository.syncPendingReceipts(userId).isSuccess) {
            Result.success()
        } else {
            Result.retry()
        }
    }
}

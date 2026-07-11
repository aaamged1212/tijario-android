package app.tijario.features.notifications

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import java.util.concurrent.TimeUnit

class NotificationReceiptSyncScheduler(private val context: Context) {
    fun trigger(userId: String) {
        if (userId.isBlank()) return
        val request = OneTimeWorkRequestBuilder<NotificationReceiptSyncWorker>()
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .setInputData(workDataOf("userId" to userId))
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 10, TimeUnit.SECONDS)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            "TijarioNotificationReceipts:$userId",
            ExistingWorkPolicy.REPLACE,
            request,
        )
    }
}

package app.tijario.analytics

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters

/** Schedules event delivery after connectivity returns without blocking business operations. */
class MobileAnalyticsEventUploadWorker(
    appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        MobileAnalyticsTracker.initialize(applicationContext)
        return if (MobileAnalyticsTracker.flushPendingEventsFromWorker()) Result.success() else Result.retry()
    }

    companion object {
        private const val UNIQUE_WORK_NAME = "tijario-mobile-analytics-events"

        fun enqueue(context: Context) {
            val request = OneTimeWorkRequestBuilder<MobileAnalyticsEventUploadWorker>()
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build(),
                )
                .build()
            WorkManager.getInstance(context.applicationContext).enqueueUniqueWork(
                UNIQUE_WORK_NAME,
                ExistingWorkPolicy.KEEP,
                request,
            )
        }
    }
}

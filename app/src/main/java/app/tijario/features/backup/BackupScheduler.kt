package app.tijario.features.backup

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import app.tijario.data.local.BackupSettingsEntity
import java.util.concurrent.TimeUnit

object BackupScheduler {
    fun apply(context: Context, settings: BackupSettingsEntity) {
        val workManager = WorkManager.getInstance(context.applicationContext)
        val workName = workName(settings.userId)
        val intervalDays = intervalDays(settings.frequency)
        if (intervalDays == null) {
            workManager.cancelUniqueWork(workName)
            return
        }

        val request = PeriodicWorkRequestBuilder<BackupWorker>(intervalDays, TimeUnit.DAYS)
            .setInitialDelay(intervalDays, TimeUnit.DAYS)
            .setInputData(workDataOf(BackupWorker.USER_ID_KEY to settings.userId))
            .setConstraints(
                Constraints.Builder()
                    .setRequiresCharging(settings.chargingOnly)
                    .build(),
            )
            .build()

        workManager.enqueueUniquePeriodicWork(
            workName,
            ExistingPeriodicWorkPolicy.UPDATE,
            request,
        )
    }

    internal fun intervalDays(frequency: String): Long? = when (frequency.lowercase()) {
        "daily" -> 1L
        "weekly" -> 7L
        else -> null
    }

    private fun workName(userId: String): String = "TijarioBackup:$userId"
}

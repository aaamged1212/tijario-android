package app.tijario.features.backup

import app.tijario.data.local.BackupSettingsEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class BackupSchedulePolicyTest {
    @Test
    fun scheduleIntervalsMatchManualDailyAndWeeklyPolicies() {
        assertNull(BackupScheduler.intervalDays("manual"))
        assertEquals(1L, BackupScheduler.intervalDays("daily"))
        assertEquals(7L, BackupScheduler.intervalDays("weekly"))
    }

    @Test
    fun automaticLocalBackupDoesNotRequireNetworkOrRetryLoop() {
        val scheduler = File("src/main/java/app/tijario/features/backup/BackupScheduler.kt").readText()
        val worker = File("src/main/java/app/tijario/features/backup/BackupWorker.kt").readText()

        val localRequest = scheduler.substringBefore("fun enqueueDriveUpload")
        assertFalse(localRequest.contains("setRequiredNetworkType"))
        assertTrue(worker.contains("allowNetwork = false"))
        assertFalse(worker.contains("Result.retry()"))
    }

    @Test
    fun driveUploadIsSeparateNetworkWorkWithBoundedRetries() {
        val scheduler = File("src/main/java/app/tijario/features/backup/BackupScheduler.kt").readText()
        assertTrue(scheduler.contains("OneTimeWorkRequestBuilder<DriveUploadWorker>"))
        assertTrue(scheduler.contains("setRequiredNetworkType"))
        assertEquals("TijarioDriveUpload:user-1:backup-1", BackupScheduler.driveWorkName("user-1", "backup-1"))
        assertEquals("TijarioDriveAccount:user-1", BackupScheduler.driveAccountTag("user-1"))
        assertEquals(3, DriveUploadWorker.MAX_ATTEMPTS)
    }

    @Test
    fun scheduleUiHasAllLocalizedPolicyOptions() {
        val source = File("src/main/java/app/tijario/ui/screens/BackupSettingsScreen.kt").readText()

        assertTrue(source.contains("backup_frequency_manual"))
        assertTrue(source.contains("backup_frequency_daily"))
        assertTrue(source.contains("backup_frequency_weekly"))
        assertTrue(source.contains("backup_charging_only"))
    }

    @Test
    fun retentionUsesFrequencySpecificLimitsAndKeepsAtLeastOneArchive() {
        val settings = BackupSettingsEntity(
            userId = "user-1",
            frequency = "daily",
            wifiOnly = true,
            chargingOnly = false,
            driveEnabled = false,
            retentionDaily = 7,
            retentionWeekly = 4,
            retentionMonthly = 3,
            updatedAt = 1L,
        )

        assertEquals(7, backupRetentionCount(settings))
        assertEquals(4, backupRetentionCount(settings.copy(frequency = "weekly")))
        assertEquals(1, backupRetentionCount(settings.copy(retentionDaily = 0)))
        assertNull(backupRetentionCount(settings.copy(frequency = "manual")))
        assertEquals(7, BackupScheduler.driveRetentionCount(settings))
        assertEquals(4, BackupScheduler.driveRetentionCount(settings.copy(frequency = "weekly")))
        assertEquals(3, BackupScheduler.driveRetentionCount(settings.copy(frequency = "manual")))
    }
}

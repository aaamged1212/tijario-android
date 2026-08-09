package app.tijario.features.backup

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class BackupRestoreWorkerContractTest {
    @Test
    fun restoreSourcesRunThroughOneCancellableForegroundWorker() {
        val scheduler = File("src/main/java/app/tijario/features/backup/BackupScheduler.kt").readText()
        val worker = File("src/main/java/app/tijario/features/backup/BackupRestoreWorker.kt").readText()

        assertTrue(scheduler.contains("enqueueDriveRestore"))
        assertTrue(scheduler.contains("enqueueFileRestore"))
        assertTrue(scheduler.contains("enqueueLocalRestore"))
        assertTrue(worker.contains("setForeground(notifier.foregroundInfo"))
        assertTrue(worker.contains("SOURCE_DRIVE"))
        assertTrue(worker.contains("SOURCE_FILE_URI"))
        assertTrue(worker.contains("SOURCE_LOCAL_RECORD"))
        assertTrue(worker.contains("BackupRestoreStage.CREATING_SAFETY_BACKUP"))
        assertTrue(worker.contains("BackupRestoreStage.RESTORING_FILES"))
        assertTrue(worker.contains("BackupRestoreStage.RESTORING_RECORDS"))
        assertTrue(worker.contains("allowNetwork = true"))
        assertTrue(worker.contains("releasePersistableUriPermission"))
        assertTrue(worker.contains("buildRestoreCompletionRecord"))
        assertTrue(worker.contains("HISTORY_RECORDED"))
    }

    @Test
    fun workerProgressAndFailuresExposeOnlySafeStageOrCodeData() {
        val worker = File("src/main/java/app/tijario/features/backup/BackupRestoreWorker.kt").readText()

        assertTrue(worker.contains("PROGRESS_STAGE_KEY"))
        assertTrue(worker.contains("ERROR_CODE_KEY to mapped.code.name"))
        assertTrue(worker.contains("WORK_FAILED_\${mapped.code.name}"))
    }
}

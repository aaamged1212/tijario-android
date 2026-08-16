package app.tijario.features.backup

import app.tijario.config.AppLanguage
import app.tijario.config.Localization
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class BackupUiContractTest {
    @Test
    fun backupActionsAreLocalizedInArabicAndEnglish() {
        val keys = listOf(
            "backup_restore",
            "backup_now",
            "backup_save_to_phone",
            "backup_drive_now",
            "backup_restore_file",
            "backup_restore_confirm_body",
            "backup_restore_failed",
            "backup_last_restored",
            "backup_status_restore_completed",
        )

        keys.forEach { key ->
            assertFalse(Localization.getString(key, AppLanguage.AR) == key)
            assertFalse(Localization.getString(key, AppLanguage.EN) == key)
        }
    }

    @Test
    fun backupScreenUsesNativeDocumentPickerAndRestoreConfirmation() {
        val source = File("src/main/java/app/tijario/ui/screens/BackupSettingsScreen.kt").readText()

        assertTrue(source.contains("ActivityResultContracts.CreateDocument"))
        assertTrue(source.contains("RestoreBackupDocumentContract"))
        assertTrue(source.contains("backup_restore_confirm_body"))
        assertTrue(source.contains("pendingRestoreUri"))
        assertFalse(source.contains("createLocalBackup(exportAfterCreate = true)"))
    }

    @Test
    fun backupScreen_groupsCompactLocalActionsAndKeepsClearSections() {
        val source = File("src/main/java/app/tijario/ui/screens/BackupSettingsScreen.kt").readText()
        val localSection = source
            .substringAfter("BackupSectionTitle(Icons.Filled.Folder")
            .substringBefore("BackupSectionTitle(Icons.Filled.Schedule")

        assertTrue(source.contains("private fun BackupSectionTitle"))
        assertTrue(localSection.contains("startPhoneBackup"))
        assertTrue(localSection.contains("restoreLauncher.launch"))
        assertTrue(source.contains("BackupSectionTitle(Icons.Filled.CloudDownload"))
        assertTrue(source.contains("BackupSectionTitle(Icons.Filled.Restore"))
        assertTrue(source.contains("verticalArrangement = Arrangement.spacedBy(10.dp)"))
    }

    @Test
    fun oversizedArchivesAreRejectedBeforeRestore() {
        val source = File("src/main/java/app/tijario/features/backup/BackupViewModel.kt").readText()
        val restoreWorker = File("src/main/java/app/tijario/features/backup/BackupRestoreWorker.kt").readText()
        val stager = File("src/main/java/app/tijario/features/backup/BackupArchiveInputStager.kt").readText()

        assertTrue(restoreWorker.contains("BackupArchiveInputStager.copyToPrivateFile"))
        assertFalse(source.contains("readBytes()"))
        assertTrue(stager.contains("if (total > maxBytes)"))
        assertTrue(source.contains("BackupTarget.PHONE"))
        assertTrue(source.contains("BackupTarget.GOOGLE_DRIVE"))
        assertTrue(source.contains("BackupScheduler.enqueueDriveUpload(getApplication(), settings, effectiveRecord.id, userInitiated = true)"))
        assertTrue(source.contains("getWorkInfoByIdFlow(workId)"))
        assertFalse(source.contains("error.message"))
    }
}

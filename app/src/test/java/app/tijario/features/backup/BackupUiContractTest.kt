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
            "backup_export_to",
            "backup_restore_file",
            "backup_restore_confirm_body",
            "backup_restore_failed",
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
        assertTrue(source.contains("ActivityResultContracts.OpenDocument"))
        assertTrue(source.contains("backup_restore_confirm_body"))
        assertTrue(source.contains("pendingRestoreUri"))
        assertFalse(source.contains("createLocalBackup(exportAfterCreate = true)"))
    }

    @Test
    fun oversizedArchivesAreRejectedBeforeRestore() {
        val source = File("src/main/java/app/tijario/features/backup/BackupViewModel.kt").readText()
        val stager = File("src/main/java/app/tijario/features/backup/BackupArchiveInputStager.kt").readText()

        assertTrue(source.contains("BackupArchiveInputStager.copyToPrivateFile"))
        assertFalse(source.contains("readBytes()"))
        assertTrue(stager.contains("if (total > maxBytes)"))
        assertTrue(source.contains("visible_backup_copy_failed error=\${error.javaClass.simpleName}"))
        assertFalse(source.contains("error.message"))
    }
}

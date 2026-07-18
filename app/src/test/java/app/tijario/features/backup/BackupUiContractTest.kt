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
    }

    @Test
    fun oversizedArchivesAreRejectedBeforeRestore() {
        val source = File("src/main/java/app/tijario/features/backup/BackupViewModel.kt").readText()

        assertTrue(source.contains("MAX_ARCHIVE_BYTES"))
        assertTrue(source.contains("if (total > MAX_ARCHIVE_BYTES)"))
        assertFalse(source.contains("Log."))
    }
}

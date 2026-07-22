package app.tijario.features.backup

import app.tijario.config.AppLanguage
import app.tijario.config.Localization
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class PhoneBackupContractTest {
    @Test
    fun phoneBackupUsesScopedStorageOrPersistedSafTreeWithoutBroadStorage() {
        val source = File("src/main/java/app/tijario/features/backup/PhoneBackupRepository.kt").readText()

        assertTrue(source.contains("MediaStore.Downloads.getContentUri"))
        assertTrue(source.contains("DocumentsContract.createDocument"))
        assertTrue(source.contains("takePersistableUriPermission"))
        assertFalse(source.contains("MANAGE_EXTERNAL_STORAGE"))
        assertFalse(source.contains("/storage/emulated/0"))
    }

    @Test
    fun sharingUsesContentUriAndReadPermissionOnlyForEncryptedArchives() {
        val source = File("src/main/java/app/tijario/features/backup/BackupShareIntents.kt").readText()

        assertTrue(source.contains("FileProvider.getUriForFile"))
        assertTrue(source.contains("FLAG_GRANT_READ_URI_PERMISSION"))
        assertTrue(source.contains("archive.name.endsWith(\".tijario\")"))
        assertFalse(source.contains("file://"))
    }

    @Test
    fun phoneBackupActionsHaveArabicAndEnglishLabels() {
        listOf("backup_phone_location", "backup_phone_folder", "backup_share", "backup_telegram").forEach { key ->
            assertFalse(Localization.getString(key, AppLanguage.AR) == key)
            assertFalse(Localization.getString(key, AppLanguage.EN) == key)
        }
    }

    @Test
    fun localHistorySupportsExplicitRestoreConfirmationAndSharing() {
        val source = File("src/main/java/app/tijario/ui/screens/BackupSettingsScreen.kt").readText()

        assertTrue(source.contains("pendingLocalRestore"))
        assertTrue(source.contains("restoreLocalRecord(record)"))
        assertTrue(source.contains("requestShareBackup(record"))
    }
}

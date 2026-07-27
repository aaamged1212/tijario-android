package app.tijario.features.backup

import app.tijario.config.AppLanguage
import app.tijario.config.Localization
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class PhoneBackupContractTest {
    @Test
    fun phoneBackupUsesScopedStorageOrPersistedSafTreeWithoutBroadStorage() {
        val source = File("src/main/java/app/tijario/features/backup/PhoneBackupRepository.kt").readText()

        assertTrue(source.contains("MediaStore.Downloads.getContentUri"))
        assertTrue(source.contains("\${Environment.DIRECTORY_DOWNLOADS}/Tijario/Backup/"))
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
    fun defaultLocalBackupFolderPathUsesEnglishSegmentsInBothLanguages() {
        assertEquals(
            "Downloads / Tijario / Backup",
            Localization.getString("backup_phone_folder_downloads", AppLanguage.AR),
        )
        assertEquals(
            "Downloads / Tijario / Backup",
            Localization.getString("backup_phone_folder_downloads", AppLanguage.EN),
        )
    }

    @Test
    fun androidQAndNewerAlwaysUseTheDefaultDownloadsBackupFolder() {
        val source = File("src/main/java/app/tijario/features/backup/PhoneBackupRepository.kt").readText()
        assertTrue(source.contains("Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> saveToMediaStore(source)"))
        assertTrue(source.contains("backup_phone_folder_downloads_tijario"))
        assertTrue(source.contains("backup_phone_folder_downloads_root"))
        assertTrue(
            source.indexOf("Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> saveToMediaStore(source)") <
                source.indexOf("selectedTree != null -> saveToTree(source, selectedTree)"),
        )
    }

    @Test
    fun localHistorySupportsExplicitRestoreConfirmationAndSharing() {
        val source = File("src/main/java/app/tijario/ui/screens/BackupSettingsScreen.kt").readText()

        assertTrue(source.contains("pendingLocalRestore"))
        assertTrue(source.contains("restoreLocalRecord(record)"))
        assertTrue(source.contains("requestShareBackup(record"))
    }
}

package app.tijario.features.backup

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class BackupDestinationContractTest {
    @Test
    fun manualPhoneAndDriveActionsUseSeparateTargets() {
        val viewModel = File("src/main/java/app/tijario/features/backup/BackupViewModel.kt").readText()
        val screen = File("src/main/java/app/tijario/ui/screens/BackupSettingsScreen.kt").readText()

        assertTrue(viewModel.contains("fun saveBackupToPhone"))
        assertTrue(viewModel.contains("fun backupNowToGoogleDrive"))
        assertTrue(viewModel.contains("BackupTarget.PHONE"))
        assertTrue(viewModel.contains("BackupTarget.GOOGLE_DRIVE"))
        assertTrue(screen.contains("backupViewModel.saveBackupToPhone()"))
        assertTrue(screen.contains("backupViewModel::backupNowToGoogleDrive"))
    }

    @Test
    fun driveBackupDoesNotCreateAVisiblePhoneCopyAndIgnoresAutomaticToggle() {
        val viewModel = File("src/main/java/app/tijario/features/backup/BackupViewModel.kt").readText()
        val scheduler = File("src/main/java/app/tijario/features/backup/BackupScheduler.kt").readText()

        val driveBranch = viewModel.substring(
            viewModel.indexOf("BackupTarget.GOOGLE_DRIVE ->"),
            viewModel.indexOf("BackupTarget.AUTOMATIC ->"),
        )
        assertFalse(driveBranch.contains("saveVisibleCopy"))
        assertTrue(scheduler.contains("(!settings.driveEnabled && !userInitiated)"))
        assertTrue(scheduler.contains(".setRequiresCharging(!userInitiated && settings.chargingOnly)"))
    }

    @Test
    fun restorePickerUsesConfiguredInitialFolderAndPersistedPermission() {
        val contract = File("src/main/java/app/tijario/features/backup/RestoreBackupDocumentContract.kt").readText()
        assertTrue(contract.contains("DocumentsContract.EXTRA_INITIAL_URI"))
        assertTrue(contract.contains("FLAG_GRANT_PERSISTABLE_URI_PERMISSION"))
        assertTrue(contract.contains("application/x-tijario-backup").not())
    }
}

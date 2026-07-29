package app.tijario.data.repository

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class AccountDeletionLocalCleanupTest {
    @Test
    fun localCleanupRunsOnlyAfterTheMobileEndpointSucceeds() {
        val app = File("src/main/java/app/tijario/ui/TijarioApp.kt").readText()
        val deleteFlow = app.substringAfter("onDeleteAccount = {").substringBefore("}\n                    )")

        assertTrue(deleteFlow.indexOf("apiClient.deleteAccount()") < deleteFlow.indexOf("deleteAccountLocal(userId)"))
        assertTrue(deleteFlow.contains("if (deleteResponse != null && !deleteResponse.ok)"))
        assertTrue(deleteFlow.contains("AppPreferences.markAccountDeletionCleanupPending(context, userId)"))
        assertTrue(deleteFlow.contains("AppPreferences.clearPendingAccountDeletionCleanup(context, userId)"))
        assertTrue(deleteFlow.contains("pendingAccountDeletionCleanupUserId(context) == userId"))
        assertTrue(deleteFlow.indexOf("authViewModel.logout()") == -1)
    }

    @Test
    fun successfulLocalCleanupCancelsAccountScopedBackgroundWork() {
        val repository = File("src/main/java/app/tijario/data/repository/TijarioRepository.kt").readText()

        assertTrue(repository.contains("BackupScheduler.cancelAccountWork(context, userId)"))
        assertTrue(repository.contains("SyncScheduler(context).cancel(userId)"))
        assertTrue(repository.contains("NotificationReceiptSyncScheduler(context).cancel(userId)"))
        assertTrue(repository.contains("deleteAccountLocalPath"))
        assertTrue(repository.contains("ACCOUNT_LOCAL_CLEANUP_FAILED"))
    }

    @Test
    fun startupRecoveryUsesThePendingMarkerAndOnlyClearsItAfterLocalCleanup() {
        val app = File("src/main/java/app/tijario/ui/TijarioApp.kt").readText()
        val auth = File("src/main/java/app/tijario/ui/state/AuthViewModel.kt").readText()
        val recovery = app.substringAfter("val pendingUserId = AppPreferences.pendingAccountDeletionCleanupUserId(context)")
            .substringBefore("accountDeletionRecoveryComplete = true")

        assertTrue(recovery.contains("dataViewModel.deleteAccountLocal(pendingUserId)"))
        assertTrue(recovery.contains("authViewModel.clearLocalSession(pendingUserId)"))
        assertTrue(recovery.indexOf("deleteAccountLocal(pendingUserId)") < recovery.indexOf("clearPendingAccountDeletionCleanup"))
        assertTrue(!recovery.contains("apiClient.deleteAccount"))
        assertTrue(!recovery.contains("currentUserId()"))
        assertTrue(auth.contains("suspend fun clearLocalSession"))
        assertTrue(auth.contains("supabaseClient.auth.clearSession()"))
        assertTrue(!auth.substringAfter("suspend fun clearLocalSession").substringBefore("\n    }").contains("signOut()"))
    }
}

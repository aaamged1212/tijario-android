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
    fun startupRecoveryBlocksAuthenticatedRoutingUntilLocalCleanupSucceeds() {
        val app = File("src/main/java/app/tijario/ui/TijarioApp.kt").readText()
        val auth = File("src/main/java/app/tijario/ui/state/AuthViewModel.kt").readText()
        val recovery = app.substringAfter("suspend fun recoverPendingAccountDeletionCleanup()")
            .substringBefore("LaunchedEffect(Unit)")

        assertTrue(app.contains("private sealed interface AccountDeletionRecoveryState"))
        assertTrue(app.contains("data object Running"))
        assertTrue(app.contains("data class Succeeded"))
        assertTrue(app.contains("data object Failed"))
        assertTrue(recovery.contains("dataViewModel.deleteAccountLocal(pendingUserId)"))
        assertTrue(recovery.contains("authViewModel.clearLocalSession(pendingUserId)"))
        assertTrue(recovery.indexOf("deleteAccountLocal(pendingUserId)") < recovery.indexOf("clearPendingAccountDeletionCleanup"))
        assertTrue(recovery.contains("accountDeletionRecovery = AccountDeletionRecoveryState.Failed"))
        assertTrue(!recovery.contains("apiClient.deleteAccount"))
        assertTrue(!recovery.contains("currentUserId()"))
        assertTrue(app.contains("if (!accountDeletionRecoverySucceeded) return@LaunchedEffect"))
        assertTrue(app.contains("AccountDeletionRecoveryState.Failed ->"))
        assertTrue(app.contains("AccountDeletionRecoveryFailedScreen("))
        assertTrue(app.contains("recoverPendingAccountDeletionCleanup()"))
        assertTrue(auth.contains("suspend fun clearLocalSession"))
        assertTrue(auth.contains("supabaseClient.auth.clearSession()"))
        assertTrue(!auth.substringAfter("suspend fun clearLocalSession").substringBefore("\n    }").contains("signOut()"))
    }
}

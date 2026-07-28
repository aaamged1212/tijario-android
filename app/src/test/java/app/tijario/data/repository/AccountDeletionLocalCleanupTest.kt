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
        assertTrue(deleteFlow.contains("if (res.ok)"))
    }

    @Test
    fun successfulLocalCleanupCancelsAccountScopedBackgroundWork() {
        val repository = File("src/main/java/app/tijario/data/repository/TijarioRepository.kt").readText()

        assertTrue(repository.contains("BackupScheduler.cancelAccountWork(context, userId)"))
        assertTrue(repository.contains("SyncScheduler(context).cancel(userId)"))
        assertTrue(repository.contains("NotificationReceiptSyncScheduler(context).cancel(userId)"))
    }
}

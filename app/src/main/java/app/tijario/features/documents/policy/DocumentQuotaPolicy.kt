package app.tijario.features.documents.policy

import android.content.Context
import app.tijario.data.AppContainer

class DocumentQuotaPolicy(private val context: Context) {
    suspend fun checkQuotaAndFinalize(documentId: String): Result<Unit> {
        val repo = AppContainer.repository(context)
        return repo.finalizeOrVerifyQuota(documentId)
    }
}

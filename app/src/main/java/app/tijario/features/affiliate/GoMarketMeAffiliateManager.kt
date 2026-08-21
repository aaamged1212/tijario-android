package app.tijario.features.affiliate

import android.content.Context
import android.util.Log
import app.tijario.BuildConfig
import co.gomarketme.kotlin.GoMarketMe
import co.gomarketme.kotlin.GoMarketMeTransactionSyncResult
import java.security.MessageDigest
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

internal interface GoMarketMeSdk {
    fun initialize(context: Context, apiKey: String)

    suspend fun syncAllTransactions(): GoMarketMeTransactionSyncResult
}

private object ProductionGoMarketMeSdk : GoMarketMeSdk {
    override fun initialize(context: Context, apiKey: String) {
        GoMarketMe.initialize(context, apiKey)
    }

    override suspend fun syncAllTransactions(): GoMarketMeTransactionSyncResult =
        GoMarketMe.syncAllTransactions()
}

/**
 * Optional affiliate attribution sidecar. Its lifecycle is deliberately isolated from core
 * authentication, Room, and billing completion paths.
 */
internal class GoMarketMeAffiliateManager(
    private val apiKey: String,
    private val sdk: GoMarketMeSdk,
    private val scope: CoroutineScope,
    private val debugLog: (String) -> Unit,
) {
    private val initializationRequested = AtomicBoolean(false)
    private val initialized = AtomicBoolean(false)
    private val reportedPurchaseFingerprints = ConcurrentHashMap.newKeySet<String>()

    fun initialize(context: Context) {
        if (apiKey.isBlank()) {
            debugLog("GoMarketMe initialization skipped: API key unavailable")
            return
        }
        if (!initializationRequested.compareAndSet(false, true)) return

        runCatching {
            sdk.initialize(context.applicationContext, apiKey)
        }.onSuccess {
            initialized.set(true)
            debugLog("GoMarketMe initialization requested")
        }.onFailure {
            initializationRequested.set(false)
            debugLog("GoMarketMe initialization failed")
        }
    }

    fun syncTransactionsForPurchase(purchaseToken: String) {
        if (apiKey.isBlank() || !initialized.get() || purchaseToken.isBlank()) {
            debugLog("GoMarketMe transaction sync skipped")
            return
        }

        val purchaseFingerprint = purchaseToken.sha256()
        if (!reportedPurchaseFingerprints.add(purchaseFingerprint)) {
            debugLog("GoMarketMe transaction sync skipped: duplicate purchase")
            return
        }

        scope.launch {
            runCatching {
                sdk.syncAllTransactions()
            }.onSuccess { result ->
                debugLog(
                    "GoMarketMe transaction sync completed: " +
                        "fetched=${result.fetchedCount}, sent=${result.sentCount}, failed=${result.failedCount}",
                )
            }.onFailure {
                // Permit a later legitimate Google Play callback to try once more.
                reportedPurchaseFingerprints.remove(purchaseFingerprint)
                debugLog("GoMarketMe transaction sync failed")
            }
        }
    }

    private fun String.sha256(): String = MessageDigest.getInstance("SHA-256")
        .digest(toByteArray(Charsets.UTF_8))
        .joinToString("") { byte -> "%02x".format(byte.toInt() and 0xff) }
}

object GoMarketMeAffiliate {
    private const val tag = "GoMarketMeAffiliate"
    private val manager = GoMarketMeAffiliateManager(
        apiKey = BuildConfig.GOMARKETME_API_KEY,
        sdk = ProductionGoMarketMeSdk,
        scope = CoroutineScope(SupervisorJob() + Dispatchers.IO),
        debugLog = { message ->
            if (BuildConfig.DEBUG) Log.d(tag, message)
        },
    )

    fun initialize(context: Context) = manager.initialize(context)

    fun syncTransactionsForPurchase(purchaseToken: String) =
        manager.syncTransactionsForPurchase(purchaseToken)
}

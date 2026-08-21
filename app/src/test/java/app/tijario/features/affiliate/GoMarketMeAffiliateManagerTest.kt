package app.tijario.features.affiliate

import android.content.Context
import co.gomarketme.kotlin.GoMarketMeTransactionSyncResult
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GoMarketMeAffiliateManagerTest {
    @Test
    fun missingApiKeySkipsInitializationAndTransactionSync() {
        val sdk = FakeGoMarketMeSdk()
        val manager = manager(apiKey = "", sdk = sdk)

        manager.initialize(applicationContext())
        manager.syncTransactionsForPurchase("purchase-token")

        assertEquals(0, sdk.initializationCalls)
        assertEquals(0, sdk.syncCalls)
    }

    @Test
    fun initializationIsIdempotentAndPurchaseSyncIsDeduplicated() {
        val sdk = FakeGoMarketMeSdk()
        val manager = manager(apiKey = "test-key", sdk = sdk)

        manager.initialize(applicationContext())
        manager.initialize(applicationContext())
        manager.syncTransactionsForPurchase("purchase-token")
        manager.syncTransactionsForPurchase("purchase-token")

        assertEquals(1, sdk.initializationCalls)
        assertEquals(1, sdk.syncCalls)
    }

    @Test
    fun syncFailureDoesNotEscapeAndAllowsOneLaterRetry() {
        val sdk = FakeGoMarketMeSdk(failFirstSync = true)
        val manager = manager(apiKey = "test-key", sdk = sdk)

        manager.initialize(applicationContext())
        manager.syncTransactionsForPurchase("purchase-token")
        manager.syncTransactionsForPurchase("purchase-token")

        assertEquals(2, sdk.syncCalls)
    }

    @Test
    fun initializationFailureDoesNotEscapeOrEnableTransactionSync() {
        val sdk = FakeGoMarketMeSdk(failInitialization = true)
        val debugMessages = mutableListOf<String>()
        val manager = manager(apiKey = "test-key", sdk = sdk, debugMessages = debugMessages)

        manager.initialize(applicationContext())
        manager.syncTransactionsForPurchase("purchase-token")

        assertEquals(1, sdk.initializationCalls)
        assertEquals(0, sdk.syncCalls)
        assertTrue(debugMessages.any { it == "GoMarketMe initialization failed" })
    }

    private fun manager(
        apiKey: String,
        sdk: FakeGoMarketMeSdk,
        debugMessages: MutableList<String> = mutableListOf(),
    ) = GoMarketMeAffiliateManager(
        apiKey = apiKey,
        sdk = sdk,
        scope = CoroutineScope(SupervisorJob() + Dispatchers.Unconfined),
        debugLog = debugMessages::add,
    )

    private fun applicationContext(): Context = mockk<Context>().also { context ->
        every { context.applicationContext } returns context
    }

    private class FakeGoMarketMeSdk(
        private val failInitialization: Boolean = false,
        private var failFirstSync: Boolean = false,
    ) : GoMarketMeSdk {
        var initializationCalls = 0
        var syncCalls = 0

        override fun initialize(context: Context, apiKey: String) {
            initializationCalls += 1
            if (failInitialization) error("initialization failure")
        }

        override suspend fun syncAllTransactions(): GoMarketMeTransactionSyncResult {
            syncCalls += 1
            if (failFirstSync) {
                failFirstSync = false
                error("sync failure")
            }
            return GoMarketMeTransactionSyncResult(
                fetchedCount = 1,
                sentCount = 1,
                failedCount = 0,
                success = true,
            )
        }
    }
}

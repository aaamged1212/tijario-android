package app.tijario.features.billing

import android.app.Activity
import android.content.Context
import app.tijario.data.remote.BackendApiClient
import app.tijario.data.remote.BillingPlanDto
import app.tijario.data.remote.GooglePlayVerifyRequest
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class GooglePlayBillingRepository(
    context: Context,
    private val backendApiClient: BackendApiClient,
) {
    private val appContext = context.applicationContext
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val purchaseEventsMutable = MutableSharedFlow<BillingPurchaseEvent>(extraBufferCapacity = 8)
    private var offerReferences: Map<String, OfferReference> = emptyMap()

    val purchaseEvents: SharedFlow<BillingPurchaseEvent> = purchaseEventsMutable

    private val billingClient: BillingClient = BillingClient.newBuilder(appContext)
        .setListener { billingResult, purchases ->
            scope.launch { handlePurchasesUpdated(billingResult, purchases.orEmpty()) }
        }
        .enablePendingPurchases(
            PendingPurchasesParams.newBuilder()
                // Billing Library requires pending purchase support to initialize. Tijario uses auto-renewing
                // subscriptions; one-time products are not sold here, but this flag is the supported builder option.
                .enableOneTimeProducts()
                .build()
        )
        .enableAutoServiceReconnection()
        .build()

    suspend fun loadCatalog(): Result<BillingCatalogSnapshot> = runCatching {
        val billingStatus = backendApiClient.fetchBillingStatus()
        val plans = billingStatus.data?.plans.orEmpty()
        ensureConnected()
        val productDetails = queryProductDetails()
        val offers = productDetails.flatMap { details ->
            val planCode = BillingCatalog.planCodeForProduct(details.productId) ?: return@flatMap emptyList()
            details.subscriptionOfferDetails.orEmpty()
                .filter { offer -> offer.basePlanId in BillingCatalog.supportedIntervals }
                .mapNotNull { offer ->
                    val formattedPrice = offer.pricingPhases.pricingPhaseList.firstOrNull()?.formattedPrice
                        ?.takeIf { it.isNotBlank() }
                        ?: return@mapNotNull null
                    val key = BillingCatalog.offerKey(planCode, offer.basePlanId)
                    key to OfferReference(
                        productDetails = details,
                        offer = offer,
                        ui = GooglePlayOfferUi(
                            planCode = planCode,
                            productId = details.productId,
                            basePlanId = offer.basePlanId,
                            formattedPrice = formattedPrice,
                        ),
                    )
                }
        }
        offerReferences = offers.toMap()
        BillingCatalogSnapshot(
            plans = plans,
            offers = offerReferences.mapValues { it.value.ui },
        )
    }

    suspend fun launchPurchase(
        activity: Activity,
        userId: String,
        planCode: String,
        billingInterval: String,
    ): Result<Unit> = runCatching {
        ensureConnected()
        val reference = offerReferences[BillingCatalog.offerKey(planCode, billingInterval)]
            ?: error("billing_product_unavailable")
        val productDetailsParams = BillingFlowParams.ProductDetailsParams.newBuilder()
            .setProductDetails(reference.productDetails)
            .setOfferToken(reference.offer.offerToken)
            .build()
        val flowParams = BillingFlowParams.newBuilder()
            .setObfuscatedAccountId(BillingCatalog.obfuscatedAccountId(userId))
            .setProductDetailsParamsList(listOf(productDetailsParams))
            .build()
        val result = billingClient.launchBillingFlow(activity, flowParams)
        if (result.responseCode != BillingClient.BillingResponseCode.OK) {
            error("billing_unavailable")
        }
    }

    suspend fun restorePurchases(): Result<Unit> = runCatching {
        ensureConnected()
        val purchases = queryPurchases()
        if (purchases.isEmpty()) {
            purchaseEventsMutable.emit(BillingPurchaseEvent.Failed("billing_no_active_purchases"))
        } else {
            purchases.forEach { purchase -> verifyPurchase(purchase) }
        }
    }

    fun close() {
        if (billingClient.isReady) {
            billingClient.endConnection()
        }
    }

    private suspend fun handlePurchasesUpdated(billingResult: BillingResult, purchases: List<Purchase>) {
        when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                if (purchases.isEmpty()) {
                    purchaseEventsMutable.emit(BillingPurchaseEvent.Failed("billing_no_purchase_returned"))
                    return
                }
                purchases.forEach { purchase -> verifyPurchase(purchase) }
            }
            BillingClient.BillingResponseCode.USER_CANCELED -> {
                purchaseEventsMutable.emit(BillingPurchaseEvent.Cancelled)
            }
            else -> {
                purchaseEventsMutable.emit(BillingPurchaseEvent.Failed("billing_google_play_failed"))
            }
        }
    }

    private suspend fun verifyPurchase(purchase: Purchase) {
        if (purchase.purchaseState == Purchase.PurchaseState.PENDING) {
            purchaseEventsMutable.emit(BillingPurchaseEvent.Pending)
            return
        }
        if (purchase.purchaseState != Purchase.PurchaseState.PURCHASED) return

        val productId = purchase.products.firstOrNull()
        if (productId.isNullOrBlank()) {
            purchaseEventsMutable.emit(BillingPurchaseEvent.Failed("billing_missing_product_data"))
            return
        }

        val response = runCatching {
            backendApiClient.verifyGooglePlayPurchase(
                GooglePlayVerifyRequest(
                    productId = productId,
                    purchaseToken = purchase.purchaseToken,
                )
            )
        }.getOrElse {
            purchaseEventsMutable.emit(BillingPurchaseEvent.Failed("billing_verification_failed"))
            return
        }

        if (!response.ok) {
            purchaseEventsMutable.emit(
                BillingPurchaseEvent.Failed(response.code ?: "billing_verification_failed")
            )
            return
        }

        if (response.data?.acknowledge == true && !purchase.isAcknowledged) {
            acknowledgePurchase(purchase.purchaseToken)
        }
        purchaseEventsMutable.emit(BillingPurchaseEvent.Verified)
    }

    private suspend fun ensureConnected() {
        if (billingClient.isReady) return
        suspendCancellableCoroutine { continuation ->
            billingClient.startConnection(object : BillingClientStateListener {
                override fun onBillingSetupFinished(billingResult: BillingResult) {
                    if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                        continuation.resume(Unit)
                    } else {
                        continuation.resumeWithException(
                            IllegalStateException(
                                "billing_unavailable"
                            )
                        )
                    }
                }

                override fun onBillingServiceDisconnected() = Unit
            })
        }
    }

    private suspend fun queryProductDetails(): List<ProductDetails> =
        suspendCancellableCoroutine { continuation ->
            val products = BillingCatalog.paidProductIds.map { productId ->
                QueryProductDetailsParams.Product.newBuilder()
                    .setProductId(productId)
                    .setProductType(BillingClient.ProductType.SUBS)
                    .build()
            }
            val params = QueryProductDetailsParams.newBuilder()
                .setProductList(products)
                .build()
            billingClient.queryProductDetailsAsync(params) { billingResult, result ->
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    continuation.resume(result.productDetailsList)
                } else {
                    continuation.resumeWithException(
                        IllegalStateException("billing_products_load_failed")
                    )
                }
            }
        }

    private suspend fun queryPurchases(): List<Purchase> =
        suspendCancellableCoroutine { continuation ->
            val params = QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
            billingClient.queryPurchasesAsync(params) { billingResult, purchases ->
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    continuation.resume(purchases)
                } else {
                    continuation.resumeWithException(
                        IllegalStateException("billing_restore_failed")
                    )
                }
            }
        }

    private suspend fun acknowledgePurchase(purchaseToken: String) {
        suspendCancellableCoroutine { continuation ->
            val params = AcknowledgePurchaseParams.newBuilder()
                .setPurchaseToken(purchaseToken)
                .build()
            billingClient.acknowledgePurchase(params) { billingResult ->
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    continuation.resume(Unit)
                } else {
                    continuation.resumeWithException(
                        IllegalStateException("billing_ack_failed")
                    )
                }
            }
        }
    }

    private data class OfferReference(
        val productDetails: ProductDetails,
        val offer: ProductDetails.SubscriptionOfferDetails,
        val ui: GooglePlayOfferUi,
    )
}

data class BillingCatalogSnapshot(
    val plans: List<BillingPlanDto>,
    val offers: Map<String, GooglePlayOfferUi>,
)

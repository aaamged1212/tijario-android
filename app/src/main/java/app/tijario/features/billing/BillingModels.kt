package app.tijario.features.billing

import app.tijario.data.remote.BillingPlanDto

data class GooglePlayOfferUi(
    val planCode: String,
    val productId: String,
    val basePlanId: String,
    val formattedPrice: String,
)

data class BillingUiState(
    val isLoading: Boolean = false,
    val isPurchasing: Boolean = false,
    val isRestoring: Boolean = false,
    val selectedInterval: String = BillingCatalog.INTERVAL_MONTHLY,
    val backendPlans: List<BillingPlanDto> = emptyList(),
    val googlePlayOffers: Map<String, GooglePlayOfferUi> = emptyMap(),
    val errorMessage: String? = null,
    val successMessage: String? = null,
) {
    fun offerFor(planCode: String, interval: String = selectedInterval): GooglePlayOfferUi? =
        googlePlayOffers[BillingCatalog.offerKey(planCode, interval)]
}

sealed interface BillingPurchaseEvent {
    data object Verified : BillingPurchaseEvent
    data object Pending : BillingPurchaseEvent
    data object Cancelled : BillingPurchaseEvent
    data class Failed(val message: String) : BillingPurchaseEvent
}

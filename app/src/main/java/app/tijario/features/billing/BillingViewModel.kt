package app.tijario.features.billing

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class BillingViewModel(
    private val repository: GooglePlayBillingRepository,
) : ViewModel() {
    private val stateMutable = MutableStateFlow(BillingUiState(isLoading = true))
    private val effectsMutable = MutableSharedFlow<BillingUiEffect>(extraBufferCapacity = 4)

    val state: StateFlow<BillingUiState> = stateMutable.asStateFlow()
    val effects: SharedFlow<BillingUiEffect> = effectsMutable.asSharedFlow()

    init {
        viewModelScope.launch {
            repository.purchaseEvents.collect { event ->
                when (event) {
                    is BillingPurchaseEvent.Verified -> {
                        stateMutable.update {
                            it.copy(
                                isPurchasing = false,
                                isRestoring = false,
                                successMessage = null,
                                errorMessage = null,
                            )
                        }

                        when (event.source) {
                            BillingVerificationSource.PURCHASE -> {
                                effectsMutable.emit(
                                    BillingUiEffect.PurchaseVerified(
                                        expectedPlanCode = event.planCode,
                                    )
                                )
                            }

                            BillingVerificationSource.SYNC -> {
                                effectsMutable.emit(
                                    BillingUiEffect.SubscriptionSynced(
                                        expectedPlanCode = event.planCode,
                                    )
                                )
                            }
                        }

                        load()
                    }

                    BillingPurchaseEvent.Pending -> {
                        stateMutable.update {
                            it.copy(
                                isPurchasing = false,
                                isRestoring = false,
                                successMessage = null,
                                errorMessage = "billing_purchase_pending",
                            )
                        }
                    }

                    BillingPurchaseEvent.Cancelled -> {
                        stateMutable.update {
                            it.copy(
                                isPurchasing = false,
                                isRestoring = false,
                                errorMessage = null,
                                successMessage = null,
                            )
                        }
                    }

                    is BillingPurchaseEvent.Failed -> {
                        stateMutable.update {
                            it.copy(
                                isPurchasing = false,
                                isRestoring = false,
                                errorMessage = normalizeBillingErrorCode(event.message),
                                successMessage = null,
                            )
                        }
                    }
                }
            }
        }
    }

    fun selectInterval(interval: String) {
        if (interval !in BillingCatalog.supportedIntervals) return
        stateMutable.update {
            it.copy(
                selectedInterval = interval,
                errorMessage = null,
                successMessage = null,
            )
        }
    }

    fun load() {
        viewModelScope.launch {
            stateMutable.update { it.copy(isLoading = true, errorMessage = null) }
            repository.loadCatalog()
                .onSuccess { snapshot ->
                    stateMutable.update {
                        it.copy(
                            isLoading = false,
                            backendPlans = snapshot.plans,
                            googlePlayOffers = snapshot.offers,
                            errorMessage = null,
                        )
                    }
                }
                .onFailure { error ->
                    stateMutable.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = normalizeBillingErrorCode(error.message) ?: "billing_unavailable",
                        )
                    }
                }
        }
    }

    fun purchase(activity: Activity, userId: String, planCode: String) {
        val interval = stateMutable.value.selectedInterval
        viewModelScope.launch {
            stateMutable.update {
                it.copy(
                    isPurchasing = true,
                    errorMessage = null,
                    successMessage = null,
                )
            }
            repository.launchPurchase(activity, userId, planCode, interval)
                .onFailure { error ->
                    stateMutable.update {
                        it.copy(
                            isPurchasing = false,
                            errorMessage = normalizeBillingErrorCode(error.message) ?: "billing_unavailable",
                        )
                    }
                }
        }
    }

    fun restorePurchases() {
        viewModelScope.launch {
            stateMutable.update {
                it.copy(
                    isRestoring = true,
                    errorMessage = null,
                    successMessage = null,
                )
            }
            repository.restorePurchases()
                .onFailure { error ->
                    stateMutable.update {
                        it.copy(
                            isRestoring = false,
                            errorMessage = normalizeBillingErrorCode(error.message) ?: "billing_restore_failed",
                        )
                    }
                }
        }
    }

    override fun onCleared() {
        repository.close()
        super.onCleared()
    }

    private fun normalizeBillingErrorCode(message: String?): String? {
        val code = message?.trim().orEmpty()
        return when {
            code.isBlank() -> null
            code.startsWith("billing_") -> code
            code == "unauthorized" || code == "unauthenticated" -> "billing_unavailable"
            else -> "billing_unavailable"
        }
    }
}

package app.tijario.features.billing

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class BillingViewModel(
    private val repository: GooglePlayBillingRepository,
) : ViewModel() {
    private val stateMutable = MutableStateFlow(BillingUiState(isLoading = true))
    val state: StateFlow<BillingUiState> = stateMutable.asStateFlow()

    init {
        viewModelScope.launch {
            repository.purchaseEvents.collect { event ->
                when (event) {
                    BillingPurchaseEvent.Verified -> {
                        stateMutable.update {
                            it.copy(
                                isPurchasing = false,
                                isRestoring = false,
                                successMessage = "billing_purchase_verified",
                                errorMessage = null,
                            )
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
                                errorMessage = event.message,
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
        stateMutable.update { it.copy(selectedInterval = interval, errorMessage = null, successMessage = null) }
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
                            errorMessage = error.message ?: "billing_unavailable",
                        )
                    }
                }
        }
    }

    fun purchase(activity: Activity, userId: String, planCode: String) {
        val interval = stateMutable.value.selectedInterval
        viewModelScope.launch {
            stateMutable.update { it.copy(isPurchasing = true, errorMessage = null, successMessage = null) }
            repository.launchPurchase(activity, userId, planCode, interval)
                .onFailure { error ->
                    stateMutable.update {
                        it.copy(
                            isPurchasing = false,
                            errorMessage = error.message ?: "billing_unavailable",
                        )
                    }
                }
        }
    }

    fun restorePurchases() {
        viewModelScope.launch {
            stateMutable.update { it.copy(isRestoring = true, errorMessage = null, successMessage = null) }
            repository.restorePurchases()
                .onFailure { error ->
                    stateMutable.update {
                        it.copy(
                            isRestoring = false,
                            errorMessage = error.message ?: "billing_restore_failed",
                        )
                    }
                }
        }
    }

    override fun onCleared() {
        repository.close()
        super.onCleared()
    }
}

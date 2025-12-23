package com.maa.health.ui.screens.premium

import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maa.health.data.remote.payment.GPayPaymentService
import com.maa.health.data.remote.payment.GPayResult
import com.maa.health.data.remote.payment.GPayState
import com.maa.health.data.remote.payment.SubscriptionPlan
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Premium Subscription Screen
 *
 * Handles Google Pay payment flow for Maa Premium subscriptions
 */
@HiltViewModel
class PremiumViewModel @Inject constructor(
    private val gPayService: GPayPaymentService
) : ViewModel() {

    private val _uiState = MutableStateFlow(PremiumUiState())
    val uiState: StateFlow<PremiumUiState> = _uiState.asStateFlow()

    val paymentState: StateFlow<GPayState> = gPayService.paymentState

    private var pendingPaymentIntent: Intent? = null

    init {
        loadSubscriptionPlans()
        checkGPayAvailability()
    }

    private fun loadSubscriptionPlans() {
        _uiState.value = _uiState.value.copy(
            subscriptionPlans = gPayService.getSubscriptionPlans()
        )
    }

    private fun checkGPayAvailability() {
        val isInstalled = gPayService.isGPayInstalled()
        val appInfo = gPayService.getGPayAppInfo()

        _uiState.value = _uiState.value.copy(
            isGPayInstalled = isInstalled,
            gPayVersion = appInfo?.versionName
        )
    }

    /**
     * Create payment intent for selected plan
     * Returns Intent to start for result in Activity
     */
    fun createPaymentIntent(plan: SubscriptionPlan): Intent {
        _uiState.value = _uiState.value.copy(
            selectedPlan = plan,
            isPaymentInProgress = true
        )

        val intent = gPayService.createPaymentForPlan(plan)
        pendingPaymentIntent = intent
        return intent
    }

    /**
     * Handle payment result from Activity
     */
    fun handlePaymentResult(resultCode: Int, data: Intent?) {
        val result = gPayService.handlePaymentResult(resultCode, data)

        when (result) {
            is GPayResult.Success -> {
                _uiState.value = _uiState.value.copy(
                    isPaymentInProgress = false,
                    isPremiumActivated = true,
                    transactionId = result.txnId
                )
                // TODO: Save premium status to local DB and sync to backend
                savePremiumStatus(result.txnId, result.txnRef)
            }

            is GPayResult.Pending -> {
                _uiState.value = _uiState.value.copy(
                    isPaymentInProgress = false,
                    isPaymentPending = true,
                    transactionId = result.txnId
                )
                // TODO: Start background check for payment confirmation
            }

            is GPayResult.Failed -> {
                _uiState.value = _uiState.value.copy(
                    isPaymentInProgress = false,
                    errorMessage = result.message
                )
            }

            is GPayResult.Cancelled -> {
                _uiState.value = _uiState.value.copy(
                    isPaymentInProgress = false
                )
            }
        }
    }

    /**
     * Save premium status after successful payment
     */
    private fun savePremiumStatus(txnId: String, txnRef: String) {
        viewModelScope.launch {
            // TODO: Implement premium status persistence
            // 1. Save to local Room database
            // 2. Sync to Firebase/backend
            // 3. Update user profile
        }
    }

    /**
     * Reset payment state for retry
     */
    fun resetPayment() {
        gPayService.resetState()
        _uiState.value = _uiState.value.copy(
            isPaymentInProgress = false,
            errorMessage = null,
            isPaymentPending = false
        )
    }

    /**
     * Get Play Store intent to install GPay
     */
    fun getGPayInstallIntent(): Intent {
        return gPayService.getGPayPlayStoreIntent()
    }

    /**
     * Clear error message
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}

/**
 * UI State for Premium Screen
 */
data class PremiumUiState(
    val subscriptionPlans: List<SubscriptionPlan> = emptyList(),
    val selectedPlan: SubscriptionPlan? = null,
    val isGPayInstalled: Boolean = false,
    val gPayVersion: String? = null,
    val isPaymentInProgress: Boolean = false,
    val isPaymentPending: Boolean = false,
    val isPremiumActivated: Boolean = false,
    val transactionId: String? = null,
    val errorMessage: String? = null
)

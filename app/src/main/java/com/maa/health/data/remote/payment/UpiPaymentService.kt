package com.maa.health.data.remote.payment

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * UPI Payment Service for Maa App
 *
 * Supports multiple UPI apps:
 * - Google Pay (GPay)
 * - PhonePe
 * - Paytm
 * - BHIM
 * - Any UPI-enabled app
 *
 * Payment Flow:
 * 1. Create UPI intent with payment details
 * 2. User selects preferred UPI app
 * 3. User authenticates in UPI app
 * 4. Payment result returned to app
 * 5. Verify transaction on backend
 *
 * Requirements for production:
 * - Indian business entity (Pvt Ltd/LLP)
 * - Indian bank current account
 * - Payment aggregator (Razorpay/PayU/Cashfree)
 * - GST registration
 */
@Singleton
class UpiPaymentService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val _paymentState = MutableStateFlow<PaymentState>(PaymentState.Idle)
    val paymentState: StateFlow<PaymentState> = _paymentState.asStateFlow()

    // UPI app package names
    companion object {
        const val GPAY_PACKAGE = "com.google.android.apps.nbu.paisa.user"
        const val PHONEPE_PACKAGE = "com.phonepe.app"
        const val PAYTM_PACKAGE = "net.one97.paytm"
        const val BHIM_PACKAGE = "in.org.npci.upiapp"

        // Request codes
        const val UPI_PAYMENT_REQUEST = 1001

        // Premium subscription prices (in INR)
        const val PREMIUM_MONTHLY_PRICE = 99
        const val PREMIUM_YEARLY_PRICE = 799
        const val PREMIUM_LIFETIME_PRICE = 1999
    }

    /**
     * Check which UPI apps are installed
     */
    fun getInstalledUpiApps(): List<UpiApp> {
        val pm = context.packageManager
        val installedApps = mutableListOf<UpiApp>()

        val upiApps = listOf(
            UpiApp("Google Pay", GPAY_PACKAGE, UpiAppType.GPAY),
            UpiApp("PhonePe", PHONEPE_PACKAGE, UpiAppType.PHONEPE),
            UpiApp("Paytm", PAYTM_PACKAGE, UpiAppType.PAYTM),
            UpiApp("BHIM", BHIM_PACKAGE, UpiAppType.BHIM)
        )

        upiApps.forEach { app ->
            if (isPackageInstalled(app.packageName, pm)) {
                installedApps.add(app)
            }
        }

        return installedApps
    }

    /**
     * Check if any UPI app is installed
     */
    fun isUpiAvailable(): Boolean {
        return getInstalledUpiApps().isNotEmpty()
    }

    /**
     * Create UPI payment intent
     *
     * @param amount Amount in INR
     * @param orderId Unique order/transaction ID
     * @param description Payment description
     * @param merchantVpa Merchant UPI VPA (Virtual Payment Address)
     * @param merchantName Merchant display name
     */
    fun createUpiPaymentIntent(
        amount: Int,
        orderId: String,
        description: String,
        merchantVpa: String = "maahealth@razorpay", // Replace with actual VPA
        merchantName: String = "Maa Health",
        currency: String = "INR"
    ): Intent {
        // UPI deep link format
        val upiUri = Uri.Builder()
            .scheme("upi")
            .authority("pay")
            .appendQueryParameter("pa", merchantVpa)       // Payee VPA
            .appendQueryParameter("pn", merchantName)      // Payee name
            .appendQueryParameter("mc", "5411")            // Merchant category code (Health)
            .appendQueryParameter("tr", orderId)           // Transaction reference
            .appendQueryParameter("tn", description)       // Transaction note
            .appendQueryParameter("am", amount.toString()) // Amount
            .appendQueryParameter("cu", currency)          // Currency
            .build()

        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = upiUri
        }

        _paymentState.value = PaymentState.Processing(orderId)
        return intent
    }

    /**
     * Create payment intent for specific UPI app
     */
    fun createUpiPaymentIntentForApp(
        amount: Int,
        orderId: String,
        description: String,
        upiApp: UpiApp,
        merchantVpa: String = "maahealth@razorpay"
    ): Intent {
        val intent = createUpiPaymentIntent(amount, orderId, description, merchantVpa)
        intent.setPackage(upiApp.packageName)
        return intent
    }

    /**
     * Handle UPI payment result
     */
    fun handlePaymentResult(resultCode: Int, data: Intent?): PaymentResult {
        if (resultCode == Activity.RESULT_OK && data != null) {
            val response = data.getStringExtra("response")
            return parseUpiResponse(response)
        } else if (resultCode == Activity.RESULT_CANCELED) {
            _paymentState.value = PaymentState.Cancelled
            return PaymentResult.Cancelled
        }

        _paymentState.value = PaymentState.Failed("Payment failed or was cancelled")
        return PaymentResult.Failed("Unknown error")
    }

    /**
     * Parse UPI response string
     *
     * Response format: Status=SUCCESS&txnId=123456&responseCode=00&txnRef=ORDER123
     */
    private fun parseUpiResponse(response: String?): PaymentResult {
        if (response.isNullOrEmpty()) {
            _paymentState.value = PaymentState.Failed("Empty response")
            return PaymentResult.Failed("Empty response from UPI app")
        }

        val params = response.split("&").associate {
            val (key, value) = it.split("=")
            key.lowercase() to value
        }

        val status = params["status"] ?: params["Status"] ?: ""
        val txnId = params["txnid"] ?: params["txnId"] ?: ""
        val txnRef = params["txnref"] ?: params["txnRef"] ?: ""
        val responseCode = params["responsecode"] ?: params["responseCode"] ?: ""

        return when (status.uppercase()) {
            "SUCCESS" -> {
                _paymentState.value = PaymentState.Success(txnId, txnRef)
                PaymentResult.Success(txnId, txnRef)
            }
            "FAILURE" -> {
                _paymentState.value = PaymentState.Failed("Payment failed: $responseCode")
                PaymentResult.Failed("Payment failed")
            }
            "SUBMITTED" -> {
                // Payment is pending, need to verify with backend
                _paymentState.value = PaymentState.Pending(txnId)
                PaymentResult.Pending(txnId)
            }
            else -> {
                _paymentState.value = PaymentState.Failed("Unknown status: $status")
                PaymentResult.Failed("Unknown payment status")
            }
        }
    }

    /**
     * Reset payment state
     */
    fun resetPaymentState() {
        _paymentState.value = PaymentState.Idle
    }

    /**
     * Get premium subscription options
     */
    fun getSubscriptionPlans(): List<SubscriptionPlan> {
        return listOf(
            SubscriptionPlan(
                id = "premium_monthly",
                name = "Monthly",
                nameHindi = "मासिक",
                price = PREMIUM_MONTHLY_PRICE,
                duration = "1 month",
                durationHindi = "1 महीना",
                features = listOf(
                    "Cloud backup & sync",
                    "AI health insights",
                    "Unlimited voice queries",
                    "Priority support"
                )
            ),
            SubscriptionPlan(
                id = "premium_yearly",
                name = "Yearly",
                nameHindi = "वार्षिक",
                price = PREMIUM_YEARLY_PRICE,
                originalPrice = PREMIUM_MONTHLY_PRICE * 12,
                duration = "1 year",
                durationHindi = "1 साल",
                savings = "Save ₹${(PREMIUM_MONTHLY_PRICE * 12) - PREMIUM_YEARLY_PRICE}",
                isBestValue = true,
                features = listOf(
                    "All monthly features",
                    "Family sharing (up to 5)",
                    "Export to PDF",
                    "Doctor consultation credits"
                )
            ),
            SubscriptionPlan(
                id = "premium_lifetime",
                name = "Lifetime",
                nameHindi = "जीवनकाल",
                price = PREMIUM_LIFETIME_PRICE,
                duration = "Forever",
                durationHindi = "हमेशा के लिए",
                features = listOf(
                    "All yearly features",
                    "Lifetime updates",
                    "No recurring payments",
                    "VIP support"
                )
            )
        )
    }

    private fun isPackageInstalled(packageName: String, pm: PackageManager): Boolean {
        return try {
            pm.getPackageInfo(packageName, 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }
}

/**
 * UPI App information
 */
data class UpiApp(
    val name: String,
    val packageName: String,
    val type: UpiAppType
)

enum class UpiAppType {
    GPAY,
    PHONEPE,
    PAYTM,
    BHIM,
    OTHER
}

/**
 * Payment state for UI
 */
sealed class PaymentState {
    data object Idle : PaymentState()
    data class Processing(val orderId: String) : PaymentState()
    data class Success(val txnId: String, val txnRef: String) : PaymentState()
    data class Pending(val txnId: String) : PaymentState()
    data class Failed(val message: String) : PaymentState()
    data object Cancelled : PaymentState()
}

/**
 * Payment result
 */
sealed class PaymentResult {
    data class Success(val txnId: String, val txnRef: String) : PaymentResult()
    data class Pending(val txnId: String) : PaymentResult()
    data class Failed(val message: String) : PaymentResult()
    data object Cancelled : PaymentResult()
}

/**
 * Subscription plan details
 */
data class SubscriptionPlan(
    val id: String,
    val name: String,
    val nameHindi: String,
    val price: Int,
    val originalPrice: Int? = null,
    val duration: String,
    val durationHindi: String,
    val savings: String? = null,
    val isBestValue: Boolean = false,
    val features: List<String>
)

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
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Google Pay Payment Service for Maa App
 *
 * Google Pay for Business accepts ALL UPI payment methods:
 * - Any bank account linked to UPI
 * - Works even if user has PhonePe, Paytm, or any other UPI app
 * - Accepts payments from 300+ banks in India
 * - QR code scanning from any UPI app
 *
 * Why Google Pay for Business:
 * 1. Single integration covers all UPI users
 * 2. No need to integrate multiple payment apps
 * 3. Trusted by 150M+ users in India
 * 4. Best-in-class security
 * 5. Simple merchant onboarding
 *
 * Payment Flow:
 * 1. App creates GPay payment intent with merchant VPA
 * 2. GPay opens → User enters UPI PIN
 * 3. Money transfers directly to merchant bank account
 * 4. Success callback to app
 * 5. Verify with GPay Business API (optional)
 *
 * Production Requirements:
 * ┌─────────────────────────────────────────────────────┐
 * │ 1. Indian Business Entity (Pvt Ltd / LLP)          │
 * │ 2. Current Account with Indian Bank                │
 * │ 3. Google Pay for Business Merchant Account        │
 * │ 4. Business VPA (e.g., maahealth@okaxis)           │
 * │ 5. GST Registration (for invoicing)                │
 * │ 6. PAN Card of Business                            │
 * └─────────────────────────────────────────────────────┘
 *
 * Sign up at: https://pay.google.com/business/
 */
@Singleton
class GPayPaymentService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val _paymentState = MutableStateFlow<GPayState>(GPayState.Idle)
    val paymentState: StateFlow<GPayState> = _paymentState.asStateFlow()

    companion object {
        // Google Pay package name
        const val GPAY_PACKAGE = "com.google.android.apps.nbu.paisa.user"

        // Request code for payment activity result
        const val GPAY_PAYMENT_REQUEST = 1001

        // Merchant details (Replace with actual business VPA after registration)
        // Format: businessname@bankcode (e.g., maahealth@okaxis, maahealth@ybl)
        const val MERCHANT_VPA = "maahealth@okaxis" // TODO: Replace with actual VPA
        const val MERCHANT_NAME = "Maa Health Pvt Ltd"
        const val MERCHANT_CATEGORY_CODE = "8099" // Health Services MCC

        // Premium subscription prices (in INR)
        const val PREMIUM_MONTHLY_PRICE = 99
        const val PREMIUM_YEARLY_PRICE = 799
        const val PREMIUM_LIFETIME_PRICE = 1999
    }

    /**
     * Check if Google Pay is installed
     */
    fun isGPayInstalled(): Boolean {
        return try {
            context.packageManager.getPackageInfo(GPAY_PACKAGE, 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }

    /**
     * Get Google Pay app info
     */
    fun getGPayAppInfo(): GPayAppInfo? {
        return try {
            val pm = context.packageManager
            val packageInfo = pm.getPackageInfo(GPAY_PACKAGE, 0)
            GPayAppInfo(
                packageName = GPAY_PACKAGE,
                versionName = packageInfo.versionName ?: "Unknown",
                versionCode = packageInfo.longVersionCode
            )
        } catch (e: PackageManager.NameNotFoundException) {
            null
        }
    }

    /**
     * Create Google Pay payment intent
     *
     * This creates a UPI deep link that opens Google Pay directly.
     * GPay will accept payment from ANY bank account the user has linked.
     *
     * @param amount Amount in INR (₹)
     * @param orderId Unique order/transaction ID for your records
     * @param description Payment description shown to user
     */
    fun createGPayIntent(
        amount: Int,
        orderId: String = generateOrderId(),
        description: String = "Maa Premium Subscription"
    ): Intent {
        // Build UPI payment URI
        // This is the standard UPI deep link format accepted by Google Pay
        val upiUri = Uri.Builder()
            .scheme("upi")
            .authority("pay")
            .appendQueryParameter("pa", MERCHANT_VPA)           // Payee VPA (your business)
            .appendQueryParameter("pn", MERCHANT_NAME)          // Payee name
            .appendQueryParameter("mc", MERCHANT_CATEGORY_CODE) // Merchant category code
            .appendQueryParameter("tr", orderId)                // Transaction reference ID
            .appendQueryParameter("tn", description)            // Transaction note
            .appendQueryParameter("am", amount.toString())      // Amount in INR
            .appendQueryParameter("cu", "INR")                  // Currency code
            .appendQueryParameter("mode", "02")                 // Mode: 02 for collect
            .build()

        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = upiUri
            setPackage(GPAY_PACKAGE) // Force open in Google Pay
        }

        _paymentState.value = GPayState.Processing(orderId, amount)
        return intent
    }

    /**
     * Create payment intent with plan details
     */
    fun createPaymentForPlan(plan: SubscriptionPlan): Intent {
        val orderId = generateOrderId()
        val description = "Maa Premium - ${plan.name} (${plan.duration})"
        return createGPayIntent(
            amount = plan.price,
            orderId = orderId,
            description = description
        )
    }

    /**
     * Handle payment result from GPay
     *
     * Call this in your Activity's onActivityResult()
     */
    fun handlePaymentResult(resultCode: Int, data: Intent?): GPayResult {
        when (resultCode) {
            Activity.RESULT_OK -> {
                val response = data?.getStringExtra("response")
                return parseGPayResponse(response)
            }
            Activity.RESULT_CANCELED -> {
                _paymentState.value = GPayState.Cancelled
                return GPayResult.Cancelled
            }
            else -> {
                _paymentState.value = GPayState.Failed("Payment was not completed")
                return GPayResult.Failed("Unknown result code: $resultCode")
            }
        }
    }

    /**
     * Parse Google Pay response string
     *
     * Response format varies but typically includes:
     * - Status: SUCCESS, FAILURE, SUBMITTED
     * - txnId: UPI transaction ID
     * - responseCode: Bank response code
     * - txnRef: Your transaction reference
     */
    private fun parseGPayResponse(response: String?): GPayResult {
        if (response.isNullOrEmpty()) {
            _paymentState.value = GPayState.Failed("No response received from Google Pay")
            return GPayResult.Failed("Empty response")
        }

        // Parse key=value pairs
        val params = try {
            response.split("&").associate { pair ->
                val parts = pair.split("=", limit = 2)
                if (parts.size == 2) {
                    parts[0].lowercase() to parts[1]
                } else {
                    parts[0].lowercase() to ""
                }
            }
        } catch (e: Exception) {
            _paymentState.value = GPayState.Failed("Could not parse response")
            return GPayResult.Failed("Parse error: ${e.message}")
        }

        val status = params["status"] ?: ""
        val txnId = params["txnid"] ?: params["txnId"] ?: ""
        val txnRef = params["txnref"] ?: params["tr"] ?: ""
        val responseCode = params["responsecode"] ?: ""
        val approvalRefNo = params["approvalrefno"] ?: ""

        return when (status.uppercase()) {
            "SUCCESS" -> {
                val successState = GPayState.Success(
                    txnId = txnId,
                    txnRef = txnRef,
                    approvalRefNo = approvalRefNo
                )
                _paymentState.value = successState
                GPayResult.Success(txnId, txnRef, approvalRefNo)
            }

            "FAILURE" -> {
                val message = getFailureMessage(responseCode)
                _paymentState.value = GPayState.Failed(message)
                GPayResult.Failed(message)
            }

            "SUBMITTED" -> {
                // Payment initiated but not yet confirmed
                // This can happen with some banks - need to verify later
                _paymentState.value = GPayState.Pending(txnId)
                GPayResult.Pending(txnId)
            }

            else -> {
                _paymentState.value = GPayState.Failed("Unknown status: $status")
                GPayResult.Failed("Payment status unknown")
            }
        }
    }

    /**
     * Get human-readable failure message from response code
     */
    private fun getFailureMessage(responseCode: String): String {
        return when (responseCode.uppercase()) {
            "U30" -> "Insufficient balance"
            "U31" -> "Daily limit exceeded"
            "U28" -> "Unable to connect to bank"
            "U09" -> "Request time out"
            "U16" -> "Risk threshold exceeded"
            "U67" -> "Debit freeze on account"
            "U17" -> "Transaction not permitted"
            "ZM" -> "Invalid UPI PIN"
            "Z9" -> "Insufficient funds"
            else -> "Payment failed (Code: $responseCode)"
        }
    }

    /**
     * Generate unique order ID
     */
    private fun generateOrderId(): String {
        val timestamp = System.currentTimeMillis()
        val uuid = UUID.randomUUID().toString().take(8).uppercase()
        return "MAA${timestamp}${uuid}"
    }

    /**
     * Reset payment state
     */
    fun resetState() {
        _paymentState.value = GPayState.Idle
    }

    /**
     * Get subscription plans
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

    /**
     * Open Google Pay Play Store page if not installed
     */
    fun getGPayPlayStoreIntent(): Intent {
        return Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse("market://details?id=$GPAY_PACKAGE")
            setPackage("com.android.vending")
        }
    }
}

/**
 * Google Pay app info
 */
data class GPayAppInfo(
    val packageName: String,
    val versionName: String,
    val versionCode: Long
)

/**
 * Google Pay payment states
 */
sealed class GPayState {
    data object Idle : GPayState()
    data class Processing(val orderId: String, val amount: Int) : GPayState()
    data class Success(
        val txnId: String,
        val txnRef: String,
        val approvalRefNo: String = ""
    ) : GPayState()
    data class Pending(val txnId: String) : GPayState()
    data class Failed(val message: String) : GPayState()
    data object Cancelled : GPayState()
}

/**
 * Google Pay payment result
 */
sealed class GPayResult {
    data class Success(
        val txnId: String,
        val txnRef: String,
        val approvalRefNo: String = ""
    ) : GPayResult()
    data class Pending(val txnId: String) : GPayResult()
    data class Failed(val message: String) : GPayResult()
    data object Cancelled : GPayResult()
}

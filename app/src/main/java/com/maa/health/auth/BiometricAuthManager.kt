package com.maa.health.auth

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Biometric Authentication Manager
 *
 * Handles Face ID and Fingerprint authentication as the primary login method.
 * Falls back to OTP if biometric is not available or fails.
 */
@Singleton
class BiometricAuthManager @Inject constructor() {

    private val _authResult = Channel<BiometricAuthResult>()
    val authResult = _authResult.receiveAsFlow()

    /**
     * Check if biometric authentication is available on this device
     */
    fun getBiometricCapability(context: Context): BiometricCapability {
        val biometricManager = BiometricManager.from(context)

        return when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
            BiometricManager.BIOMETRIC_SUCCESS -> BiometricCapability.AVAILABLE
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> BiometricCapability.NO_HARDWARE
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> BiometricCapability.HARDWARE_UNAVAILABLE
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> BiometricCapability.NOT_ENROLLED
            BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED -> BiometricCapability.SECURITY_UPDATE_REQUIRED
            else -> BiometricCapability.UNKNOWN
        }
    }

    /**
     * Check if biometric is available (quick check)
     */
    fun isBiometricAvailable(context: Context): Boolean {
        return getBiometricCapability(context) == BiometricCapability.AVAILABLE
    }

    /**
     * Authenticate using biometric (Face ID or Fingerprint)
     *
     * @param activity The FragmentActivity to show the prompt on
     * @param title Title of the biometric prompt
     * @param subtitle Subtitle of the biometric prompt
     * @param negativeButtonText Text for the cancel/fallback button
     * @param onResult Callback with the authentication result
     */
    fun authenticate(
        activity: FragmentActivity,
        title: String = "Sign in with Face ID",
        subtitle: String = "Use your face or fingerprint to sign in",
        negativeButtonText: String = "Use OTP instead",
        onResult: (BiometricAuthResult) -> Unit
    ) {
        val executor = ContextCompat.getMainExecutor(activity)

        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                val result = when (errorCode) {
                    BiometricPrompt.ERROR_NEGATIVE_BUTTON -> BiometricAuthResult.FallbackRequested
                    BiometricPrompt.ERROR_USER_CANCELED -> BiometricAuthResult.Cancelled
                    BiometricPrompt.ERROR_LOCKOUT -> BiometricAuthResult.LockedOut
                    BiometricPrompt.ERROR_LOCKOUT_PERMANENT -> BiometricAuthResult.PermanentlyLockedOut
                    else -> BiometricAuthResult.Error(errString.toString())
                }
                onResult(result)
            }

            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                onResult(BiometricAuthResult.Success)
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                // This is called when the biometric doesn't match
                // We don't treat this as a final result - the user can try again
            }
        }

        val biometricPrompt = BiometricPrompt(activity, executor, callback)

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setNegativeButtonText(negativeButtonText)
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
            .build()

        biometricPrompt.authenticate(promptInfo)
    }

    /**
     * Get user-friendly message for biometric capability
     */
    fun getCapabilityMessage(capability: BiometricCapability): String {
        return when (capability) {
            BiometricCapability.AVAILABLE -> "Face ID or Fingerprint is ready to use"
            BiometricCapability.NO_HARDWARE -> "This device doesn't support biometric authentication"
            BiometricCapability.HARDWARE_UNAVAILABLE -> "Biometric hardware is currently unavailable"
            BiometricCapability.NOT_ENROLLED -> "No face or fingerprint is enrolled. Please set up in device settings."
            BiometricCapability.SECURITY_UPDATE_REQUIRED -> "A security update is required to use biometrics"
            BiometricCapability.UNKNOWN -> "Unable to determine biometric status"
        }
    }
}

/**
 * Biometric capability status
 */
enum class BiometricCapability {
    AVAILABLE,              // Biometric is available and ready
    NO_HARDWARE,            // Device doesn't have biometric hardware
    HARDWARE_UNAVAILABLE,   // Hardware exists but is currently unavailable
    NOT_ENROLLED,           // No biometric credentials enrolled
    SECURITY_UPDATE_REQUIRED, // Security update needed
    UNKNOWN                 // Unknown state
}

/**
 * Result of biometric authentication attempt
 */
sealed class BiometricAuthResult {
    object Success : BiometricAuthResult()
    object Cancelled : BiometricAuthResult()
    object FallbackRequested : BiometricAuthResult()  // User clicked "Use OTP instead"
    object LockedOut : BiometricAuthResult()          // Too many failed attempts
    object PermanentlyLockedOut : BiometricAuthResult()
    data class Error(val message: String) : BiometricAuthResult()
}

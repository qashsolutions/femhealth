package com.maa.health.ui.screens.onboarding

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import com.maa.health.auth.BiometricAuthManager
import com.maa.health.auth.BiometricAuthResult
import com.maa.health.auth.BiometricCapability
import com.maa.health.ui.components.MaaButton
import com.maa.health.ui.components.MaaButtonSize
import com.maa.health.ui.components.MaaButtonVariant
import com.maa.health.ui.theme.MaaColors
import com.maa.health.ui.theme.MaaSpacing
import com.maa.health.ui.theme.MaaTypography

/**
 * Authentication Screen
 *
 * Shows biometric authentication as primary method with OTP as fallback.
 * For returning users, directly offers biometric login.
 * For new users, guides them to set up authentication.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    biometricAuthManager: BiometricAuthManager,
    isReturningUser: Boolean = false,
    onBiometricSuccess: () -> Unit,
    onUsePhone: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var biometricCapability by remember { mutableStateOf(BiometricCapability.UNKNOWN) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Check biometric capability on launch
    LaunchedEffect(Unit) {
        biometricCapability = biometricAuthManager.getBiometricCapability(context)
    }

    val isBiometricAvailable = biometricCapability == BiometricCapability.AVAILABLE

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaaColors.background)
    ) {
        TopAppBar(
            title = {},
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaaColors.textPrimary
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaaColors.background
            )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = MaaSpacing.large),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(MaaSpacing.extraLarge))

            // Face/Fingerprint Icon
            Icon(
                imageVector = if (isBiometricAvailable) Icons.Default.Face else Icons.Default.Fingerprint,
                contentDescription = "Biometric",
                modifier = Modifier.size(80.dp),
                tint = MaaColors.accent
            )

            Spacer(modifier = Modifier.height(MaaSpacing.large))

            // Header
            Text(
                text = if (isReturningUser) "Welcome Back" else "Quick Sign In",
                style = MaaTypography.headlineMedium,
                color = MaaColors.textPrimary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(MaaSpacing.small))

            Text(
                text = if (isBiometricAvailable) {
                    "Use your face or fingerprint for quick, secure access"
                } else {
                    biometricAuthManager.getCapabilityMessage(biometricCapability)
                },
                style = MaaTypography.bodyMedium,
                color = MaaColors.textSecondary,
                textAlign = TextAlign.Center
            )

            // Error message
            errorMessage?.let { error ->
                Spacer(modifier = Modifier.height(MaaSpacing.medium))
                Text(
                    text = error,
                    style = MaaTypography.bodySmall,
                    color = MaaColors.danger,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Biometric Button (Primary)
            if (isBiometricAvailable) {
                MaaButton(
                    text = "Sign in with Face ID",
                    onClick = {
                        errorMessage = null
                        val activity = context as? FragmentActivity
                        if (activity != null) {
                            biometricAuthManager.authenticate(
                                activity = activity,
                                onResult = { result ->
                                    when (result) {
                                        is BiometricAuthResult.Success -> onBiometricSuccess()
                                        is BiometricAuthResult.FallbackRequested -> onUsePhone()
                                        is BiometricAuthResult.Cancelled -> {
                                            // User cancelled, do nothing
                                        }
                                        is BiometricAuthResult.LockedOut -> {
                                            errorMessage = "Too many attempts. Please try again later or use OTP."
                                        }
                                        is BiometricAuthResult.PermanentlyLockedOut -> {
                                            errorMessage = "Biometric locked. Please use OTP to sign in."
                                        }
                                        is BiometricAuthResult.Error -> {
                                            errorMessage = result.message
                                        }
                                    }
                                }
                            )
                        }
                    },
                    leadingIcon = Icons.Default.Face,
                    fullWidth = true,
                    size = MaaButtonSize.LARGE
                )

                Spacer(modifier = Modifier.height(MaaSpacing.medium))

                // Divider with "or"
                Text(
                    text = "or",
                    style = MaaTypography.bodyMedium,
                    color = MaaColors.textTertiary
                )

                Spacer(modifier = Modifier.height(MaaSpacing.medium))
            }

            // Phone OTP Button (Secondary or Primary if no biometric)
            MaaButton(
                text = if (isBiometricAvailable) "Use Phone Number Instead" else "Sign in with Phone Number",
                onClick = onUsePhone,
                leadingIcon = Icons.Default.Phone,
                variant = if (isBiometricAvailable) MaaButtonVariant.SECONDARY else MaaButtonVariant.PRIMARY,
                fullWidth = true,
                size = MaaButtonSize.LARGE
            )

            Spacer(modifier = Modifier.height(MaaSpacing.medium))

            // Privacy note
            Text(
                text = "Your biometric data never leaves your device and is not stored by the app.",
                style = MaaTypography.bodySmall,
                color = MaaColors.textTertiary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(MaaSpacing.large))
        }
    }
}

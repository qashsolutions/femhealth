package com.maa.health.ui.screens.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.maa.health.ui.components.MaaButton
import com.maa.health.ui.components.MaaButtonSize
import com.maa.health.ui.components.MaaButtonVariant
import com.maa.health.ui.theme.MaaColors
import com.maa.health.ui.theme.MaaShapes
import com.maa.health.ui.theme.MaaSpacing
import com.maa.health.ui.theme.MaaTypography

/**
 * OTP Verification screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OtpVerificationScreen(
    phoneNumber: String,
    onVerified: () -> Unit,
    onBack: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(uiState.otpVerified) {
        if (uiState.otpVerified) {
            onVerified()
        }
    }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

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
            Spacer(modifier = Modifier.height(MaaSpacing.large))

            // Header
            Text(
                text = "Verify Your Number",
                style = MaaTypography.headlineMedium,
                color = MaaColors.textPrimary
            )

            Spacer(modifier = Modifier.height(MaaSpacing.small))

            Text(
                text = "Enter the 6-digit code sent to",
                style = MaaTypography.bodyMedium,
                color = MaaColors.textSecondary,
                textAlign = TextAlign.Center
            )

            Text(
                text = phoneNumber,
                style = MaaTypography.titleSmall,
                color = MaaColors.textPrimary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(MaaSpacing.extraLarge))

            // OTP Input
            OtpInputField(
                otpValue = uiState.otp,
                onOtpChange = { viewModel.setOtp(it) },
                hasError = uiState.otpError != null,
                modifier = Modifier.focusRequester(focusRequester)
            )

            // Error message
            if (uiState.otpError != null) {
                Spacer(modifier = Modifier.height(MaaSpacing.small))
                Text(
                    text = uiState.otpError!!,
                    style = MaaTypography.labelSmall,
                    color = MaaColors.danger
                )
            }

            Spacer(modifier = Modifier.height(MaaSpacing.extraLarge))

            // Verify button
            MaaButton(
                text = "Verify",
                onClick = { viewModel.verifyOtp() },
                enabled = uiState.otp.length == 6 && !uiState.isLoading,
                loading = uiState.isLoading,
                fullWidth = true,
                size = MaaButtonSize.LARGE
            )

            Spacer(modifier = Modifier.height(MaaSpacing.large))

            // Resend option
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Didn't receive the code?",
                    style = MaaTypography.bodySmall,
                    color = MaaColors.textSecondary
                )
                Spacer(modifier = Modifier.width(MaaSpacing.small))
                Text(
                    text = "Resend",
                    style = MaaTypography.labelMedium,
                    color = MaaColors.accent,
                    modifier = Modifier.clickable { viewModel.resendOtp() }
                )
            }

            if (uiState.otpResent) {
                Spacer(modifier = Modifier.height(MaaSpacing.small))
                Text(
                    text = "OTP sent again!",
                    style = MaaTypography.labelSmall,
                    color = MaaColors.safe
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Skip for testing
            MaaButton(
                text = "Skip verification (testing)",
                onClick = { viewModel.skipAuth() },
                variant = MaaButtonVariant.TERTIARY,
                fullWidth = true
            )

            Spacer(modifier = Modifier.height(MaaSpacing.large))
        }
    }
}

@Composable
private fun OtpInputField(
    otpValue: String,
    onOtpChange: (String) -> Unit,
    hasError: Boolean,
    modifier: Modifier = Modifier,
    otpLength: Int = 6
) {
    BasicTextField(
        value = otpValue,
        onValueChange = { newValue ->
            if (newValue.length <= otpLength && newValue.all { it.isDigit() }) {
                onOtpChange(newValue)
            }
        },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
        modifier = modifier,
        decorationBox = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(MaaSpacing.small),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(otpLength) { index ->
                    val char = otpValue.getOrNull(index)?.toString() ?: ""
                    val isFilled = char.isNotEmpty()
                    val isCurrent = index == otpValue.length

                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(MaaShapes.small)
                            .background(MaaColors.surfaceVariant)
                            .border(
                                width = if (isCurrent) 2.dp else 1.dp,
                                color = when {
                                    hasError -> MaaColors.danger
                                    isCurrent -> MaaColors.accent
                                    isFilled -> MaaColors.accent.copy(alpha = 0.5f)
                                    else -> MaaColors.border
                                },
                                shape = MaaShapes.small
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = char,
                            style = MaaTypography.headlineSmall,
                            color = MaaColors.textPrimary
                        )
                    }
                }
            }
        }
    )
}

package com.maa.health.ui.screens.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.maa.health.ui.components.MaaButton
import com.maa.health.ui.components.MaaButtonSize
import com.maa.health.ui.components.MaaButtonVariant
import com.maa.health.ui.components.MaaPhoneField
import com.maa.health.ui.theme.MaaColors
import com.maa.health.ui.theme.MaaSpacing
import com.maa.health.ui.theme.MaaTypography

/**
 * Phone authentication screen
 *
 * Collects phone number for Firebase Auth OTP
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhoneAuthScreen(
    onOtpSent: (String) -> Unit,
    onBack: () -> Unit
) {
    var phoneNumber by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorText by remember { mutableStateOf<String?>(null) }

    val isValidPhone = phoneNumber.length == 10

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
                .padding(horizontal = MaaSpacing.large)
        ) {
            Spacer(modifier = Modifier.height(MaaSpacing.large))

            // Header
            Text(
                text = "Enter Your Phone Number",
                style = MaaTypography.headlineMedium,
                color = MaaColors.textPrimary
            )

            Spacer(modifier = Modifier.height(MaaSpacing.small))

            Text(
                text = "We'll send you a verification code to confirm your number",
                style = MaaTypography.bodyMedium,
                color = MaaColors.textSecondary
            )

            Spacer(modifier = Modifier.height(MaaSpacing.extraLarge))

            // Phone input
            MaaPhoneField(
                value = phoneNumber,
                onValueChange = {
                    phoneNumber = it
                    errorText = null
                },
                label = "Phone Number",
                placeholder = "10-digit mobile number",
                errorText = errorText
            )

            Spacer(modifier = Modifier.height(MaaSpacing.medium))

            // Privacy note
            Text(
                text = "Your phone number is used only for verification and will be kept private.",
                style = MaaTypography.bodySmall,
                color = MaaColors.textTertiary
            )

            Spacer(modifier = Modifier.weight(1f))

            // Send OTP button
            MaaButton(
                text = "Send Verification Code",
                onClick = {
                    if (isValidPhone) {
                        isLoading = true
                        // TODO: Implement Firebase phone auth
                        onOtpSent("+91$phoneNumber")
                    } else {
                        errorText = "Please enter a valid 10-digit phone number"
                    }
                },
                enabled = isValidPhone && !isLoading,
                loading = isLoading,
                fullWidth = true,
                size = MaaButtonSize.LARGE
            )

            Spacer(modifier = Modifier.height(MaaSpacing.medium))

            // Skip option (for testing)
            MaaButton(
                text = "Skip for now",
                onClick = { onOtpSent("skip") },
                variant = MaaButtonVariant.TERTIARY,
                fullWidth = true
            )

            Spacer(modifier = Modifier.height(MaaSpacing.large))
        }
    }
}

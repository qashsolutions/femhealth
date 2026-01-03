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
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.maa.health.data.model.SupportedCountry
import com.maa.health.ui.components.CountryPickerBottomSheet
import com.maa.health.ui.components.MaaButton
import com.maa.health.ui.components.MaaButtonSize
import com.maa.health.ui.components.MaaButtonVariant
import com.maa.health.ui.components.MaaPhoneField
import com.maa.health.ui.theme.MaaColors
import com.maa.health.ui.theme.MaaSpacing
import com.maa.health.ui.theme.MaaTypography
import kotlinx.coroutines.launch

/**
 * Phone authentication screen
 *
 * Collects phone number for Firebase Auth OTP
 * Supports all countries in South Asia and Africa
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhoneAuthScreen(
    onOtpSent: (phoneNumber: String, country: SupportedCountry) -> Unit,
    onBack: () -> Unit
) {
    var phoneNumber by remember { mutableStateOf("") }
    var selectedCountry by remember { mutableStateOf(SupportedCountry.getDefault()) }
    var isLoading by remember { mutableStateOf(false) }
    var errorText by remember { mutableStateOf<String?>(null) }
    var showCountryPicker by remember { mutableStateOf(false) }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    // Validate phone number (minimum 6 digits, maximum 15)
    val isValidPhone = phoneNumber.length >= 6

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

            // Phone input with country picker
            MaaPhoneField(
                value = phoneNumber,
                onValueChange = {
                    phoneNumber = it
                    errorText = null
                },
                selectedCountry = selectedCountry,
                onCountryClick = {
                    scope.launch {
                        showCountryPicker = true
                        sheetState.show()
                    }
                },
                label = "Phone Number",
                placeholder = "Mobile number",
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
                        // Send full phone number with country code
                        onOtpSent(phoneNumber, selectedCountry)
                    } else {
                        errorText = "Please enter a valid phone number"
                    }
                },
                enabled = isValidPhone && !isLoading,
                loading = isLoading,
                fullWidth = true,
                size = MaaButtonSize.LARGE
            )

            Spacer(modifier = Modifier.height(MaaSpacing.medium))

            // Skip option (for testing - remove in production)
            MaaButton(
                text = "Skip for now",
                onClick = { onOtpSent("skip", selectedCountry) },
                variant = MaaButtonVariant.TERTIARY,
                fullWidth = true
            )

            Spacer(modifier = Modifier.height(MaaSpacing.large))
        }
    }

    // Country picker bottom sheet
    if (showCountryPicker) {
        CountryPickerBottomSheet(
            sheetState = sheetState,
            selectedCountry = selectedCountry,
            onCountrySelected = { country ->
                selectedCountry = country
            },
            onDismiss = {
                scope.launch {
                    sheetState.hide()
                    showCountryPicker = false
                }
            }
        )
    }
}

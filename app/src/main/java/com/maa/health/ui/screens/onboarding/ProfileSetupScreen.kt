package com.maa.health.ui.screens.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import com.maa.health.ui.components.MaaButton
import com.maa.health.ui.components.MaaButtonSize
import com.maa.health.ui.components.MaaTextField
import com.maa.health.ui.theme.MaaColors
import com.maa.health.ui.theme.MaaSpacing
import com.maa.health.ui.theme.MaaTypography

/**
 * Profile setup screen
 *
 * Collects basic user information:
 * - Name (optional)
 * - Age/Date of birth
 * - Location (for facility finder)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileSetupScreen(
    onProfileComplete: () -> Unit,
    onBack: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var pincode by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    // Validation
    val ageInt = age.toIntOrNull()
    val isAgeValid = ageInt != null && ageInt in 10..100
    val isPincodeValid = pincode.isEmpty() || pincode.length == 6

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
                .verticalScroll(scrollState)
                .padding(horizontal = MaaSpacing.large)
        ) {
            Spacer(modifier = Modifier.height(MaaSpacing.medium))

            // Header
            Text(
                text = "Tell Us About Yourself",
                style = MaaTypography.headlineMedium,
                color = MaaColors.textPrimary
            )

            Spacer(modifier = Modifier.height(MaaSpacing.small))

            Text(
                text = "This helps us personalize your health guidance",
                style = MaaTypography.bodyMedium,
                color = MaaColors.textSecondary
            )

            Spacer(modifier = Modifier.height(MaaSpacing.extraLarge))

            // Name field (optional)
            MaaTextField(
                value = name,
                onValueChange = { name = it },
                label = "Name (optional)",
                placeholder = "How should we address you?",
                helperText = "You can skip this if you prefer privacy",
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Next
                )
            )

            Spacer(modifier = Modifier.height(MaaSpacing.large))

            // Age field
            MaaTextField(
                value = age,
                onValueChange = { newValue ->
                    if (newValue.isEmpty() || (newValue.all { it.isDigit() } && newValue.length <= 3)) {
                        age = newValue
                    }
                },
                label = "Your Age",
                placeholder = "Enter your age",
                helperText = "This helps us show relevant health content for your life stage",
                errorText = if (age.isNotEmpty() && !isAgeValid) "Please enter a valid age (10-100)" else null,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next
                )
            )

            Spacer(modifier = Modifier.height(MaaSpacing.large))

            // Pincode field (optional)
            MaaTextField(
                value = pincode,
                onValueChange = { newValue ->
                    if (newValue.isEmpty() || (newValue.all { it.isDigit() } && newValue.length <= 6)) {
                        pincode = newValue
                    }
                },
                label = "Pincode (optional)",
                placeholder = "Your area pincode",
                helperText = "Used to find nearby health facilities",
                errorText = if (pincode.isNotEmpty() && !isPincodeValid) "Pincode must be 6 digits" else null,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done
                )
            )

            Spacer(modifier = Modifier.height(MaaSpacing.extraLarge))

            // Privacy note
            Text(
                text = "Your information is stored securely on your device and never shared without your consent.",
                style = MaaTypography.bodySmall,
                color = MaaColors.textTertiary
            )

            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.height(MaaSpacing.large))

            // Continue button
            MaaButton(
                text = "Continue",
                onClick = {
                    isLoading = true
                    // TODO: Save profile to DataStore
                    onProfileComplete()
                },
                enabled = isAgeValid && isPincodeValid && !isLoading,
                loading = isLoading,
                fullWidth = true,
                size = MaaButtonSize.LARGE
            )

            Spacer(modifier = Modifier.height(MaaSpacing.large))
        }
    }
}

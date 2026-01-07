package com.maa.health.ui.screens.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import com.maa.health.ui.components.MaaButtonVariant
import com.maa.health.ui.components.MaaTextField
import com.maa.health.ui.theme.MaaColors
import com.maa.health.ui.theme.MaaSpacing
import com.maa.health.ui.theme.MaaTypography

/**
 * Simplified Profile setup screen
 *
 * Minimal friction signup - all fields are optional:
 * - Name (optional) - for personalization
 * - Age (optional) - for relevant health content
 *
 * Users can complete this later in settings.
 * App is free, so we minimize signup friction.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileSetupScreen(
    onProfileComplete: () -> Unit,
    onBack: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    // Validation - age is optional but must be valid if provided
    val ageInt = age.toIntOrNull()
    val isAgeValid = age.isEmpty() || (ageInt != null && ageInt in 10..100)

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
                text = "Almost There!",
                style = MaaTypography.headlineMedium,
                color = MaaColors.textPrimary
            )

            Spacer(modifier = Modifier.height(MaaSpacing.small))

            Text(
                text = "Just a couple of optional details to personalize your experience",
                style = MaaTypography.bodyMedium,
                color = MaaColors.textSecondary
            )

            Spacer(modifier = Modifier.height(MaaSpacing.extraLarge))

            // Name field (optional)
            MaaTextField(
                value = name,
                onValueChange = { name = it },
                label = "Your Name (optional)",
                placeholder = "How should we address you?",
                helperText = "Used for personalized greetings",
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Next
                )
            )

            Spacer(modifier = Modifier.height(MaaSpacing.large))

            // Age field (optional)
            MaaTextField(
                value = age,
                onValueChange = { newValue ->
                    if (newValue.isEmpty() || (newValue.all { it.isDigit() } && newValue.length <= 3)) {
                        age = newValue
                    }
                },
                label = "Your Age (optional)",
                placeholder = "Enter your age",
                helperText = "Helps show relevant health content for your life stage",
                errorText = if (age.isNotEmpty() && !isAgeValid) "Please enter a valid age (10-100)" else null,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done
                )
            )

            Spacer(modifier = Modifier.height(MaaSpacing.extraLarge))

            // Privacy note
            Text(
                text = "Your information is stored securely on your device. You can update this anytime in Settings.",
                style = MaaTypography.bodySmall,
                color = MaaColors.textTertiary
            )

            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.height(MaaSpacing.large))

            // Continue button
            MaaButton(
                text = if (name.isNotEmpty() || age.isNotEmpty()) "Continue" else "Skip for Now",
                onClick = {
                    isLoading = true
                    // TODO: Save profile to DataStore
                    onProfileComplete()
                },
                enabled = isAgeValid && !isLoading,
                loading = isLoading,
                fullWidth = true,
                size = MaaButtonSize.LARGE,
                variant = if (name.isNotEmpty() || age.isNotEmpty()) MaaButtonVariant.PRIMARY else MaaButtonVariant.SECONDARY
            )

            Spacer(modifier = Modifier.height(MaaSpacing.large))
        }
    }
}

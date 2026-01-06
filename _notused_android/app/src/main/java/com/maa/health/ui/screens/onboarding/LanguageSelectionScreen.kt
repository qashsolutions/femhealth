package com.maa.health.ui.screens.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.maa.health.data.model.SupportedLanguage
import com.maa.health.ui.components.MaaButton
import com.maa.health.ui.components.MaaButtonSize
import com.maa.health.ui.theme.MaaColors
import com.maa.health.ui.theme.MaaShapes
import com.maa.health.ui.theme.MaaSpacing
import com.maa.health.ui.theme.MaaTypography

/**
 * Language selection screen
 *
 * First screen in onboarding flow
 * Supports 10 Indian languages via Sarvam AI
 */
@Composable
fun LanguageSelectionScreen(
    onLanguageSelected: () -> Unit
) {
    var selectedLanguage by remember { mutableStateOf<SupportedLanguage?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaaColors.background)
            .padding(MaaSpacing.large)
    ) {
        Spacer(modifier = Modifier.height(MaaSpacing.extraLarge))

        // Header
        Text(
            text = "Choose Your Language",
            style = MaaTypography.headlineMedium,
            color = MaaColors.textPrimary
        )

        Spacer(modifier = Modifier.height(MaaSpacing.small))

        Text(
            text = "Select the language you're most comfortable with",
            style = MaaTypography.bodyMedium,
            color = MaaColors.textSecondary
        )

        Spacer(modifier = Modifier.height(MaaSpacing.extraLarge))

        // Language grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(vertical = MaaSpacing.small),
            horizontalArrangement = Arrangement.spacedBy(MaaSpacing.medium),
            verticalArrangement = Arrangement.spacedBy(MaaSpacing.medium),
            modifier = Modifier.weight(1f)
        ) {
            items(SupportedLanguage.entries.toTypedArray()) { language ->
                LanguageCard(
                    language = language,
                    isSelected = selectedLanguage == language,
                    onClick = { selectedLanguage = language }
                )
            }
        }

        Spacer(modifier = Modifier.height(MaaSpacing.large))

        // Continue button
        MaaButton(
            text = "Continue",
            onClick = onLanguageSelected,
            enabled = selectedLanguage != null,
            fullWidth = true,
            size = MaaButtonSize.LARGE
        )

        Spacer(modifier = Modifier.height(MaaSpacing.medium))
    }
}

@Composable
private fun LanguageCard(
    language: SupportedLanguage,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaaShapes.medium)
            .background(if (isSelected) MaaColors.accentLight.copy(alpha = 0.3f) else MaaColors.surface)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) MaaColors.accent else MaaColors.border,
                shape = MaaShapes.medium
            )
            .clickable(onClick = onClick)
            .padding(MaaSpacing.medium)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Native script name
            Text(
                text = language.nativeName,
                style = MaaTypography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaaColors.textPrimary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(MaaSpacing.extraSmall))

            // English name
            Text(
                text = language.displayName,
                style = MaaTypography.bodySmall,
                color = MaaColors.textSecondary,
                textAlign = TextAlign.Center
            )
        }

        // Selection indicator
        if (isSelected) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(20.dp)
                    .clip(MaaShapes.extraSmall)
                    .background(MaaColors.accent),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Selected",
                    modifier = Modifier.size(14.dp),
                    tint = androidx.compose.ui.graphics.Color.White
                )
            }
        }
    }
}

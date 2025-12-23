package com.maa.health.ui.screens.onboarding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.maa.health.data.model.SupportedLanguage
import com.maa.health.service.VoiceService
import com.maa.health.service.VoiceState
import com.maa.health.ui.components.MaaButton
import com.maa.health.ui.components.MaaButtonSize
import com.maa.health.ui.components.MaaButtonStyle
import com.maa.health.ui.theme.MaaColors
import com.maa.health.ui.theme.MaaShapes
import com.maa.health.ui.theme.MaaSpacing
import com.maa.health.ui.theme.MaaTypography

/**
 * Language selection screen
 *
 * First screen in onboarding flow
 * Supports 22 Indian languages via Sarvam AI
 * Voice-first UX with location-based language detection
 */
@Composable
fun LanguageSelectionScreen(
    onLanguageSelected: (SupportedLanguage) -> Unit,
    voiceService: VoiceService? = null,
    detectedLanguage: SupportedLanguage? = null,
    detectedLocation: String? = null
) {
    var selectedLanguage by remember { mutableStateOf<SupportedLanguage?>(detectedLanguage) }
    var showVoiceGreeting by remember { mutableStateOf(detectedLanguage != null && voiceService != null) }
    val voiceState = voiceService?.voiceState?.collectAsState()

    // Auto-play voice greeting when language is detected
    LaunchedEffect(detectedLanguage, voiceService) {
        if (detectedLanguage != null && voiceService != null && detectedLanguage.hasVoice) {
            voiceService.speakGreeting(detectedLanguage)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaaColors.background)
            .padding(MaaSpacing.large)
    ) {
        Spacer(modifier = Modifier.height(MaaSpacing.extraLarge))

        // Header with voice greeting card if language detected
        AnimatedVisibility(
            visible = showVoiceGreeting && detectedLanguage != null,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            VoiceGreetingCard(
                language = detectedLanguage!!,
                location = detectedLocation,
                onConfirm = {
                    selectedLanguage = detectedLanguage
                    showVoiceGreeting = false
                    onLanguageSelected(detectedLanguage)
                },
                onChooseDifferent = {
                    showVoiceGreeting = false
                }
            )
        }

        AnimatedVisibility(
            visible = !showVoiceGreeting,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Column {
                // Header
                Text(
                    text = "Choose Your Language",
                    style = MaaTypography.headlineMedium,
                    color = MaaColors.textPrimary
                )

                Spacer(modifier = Modifier.height(MaaSpacing.small))

                Text(
                    text = "अपनी भाषा चुनें",
                    style = MaaTypography.bodyMedium,
                    color = MaaColors.textSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(MaaSpacing.large))

        // Voice-supported languages section
        if (!showVoiceGreeting) {
            Text(
                text = "VOICE SUPPORTED",
                style = MaaTypography.labelSmall,
                color = MaaColors.textTertiary
            )

            Spacer(modifier = Modifier.height(MaaSpacing.small))
        }

        // Language grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            contentPadding = PaddingValues(vertical = MaaSpacing.small),
            horizontalArrangement = Arrangement.spacedBy(MaaSpacing.small),
            verticalArrangement = Arrangement.spacedBy(MaaSpacing.small),
            modifier = Modifier.weight(1f)
        ) {
            // Voice-supported languages first
            items(SupportedLanguage.getVoiceLanguages()) { language ->
                LanguageCard(
                    language = language,
                    isSelected = selectedLanguage == language,
                    hasVoice = true,
                    onClick = { selectedLanguage = language }
                )
            }
            // Translation-only languages
            items(SupportedLanguage.entries.filter { !it.hasVoice }) { language ->
                LanguageCard(
                    language = language,
                    isSelected = selectedLanguage == language,
                    hasVoice = false,
                    onClick = { selectedLanguage = language }
                )
            }
        }

        Spacer(modifier = Modifier.height(MaaSpacing.large))

        // Continue button
        MaaButton(
            text = "Continue",
            onClick = { selectedLanguage?.let { onLanguageSelected(it) } },
            enabled = selectedLanguage != null,
            fullWidth = true,
            size = MaaButtonSize.LARGE
        )

        Spacer(modifier = Modifier.height(MaaSpacing.medium))
    }
}

/**
 * Voice greeting card for language confirmation
 */
@Composable
private fun VoiceGreetingCard(
    language: SupportedLanguage,
    location: String?,
    onConfirm: () -> Unit,
    onChooseDifferent: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaaColors.surface),
        shape = MaaShapes.large
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(MaaSpacing.large),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Voice indicator
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(MaaColors.accent.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.VolumeUp,
                    contentDescription = "Speaking",
                    tint = MaaColors.accent,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.height(MaaSpacing.medium))

            // Location badge
            if (location != null) {
                Box(
                    modifier = Modifier
                        .clip(MaaShapes.small)
                        .background(MaaColors.safe.copy(alpha = 0.15f))
                        .padding(horizontal = MaaSpacing.small, vertical = MaaSpacing.extraSmall)
                ) {
                    Text(
                        text = location,
                        style = MaaTypography.labelSmall,
                        color = MaaColors.safe
                    )
                }

                Spacer(modifier = Modifier.height(MaaSpacing.small))
            }

            // Greeting in detected language
            Text(
                text = getGreeting(language),
                style = MaaTypography.titleMedium,
                color = MaaColors.textPrimary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(MaaSpacing.small))

            // English translation
            Text(
                text = "\"Hello! I'm Maa. Your language is ${language.englishName}, is that correct?\"",
                style = MaaTypography.bodySmall,
                color = MaaColors.textTertiary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(MaaSpacing.large))

            // Confirmation buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(MaaSpacing.medium)
            ) {
                MaaButton(
                    text = getYesText(language),
                    onClick = onConfirm,
                    modifier = Modifier.weight(1f)
                )
                MaaButton(
                    text = "Different",
                    onClick = onChooseDifferent,
                    style = MaaButtonStyle.OUTLINE,
                    modifier = Modifier.weight(0.7f)
                )
            }

            Spacer(modifier = Modifier.height(MaaSpacing.medium))

            // Voice input hint
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = null,
                    tint = MaaColors.textTertiary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(MaaSpacing.extraSmall))
                Text(
                    text = "Tap mic or speak your response",
                    style = MaaTypography.labelSmall,
                    color = MaaColors.textTertiary
                )
            }
        }
    }
}

private fun getGreeting(language: SupportedLanguage): String {
    return when (language) {
        SupportedLanguage.HINDI -> "नमस्ते! मैं माँ हूं। आपकी भाषा हिंदी है, क्या यह सही है?"
        SupportedLanguage.BENGALI -> "নমস্কার! আমি মা। আপনার ভাষা বাংলা, এটি কি সঠিক?"
        SupportedLanguage.TAMIL -> "வணக்கம்! நான் மா. உங்கள் மொழி தமிழ், இது சரியா?"
        SupportedLanguage.TELUGU -> "నమస్కారం! నేను మా. మీ భాష తెలుగు, ఇది సరైనదా?"
        SupportedLanguage.KANNADA -> "ನಮಸ್ಕಾರ! ನಾನು ಮಾ. ನಿಮ್ಮ ಭಾಷೆ ಕನ್ನಡ, ಇದು ಸರಿಯೇ?"
        SupportedLanguage.MALAYALAM -> "നമസ്കാരം! ഞാൻ മാ. നിങ്ങളുടെ ഭാഷ മലയാളം, ഇത് ശരിയാണോ?"
        SupportedLanguage.MARATHI -> "नमस्कार! मी माँ आहे. तुमची भाषा मराठी आहे, हे बरोबर आहे का?"
        SupportedLanguage.GUJARATI -> "નમસ્તે! હું માં છું. તમારી ભાષા ગુજરાતી છે, આ સાચું છે?"
        SupportedLanguage.PUNJABI -> "ਸਤ ਸ੍ਰੀ ਅਕਾਲ! ਮੈਂ ਮਾਂ ਹਾਂ। ਤੁਹਾਡੀ ਭਾਸ਼ਾ ਪੰਜਾਬੀ ਹੈ, ਕੀ ਇਹ ਸਹੀ ਹੈ?"
        SupportedLanguage.ODIA -> "ନମସ୍କାର! ମୁଁ ମା। ତୁମର ଭାଷା ଓଡ଼ିଆ, ଏହା ଠିକ୍ କି?"
        SupportedLanguage.ENGLISH -> "Hello! I'm Maa. Your language is English, is that correct?"
        else -> "Hello! I'm Maa, your health companion."
    }
}

private fun getYesText(language: SupportedLanguage): String {
    return when (language) {
        SupportedLanguage.HINDI -> "हाँ"
        SupportedLanguage.BENGALI -> "হ্যাঁ"
        SupportedLanguage.TAMIL -> "ஆம்"
        SupportedLanguage.TELUGU -> "అవును"
        SupportedLanguage.KANNADA -> "ಹೌದು"
        SupportedLanguage.MALAYALAM -> "അതെ"
        SupportedLanguage.MARATHI -> "हो"
        SupportedLanguage.GUJARATI -> "હા"
        SupportedLanguage.PUNJABI -> "ਹਾਂ"
        SupportedLanguage.ODIA -> "ହଁ"
        else -> "Yes"
    }
}

@Composable
private fun LanguageCard(
    language: SupportedLanguage,
    isSelected: Boolean,
    hasVoice: Boolean = true,
    onClick: () -> Unit
) {
    val backgroundColor = when {
        isSelected -> MaaColors.accent
        hasVoice -> MaaColors.surface
        else -> MaaColors.surfaceVariant.copy(alpha = 0.7f)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaaShapes.medium)
            .background(backgroundColor)
            .border(
                width = if (isSelected) 0.dp else 1.dp,
                color = if (isSelected) Color.Transparent else MaaColors.border,
                shape = MaaShapes.medium
            )
            .clickable(onClick = onClick)
            .padding(MaaSpacing.small)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Native script name
            Text(
                text = language.nativeName,
                style = MaaTypography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                color = if (isSelected) Color.White else MaaColors.textPrimary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(2.dp))

            // English name
            Text(
                text = language.englishName,
                style = MaaTypography.labelSmall,
                color = if (isSelected) Color.White.copy(alpha = 0.8f) else MaaColors.textSecondary,
                textAlign = TextAlign.Center
            )
        }

        // Voice indicator for voice-supported languages
        if (hasVoice && !isSelected) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(MaaColors.accent.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = "Voice supported",
                    modifier = Modifier.size(10.dp),
                    tint = MaaColors.accent
                )
            }
        }

        // Selection indicator
        if (isSelected) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Selected",
                    modifier = Modifier.size(10.dp),
                    tint = MaaColors.accent
                )
            }
        }
    }
}

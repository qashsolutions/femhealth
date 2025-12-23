package com.maa.health.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.maa.health.service.VoiceAction
import com.maa.health.service.VoiceService
import com.maa.health.service.VoiceState
import com.maa.health.ui.theme.MaaColors
import com.maa.health.ui.theme.MaaShapes
import com.maa.health.ui.theme.MaaSpacing
import com.maa.health.ui.theme.MaaTypography

/**
 * Floating Voice FAB - Global voice input for any screen
 *
 * Features:
 * - Large touch target (64dp)
 * - Animated pulse when listening
 * - Confirmation card with transcription
 * - Confirm/Edit/Cancel actions
 * - Contextual voice prompts
 */
@Composable
fun FloatingVoiceFAB(
    voiceService: VoiceService,
    voiceAction: VoiceAction,
    onTranscriptionConfirmed: (String) -> Unit,
    onEditRequested: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val voiceState by voiceService.voiceState.collectAsState()
    val transcription by voiceService.transcription.collectAsState()
    val currentLanguage by voiceService.currentLanguage.collectAsState()

    val infiniteTransition = rememberInfiniteTransition(label = "voicePulse")

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    val backgroundColor by animateColorAsState(
        targetValue = when (voiceState) {
            is VoiceState.IDLE -> MaaColors.accent
            is VoiceState.LISTENING -> MaaColors.danger
            is VoiceState.PROCESSING -> MaaColors.info
            is VoiceState.CONFIRMING -> MaaColors.safe
            is VoiceState.CONFIRMED -> MaaColors.safe
            is VoiceState.ERROR -> MaaColors.warning
        },
        animationSpec = tween(200),
        label = "bgColor"
    )

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.End
    ) {
        // Confirmation Card (shown when confirming)
        AnimatedVisibility(
            visible = voiceState is VoiceState.CONFIRMING,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
        ) {
            VoiceConfirmationCard(
                transcription = transcription ?: "",
                onConfirm = {
                    transcription?.let { text ->
                        voiceService.confirmTranscription()
                        onTranscriptionConfirmed(text)
                    }
                },
                onEdit = {
                    transcription?.let { text ->
                        voiceService.reset()
                        onEditRequested(text)
                    }
                },
                onCancel = {
                    voiceService.reset()
                }
            )
        }

        Spacer(modifier = Modifier.height(MaaSpacing.medium))

        // Voice prompt hint
        AnimatedVisibility(
            visible = voiceState is VoiceState.IDLE,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaaColors.surface),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.padding(end = MaaSpacing.small)
            ) {
                Text(
                    text = voiceService.getVoicePrompt(voiceAction),
                    style = MaaTypography.labelSmall,
                    color = MaaColors.textSecondary,
                    modifier = Modifier.padding(horizontal = MaaSpacing.medium, vertical = MaaSpacing.small)
                )
            }
        }

        // Listening indicator
        AnimatedVisibility(
            visible = voiceState is VoiceState.LISTENING,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaaColors.dangerLight),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.padding(end = MaaSpacing.small)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = MaaSpacing.medium, vertical = MaaSpacing.small),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Voice wave animation
                    VoiceWaveAnimation()
                    Spacer(modifier = Modifier.width(MaaSpacing.small))
                    Text(
                        text = "Listening...",
                        style = MaaTypography.labelSmall,
                        color = MaaColors.danger
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(MaaSpacing.small))

        // Main FAB
        Box(contentAlignment = Alignment.Center) {
            // Pulse ring
            if (voiceState is VoiceState.LISTENING) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .scale(pulseScale)
                        .clip(CircleShape)
                        .background(backgroundColor.copy(alpha = 0.3f))
                )
            }

            // FAB Button
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .shadow(8.dp, CircleShape)
                    .clip(CircleShape)
                    .background(backgroundColor)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = rememberRipple(bounded = true, color = Color.White),
                        onClick = {
                            when (voiceState) {
                                is VoiceState.IDLE -> {
                                    // Start recording - handled by ViewModel
                                }
                                is VoiceState.LISTENING -> {
                                    // Stop recording - handled by ViewModel
                                }
                                else -> {}
                            }
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (voiceState) {
                        is VoiceState.LISTENING -> Icons.Default.Stop
                        else -> Icons.Default.Mic
                    },
                    contentDescription = when (voiceState) {
                        is VoiceState.IDLE -> "Tap to speak"
                        is VoiceState.LISTENING -> "Tap to stop"
                        is VoiceState.PROCESSING -> "Processing"
                        else -> "Voice input"
                    },
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}

/**
 * Voice confirmation card with transcription and actions
 */
@Composable
private fun VoiceConfirmationCard(
    transcription: String,
    onConfirm: () -> Unit,
    onEdit: () -> Unit,
    onCancel: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaaColors.surface),
        shape = MaaShapes.large,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = MaaSpacing.medium)
            .shadow(4.dp, MaaShapes.large)
    ) {
        Column(
            modifier = Modifier.padding(MaaSpacing.medium)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Is this correct?",
                    style = MaaTypography.titleSmall,
                    color = MaaColors.textPrimary
                )
                IconButton(
                    onClick = onCancel,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Cancel",
                        tint = MaaColors.textTertiary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(MaaSpacing.small))

            // Transcription
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(MaaShapes.medium)
                    .background(MaaColors.surfaceVariant)
                    .padding(MaaSpacing.medium)
            ) {
                Text(
                    text = "\"$transcription\"",
                    style = MaaTypography.bodyMedium,
                    color = MaaColors.textPrimary
                )
            }

            Spacer(modifier = Modifier.height(MaaSpacing.medium))

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(MaaSpacing.small)
            ) {
                // Confirm button
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .clip(MaaShapes.medium)
                        .background(MaaColors.safe)
                        .clickable(onClick = onConfirm),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(MaaSpacing.small))
                        Text(
                            text = "Confirm",
                            style = MaaTypography.labelLarge,
                            color = Color.White
                        )
                    }
                }

                // Edit button
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .clip(MaaShapes.medium)
                        .border(1.dp, MaaColors.border, MaaShapes.medium)
                        .clickable(onClick = onEdit),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = null,
                            tint = MaaColors.textSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(MaaSpacing.small))
                        Text(
                            text = "Edit",
                            style = MaaTypography.labelLarge,
                            color = MaaColors.textSecondary
                        )
                    }
                }
            }
        }
    }
}

/**
 * Voice wave animation indicator
 */
@Composable
private fun VoiceWaveAnimation() {
    val infiniteTransition = rememberInfiniteTransition(label = "wave")

    Row(
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(4) { index ->
            val height by infiniteTransition.animateFloat(
                initialValue = 8f,
                targetValue = 16f,
                animationSpec = infiniteRepeatable(
                    animation = tween(300, delayMillis = index * 100),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "bar$index"
            )

            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(height.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(MaaColors.danger)
            )
        }
    }
}

/**
 * Compact voice button for inline use in text fields
 */
@Composable
fun InlineVoiceButton(
    voiceState: VoiceState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor by animateColorAsState(
        targetValue = when (voiceState) {
            is VoiceState.IDLE -> MaaColors.surfaceVariant
            is VoiceState.LISTENING -> MaaColors.danger.copy(alpha = 0.2f)
            is VoiceState.PROCESSING -> MaaColors.info.copy(alpha = 0.2f)
            else -> MaaColors.surfaceVariant
        },
        animationSpec = tween(200),
        label = "bgColor"
    )

    val iconTint by animateColorAsState(
        targetValue = when (voiceState) {
            is VoiceState.IDLE -> MaaColors.accent
            is VoiceState.LISTENING -> MaaColors.danger
            is VoiceState.PROCESSING -> MaaColors.info
            else -> MaaColors.accent
        },
        animationSpec = tween(200),
        label = "iconTint"
    )

    Box(
        modifier = modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(backgroundColor)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = if (voiceState is VoiceState.LISTENING) Icons.Default.Stop else Icons.Default.Mic,
            contentDescription = "Voice input",
            tint = iconTint,
            modifier = Modifier.size(20.dp)
        )
    }
}

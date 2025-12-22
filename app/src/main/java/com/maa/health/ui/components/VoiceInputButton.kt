package com.maa.health.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.maa.health.ui.theme.MaaColors
import com.maa.health.ui.theme.MaaSpacing
import com.maa.health.ui.theme.MaaTypography

/**
 * Voice input button states
 */
enum class VoiceInputState {
    IDLE,           // Ready to record
    LISTENING,      // Recording audio
    PROCESSING,     // Converting speech to text
    ERROR,          // Recording failed
    DISABLED        // Microphone unavailable
}

/**
 * Voice input button for Sarvam AI STT integration
 *
 * Features:
 * - Large touch target (56dp minimum)
 * - Animated pulse when listening
 * - Clear state indication
 * - Accessibility support
 */
@Composable
fun VoiceInputButton(
    state: VoiceInputState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    hint: String = "Tap to speak",
    processingText: String = "Listening...",
    showHint: Boolean = true
) {
    val infiniteTransition = rememberInfiniteTransition(label = "voicePulse")

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(600),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    val backgroundColor by animateColorAsState(
        targetValue = when (state) {
            VoiceInputState.IDLE -> MaaColors.accent
            VoiceInputState.LISTENING -> MaaColors.danger
            VoiceInputState.PROCESSING -> MaaColors.info
            VoiceInputState.ERROR -> MaaColors.warning
            VoiceInputState.DISABLED -> MaaColors.textDisabled
        },
        animationSpec = tween(200),
        label = "bgColor"
    )

    val iconTint = Color.White

    val icon = when (state) {
        VoiceInputState.IDLE -> Icons.Default.Mic
        VoiceInputState.LISTENING -> Icons.Default.Stop
        VoiceInputState.PROCESSING -> Icons.Default.Mic
        VoiceInputState.ERROR -> Icons.Default.MicOff
        VoiceInputState.DISABLED -> Icons.Default.MicOff
    }

    val accessibilityLabel = when (state) {
        VoiceInputState.IDLE -> "Tap to start voice input"
        VoiceInputState.LISTENING -> "Recording. Tap to stop"
        VoiceInputState.PROCESSING -> "Processing your speech"
        VoiceInputState.ERROR -> "Voice input error. Tap to retry"
        VoiceInputState.DISABLED -> "Voice input unavailable"
    }

    val displayText = when (state) {
        VoiceInputState.IDLE -> hint
        VoiceInputState.LISTENING -> processingText
        VoiceInputState.PROCESSING -> "Processing..."
        VoiceInputState.ERROR -> "Tap to retry"
        VoiceInputState.DISABLED -> "Unavailable"
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            contentAlignment = Alignment.Center
        ) {
            // Pulse ring when listening
            if (state == VoiceInputState.LISTENING) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .scale(pulseScale)
                        .clip(CircleShape)
                        .background(backgroundColor.copy(alpha = 0.3f))
                )
            }

            // Main button
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(backgroundColor)
                    .clickable(
                        enabled = state != VoiceInputState.DISABLED && state != VoiceInputState.PROCESSING,
                        interactionSource = remember { MutableInteractionSource() },
                        indication = rememberRipple(bounded = true, color = Color.White),
                        onClick = onClick
                    )
                    .semantics {
                        contentDescription = accessibilityLabel
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(28.dp),
                    tint = iconTint
                )
            }
        }

        if (showHint) {
            Spacer(modifier = Modifier.height(MaaSpacing.small))
            Text(
                text = displayText,
                style = MaaTypography.labelSmall,
                color = when (state) {
                    VoiceInputState.ERROR -> MaaColors.warning
                    VoiceInputState.DISABLED -> MaaColors.textDisabled
                    else -> MaaColors.textSecondary
                },
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Compact voice input button for inline use
 */
@Composable
fun VoiceInputButtonCompact(
    state: VoiceInputState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor by animateColorAsState(
        targetValue = when (state) {
            VoiceInputState.IDLE -> MaaColors.surfaceVariant
            VoiceInputState.LISTENING -> MaaColors.danger.copy(alpha = 0.2f)
            VoiceInputState.PROCESSING -> MaaColors.info.copy(alpha = 0.2f)
            VoiceInputState.ERROR -> MaaColors.warning.copy(alpha = 0.2f)
            VoiceInputState.DISABLED -> MaaColors.textDisabled.copy(alpha = 0.2f)
        },
        animationSpec = tween(200),
        label = "bgColor"
    )

    val iconTint by animateColorAsState(
        targetValue = when (state) {
            VoiceInputState.IDLE -> MaaColors.accent
            VoiceInputState.LISTENING -> MaaColors.danger
            VoiceInputState.PROCESSING -> MaaColors.info
            VoiceInputState.ERROR -> MaaColors.warning
            VoiceInputState.DISABLED -> MaaColors.textDisabled
        },
        animationSpec = tween(200),
        label = "iconTint"
    )

    val icon = when (state) {
        VoiceInputState.IDLE -> Icons.Default.Mic
        VoiceInputState.LISTENING -> Icons.Default.Stop
        VoiceInputState.PROCESSING -> Icons.Default.Mic
        VoiceInputState.ERROR -> Icons.Default.MicOff
        VoiceInputState.DISABLED -> Icons.Default.MicOff
    }

    Box(
        modifier = modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(backgroundColor)
            .clickable(
                enabled = state != VoiceInputState.DISABLED && state != VoiceInputState.PROCESSING,
                interactionSource = remember { MutableInteractionSource() },
                indication = rememberRipple(bounded = true),
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = when (state) {
                VoiceInputState.IDLE -> "Voice input"
                VoiceInputState.LISTENING -> "Stop recording"
                VoiceInputState.PROCESSING -> "Processing"
                VoiceInputState.ERROR -> "Voice input error"
                VoiceInputState.DISABLED -> "Voice input disabled"
            },
            modifier = Modifier.size(20.dp),
            tint = iconTint
        )
    }
}

/**
 * Voice input field with text result
 */
@Composable
fun VoiceInputField(
    value: String,
    onValueChange: (String) -> Unit,
    voiceState: VoiceInputState,
    onVoiceClick: () -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Speak or type...",
    label: String? = null
) {
    Column(modifier = modifier) {
        if (label != null) {
            Text(
                text = label,
                style = MaaTypography.labelMedium,
                color = MaaColors.textSecondary
            )
            Spacer(modifier = Modifier.height(MaaSpacing.small))
        }

        Box(
            modifier = Modifier
                .clip(com.maa.health.ui.theme.MaaShapes.medium)
                .background(MaaColors.surfaceVariant)
                .border(
                    width = 1.dp,
                    color = if (voiceState == VoiceInputState.LISTENING) {
                        MaaColors.danger
                    } else {
                        MaaColors.border
                    },
                    shape = com.maa.health.ui.theme.MaaShapes.medium
                )
                .padding(MaaSpacing.medium)
        ) {
            androidx.compose.foundation.layout.Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                androidx.compose.foundation.text.BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    modifier = Modifier.weight(1f),
                    textStyle = MaaTypography.bodyMedium.copy(color = MaaColors.textPrimary),
                    decorationBox = { innerTextField ->
                        if (value.isEmpty()) {
                            Text(
                                text = placeholder,
                                style = MaaTypography.bodyMedium,
                                color = MaaColors.textTertiary
                            )
                        }
                        innerTextField()
                    }
                )

                Spacer(modifier = Modifier.size(MaaSpacing.small))

                VoiceInputButtonCompact(
                    state = voiceState,
                    onClick = onVoiceClick
                )
            }
        }
    }
}

package com.maa.health.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.maa.health.ui.theme.MaaColors
import com.maa.health.ui.theme.MaaElevation
import com.maa.health.ui.theme.MaaShapes
import com.maa.health.ui.theme.MaaSpacing
import com.maa.health.ui.theme.MaaTypography

/**
 * Card variants for different use cases
 */
enum class MaaCardVariant {
    DEFAULT,        // Standard surface card
    ELEVATED,       // With shadow
    OUTLINED,       // With border
    FILLED,         // Colored background
    URGENCY         // For triage results
}

/**
 * Base card component
 */
@Composable
fun MaaCard(
    modifier: Modifier = Modifier,
    variant: MaaCardVariant = MaaCardVariant.DEFAULT,
    backgroundColor: Color = MaaColors.surface,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed && onClick != null) 0.98f else 1f,
        animationSpec = tween(100),
        label = "cardScale"
    )

    val elevation = when (variant) {
        MaaCardVariant.DEFAULT -> MaaElevation.level0
        MaaCardVariant.ELEVATED -> MaaElevation.level2
        MaaCardVariant.OUTLINED -> MaaElevation.level0
        MaaCardVariant.FILLED -> MaaElevation.level0
        MaaCardVariant.URGENCY -> MaaElevation.level1
    }

    val border = if (variant == MaaCardVariant.OUTLINED) {
        BorderStroke(1.dp, MaaColors.border)
    } else null

    Card(
        modifier = modifier
            .scale(scale)
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = interactionSource,
                        indication = rememberRipple(color = MaaColors.touchFeedback),
                        onClick = onClick
                    )
                } else Modifier
            ),
        shape = MaaShapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = elevation
        ),
        border = border
    ) {
        content()
    }
}

/**
 * Info card with icon, title, and description
 */
@Composable
fun MaaInfoCard(
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    iconBackgroundColor: Color = MaaColors.accentLight,
    iconColor: Color = MaaColors.accent,
    onClick: (() -> Unit)? = null
) {
    MaaCard(
        modifier = modifier.fillMaxWidth(),
        variant = MaaCardVariant.DEFAULT,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(MaaSpacing.medium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(MaaShapes.small)
                        .background(iconBackgroundColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = iconColor
                    )
                }
                Spacer(modifier = Modifier.width(MaaSpacing.medium))
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaaTypography.titleSmall,
                    color = MaaColors.textPrimary
                )
                Spacer(modifier = Modifier.height(MaaSpacing.extraSmall))
                Text(
                    text = description,
                    style = MaaTypography.bodySmall,
                    color = MaaColors.textSecondary
                )
            }
        }
    }
}

/**
 * Stat card for displaying metrics
 */
@Composable
fun MaaStatCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    unit: String? = null,
    trend: String? = null,
    trendIsPositive: Boolean = true,
    onClick: (() -> Unit)? = null
) {
    MaaCard(
        modifier = modifier,
        variant = MaaCardVariant.ELEVATED,
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.padding(MaaSpacing.medium)
        ) {
            Text(
                text = label,
                style = MaaTypography.labelMedium,
                color = MaaColors.textTertiary
            )
            Spacer(modifier = Modifier.height(MaaSpacing.small))
            Row(
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = value,
                    style = MaaTypography.dataLarge,
                    color = MaaColors.textPrimary
                )
                if (unit != null) {
                    Spacer(modifier = Modifier.width(MaaSpacing.extraSmall))
                    Text(
                        text = unit,
                        style = MaaTypography.bodySmall,
                        color = MaaColors.textSecondary
                    )
                }
            }
            if (trend != null) {
                Spacer(modifier = Modifier.height(MaaSpacing.extraSmall))
                Text(
                    text = trend,
                    style = MaaTypography.labelSmall,
                    color = if (trendIsPositive) MaaColors.safe else MaaColors.warning
                )
            }
        }
    }
}

/**
 * Urgency card for triage results
 */
@Composable
fun MaaUrgencyCard(
    title: String,
    description: String,
    urgencyLevel: UrgencyLevel,
    modifier: Modifier = Modifier,
    actionText: String? = null,
    onAction: (() -> Unit)? = null
) {
    val (backgroundColor, accentColor) = when (urgencyLevel) {
        UrgencyLevel.EMERGENCY -> MaaColors.dangerLight to MaaColors.danger
        UrgencyLevel.URGENT -> MaaColors.warningLight to MaaColors.warning
        UrgencyLevel.ROUTINE -> MaaColors.cautionLight to MaaColors.caution
        UrgencyLevel.SELF_CARE -> MaaColors.safeLight to MaaColors.safe
    }

    MaaCard(
        modifier = modifier.fillMaxWidth(),
        variant = MaaCardVariant.URGENCY,
        backgroundColor = backgroundColor
    ) {
        Column(
            modifier = Modifier.padding(MaaSpacing.medium)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(MaaShapes.extraSmall)
                        .background(accentColor)
                )
                Spacer(modifier = Modifier.width(MaaSpacing.small))
                Text(
                    text = urgencyLevel.label,
                    style = MaaTypography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = accentColor
                )
            }
            Spacer(modifier = Modifier.height(MaaSpacing.small))
            Text(
                text = title,
                style = MaaTypography.titleMedium,
                color = MaaColors.textPrimary
            )
            Spacer(modifier = Modifier.height(MaaSpacing.extraSmall))
            Text(
                text = description,
                style = MaaTypography.bodyMedium,
                color = MaaColors.textSecondary
            )
            if (actionText != null && onAction != null) {
                Spacer(modifier = Modifier.height(MaaSpacing.medium))
                MaaButton(
                    text = actionText,
                    onClick = onAction,
                    variant = if (urgencyLevel == UrgencyLevel.EMERGENCY) {
                        MaaButtonVariant.EMERGENCY
                    } else {
                        MaaButtonVariant.PRIMARY
                    },
                    fullWidth = true
                )
            }
        }
    }
}

enum class UrgencyLevel(val label: String) {
    EMERGENCY("Emergency"),
    URGENT("Urgent"),
    ROUTINE("Routine"),
    SELF_CARE("Self Care")
}

/**
 * Lifecycle stage card
 */
@Composable
fun MaaLifecycleCard(
    title: String,
    description: String,
    backgroundColor: Color,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    isSelected: Boolean = false,
    onClick: () -> Unit
) {
    val animatedBorderColor by animateColorAsState(
        targetValue = if (isSelected) MaaColors.accent else Color.Transparent,
        animationSpec = tween(200),
        label = "borderColor"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = MaaShapes.medium,
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        border = if (isSelected) BorderStroke(2.dp, animatedBorderColor) else null,
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) MaaElevation.level2 else MaaElevation.level0
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(MaaSpacing.medium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = MaaColors.textPrimary
                )
                Spacer(modifier = Modifier.width(MaaSpacing.medium))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaaTypography.titleSmall,
                    color = MaaColors.textPrimary
                )
                Spacer(modifier = Modifier.height(MaaSpacing.extraSmall))
                Text(
                    text = description,
                    style = MaaTypography.bodySmall,
                    color = MaaColors.textSecondary
                )
            }
            if (isSelected) {
                Icon(
                    imageVector = androidx.compose.material.icons.Icons.Default.Check,
                    contentDescription = "Selected",
                    modifier = Modifier.size(24.dp),
                    tint = MaaColors.accent
                )
            }
        }
    }
}

/**
 * Danger sign warning card
 */
@Composable
fun MaaDangerSignCard(
    title: String,
    signs: List<String>,
    modifier: Modifier = Modifier,
    onEmergencyClick: () -> Unit
) {
    MaaCard(
        modifier = modifier.fillMaxWidth(),
        variant = MaaCardVariant.URGENCY,
        backgroundColor = MaaColors.dangerLight
    ) {
        Column(
            modifier = Modifier.padding(MaaSpacing.medium)
        ) {
            Text(
                text = title,
                style = MaaTypography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaaColors.danger
            )
            Spacer(modifier = Modifier.height(MaaSpacing.small))
            signs.forEach { sign ->
                Row(
                    modifier = Modifier.padding(vertical = MaaSpacing.extraSmall),
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = "\u2022",
                        style = MaaTypography.bodyMedium,
                        color = MaaColors.danger
                    )
                    Spacer(modifier = Modifier.width(MaaSpacing.small))
                    Text(
                        text = sign,
                        style = MaaTypography.bodyMedium,
                        color = MaaColors.textPrimary
                    )
                }
            }
            Spacer(modifier = Modifier.height(MaaSpacing.medium))
            MaaButton(
                text = "Get Emergency Help",
                onClick = onEmergencyClick,
                variant = MaaButtonVariant.EMERGENCY,
                fullWidth = true
            )
        }
    }
}

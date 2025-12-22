package com.maa.health.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.maa.health.ui.theme.MaaColors
import com.maa.health.ui.theme.MaaShapes
import com.maa.health.ui.theme.MaaTypography

/**
 * Button variants for Maa app
 */
enum class MaaButtonVariant {
    PRIMARY,        // Filled, accent color
    SECONDARY,      // Outlined
    TERTIARY,       // Text only
    DANGER,         // Red, destructive actions
    EMERGENCY       // High contrast emergency button
}

/**
 * Button sizes
 */
enum class MaaButtonSize {
    SMALL,          // Compact actions
    MEDIUM,         // Default
    LARGE           // Primary CTAs
}

/**
 * Primary button component for Maa app
 *
 * Follows design guidelines:
 * - Warm taupe accent color
 * - Appropriate sizing for touch targets
 * - Loading states
 * - Icon support
 */
@Composable
fun MaaButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: MaaButtonVariant = MaaButtonVariant.PRIMARY,
    size: MaaButtonSize = MaaButtonSize.MEDIUM,
    enabled: Boolean = true,
    loading: Boolean = false,
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
    fullWidth: Boolean = false
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = tween(100),
        label = "buttonScale"
    )

    val height = when (size) {
        MaaButtonSize.SMALL -> 36.dp
        MaaButtonSize.MEDIUM -> 48.dp
        MaaButtonSize.LARGE -> 56.dp
    }

    val contentPadding = when (size) {
        MaaButtonSize.SMALL -> PaddingValues(horizontal = 12.dp, vertical = 8.dp)
        MaaButtonSize.MEDIUM -> PaddingValues(horizontal = 16.dp, vertical = 12.dp)
        MaaButtonSize.LARGE -> PaddingValues(horizontal = 24.dp, vertical = 16.dp)
    }

    val textStyle = when (size) {
        MaaButtonSize.SMALL -> MaaTypography.labelMedium
        MaaButtonSize.MEDIUM -> MaaTypography.labelLarge
        MaaButtonSize.LARGE -> MaaTypography.titleSmall
    }

    val buttonModifier = modifier
        .height(height)
        .scale(scale)
        .then(if (fullWidth) Modifier.fillMaxWidth() else Modifier)

    when (variant) {
        MaaButtonVariant.PRIMARY -> {
            Button(
                onClick = { if (!loading) onClick() },
                modifier = buttonModifier,
                enabled = enabled && !loading,
                shape = MaaShapes.medium,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaaColors.accent,
                    contentColor = Color.White,
                    disabledContainerColor = MaaColors.accent.copy(alpha = 0.4f),
                    disabledContentColor = Color.White.copy(alpha = 0.6f)
                ),
                contentPadding = contentPadding,
                interactionSource = interactionSource
            ) {
                ButtonContent(
                    text = text,
                    textStyle = textStyle,
                    loading = loading,
                    leadingIcon = leadingIcon,
                    trailingIcon = trailingIcon,
                    iconTint = Color.White
                )
            }
        }

        MaaButtonVariant.SECONDARY -> {
            OutlinedButton(
                onClick = { if (!loading) onClick() },
                modifier = buttonModifier,
                enabled = enabled && !loading,
                shape = MaaShapes.medium,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaaColors.accent,
                    disabledContentColor = MaaColors.accent.copy(alpha = 0.4f)
                ),
                border = BorderStroke(
                    width = 1.5.dp,
                    color = if (enabled) MaaColors.accent else MaaColors.accent.copy(alpha = 0.4f)
                ),
                contentPadding = contentPadding,
                interactionSource = interactionSource
            ) {
                ButtonContent(
                    text = text,
                    textStyle = textStyle,
                    loading = loading,
                    leadingIcon = leadingIcon,
                    trailingIcon = trailingIcon,
                    iconTint = MaaColors.accent
                )
            }
        }

        MaaButtonVariant.TERTIARY -> {
            TextButton(
                onClick = { if (!loading) onClick() },
                modifier = buttonModifier,
                enabled = enabled && !loading,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaaColors.accent,
                    disabledContentColor = MaaColors.accent.copy(alpha = 0.4f)
                ),
                contentPadding = contentPadding,
                interactionSource = interactionSource
            ) {
                ButtonContent(
                    text = text,
                    textStyle = textStyle,
                    loading = loading,
                    leadingIcon = leadingIcon,
                    trailingIcon = trailingIcon,
                    iconTint = MaaColors.accent
                )
            }
        }

        MaaButtonVariant.DANGER -> {
            Button(
                onClick = { if (!loading) onClick() },
                modifier = buttonModifier,
                enabled = enabled && !loading,
                shape = MaaShapes.medium,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaaColors.danger,
                    contentColor = Color.White,
                    disabledContainerColor = MaaColors.danger.copy(alpha = 0.4f),
                    disabledContentColor = Color.White.copy(alpha = 0.6f)
                ),
                contentPadding = contentPadding,
                interactionSource = interactionSource
            ) {
                ButtonContent(
                    text = text,
                    textStyle = textStyle,
                    loading = loading,
                    leadingIcon = leadingIcon,
                    trailingIcon = trailingIcon,
                    iconTint = Color.White
                )
            }
        }

        MaaButtonVariant.EMERGENCY -> {
            Button(
                onClick = { if (!loading) onClick() },
                modifier = buttonModifier,
                enabled = enabled && !loading,
                shape = MaaShapes.medium,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaaColors.danger,
                    contentColor = Color.White
                ),
                contentPadding = contentPadding,
                interactionSource = interactionSource
            ) {
                ButtonContent(
                    text = text.uppercase(),
                    textStyle = textStyle.copy(fontWeight = FontWeight.Bold),
                    loading = loading,
                    leadingIcon = leadingIcon,
                    trailingIcon = trailingIcon,
                    iconTint = Color.White
                )
            }
        }
    }
}

@Composable
private fun ButtonContent(
    text: String,
    textStyle: androidx.compose.ui.text.TextStyle,
    loading: Boolean,
    leadingIcon: ImageVector?,
    trailingIcon: ImageVector?,
    iconTint: Color
) {
    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(18.dp),
                color = iconTint,
                strokeWidth = 2.dp
            )
            Spacer(modifier = Modifier.width(8.dp))
        } else if (leadingIcon != null) {
            Icon(
                imageVector = leadingIcon,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = iconTint
            )
            Spacer(modifier = Modifier.width(8.dp))
        }

        Text(
            text = text,
            style = textStyle
        )

        if (trailingIcon != null && !loading) {
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = trailingIcon,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = iconTint
            )
        }
    }
}

/**
 * Icon-only button
 */
@Composable
fun MaaIconButton(
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    variant: MaaButtonVariant = MaaButtonVariant.TERTIARY,
    size: MaaButtonSize = MaaButtonSize.MEDIUM,
    enabled: Boolean = true
) {
    val iconSize = when (size) {
        MaaButtonSize.SMALL -> 18.dp
        MaaButtonSize.MEDIUM -> 24.dp
        MaaButtonSize.LARGE -> 28.dp
    }

    val buttonSize = when (size) {
        MaaButtonSize.SMALL -> 36.dp
        MaaButtonSize.MEDIUM -> 48.dp
        MaaButtonSize.LARGE -> 56.dp
    }

    val containerColor = when (variant) {
        MaaButtonVariant.PRIMARY -> MaaColors.accent
        MaaButtonVariant.SECONDARY -> Color.Transparent
        MaaButtonVariant.TERTIARY -> Color.Transparent
        MaaButtonVariant.DANGER -> MaaColors.danger
        MaaButtonVariant.EMERGENCY -> MaaColors.danger
    }

    val iconColor = when (variant) {
        MaaButtonVariant.PRIMARY -> Color.White
        MaaButtonVariant.SECONDARY -> MaaColors.accent
        MaaButtonVariant.TERTIARY -> MaaColors.textSecondary
        MaaButtonVariant.DANGER -> Color.White
        MaaButtonVariant.EMERGENCY -> Color.White
    }

    androidx.compose.material3.IconButton(
        onClick = onClick,
        modifier = modifier.size(buttonSize),
        enabled = enabled,
        colors = androidx.compose.material3.IconButtonDefaults.iconButtonColors(
            containerColor = containerColor,
            contentColor = iconColor,
            disabledContainerColor = containerColor.copy(alpha = 0.4f),
            disabledContentColor = iconColor.copy(alpha = 0.4f)
        )
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            modifier = Modifier.size(iconSize)
        )
    }
}

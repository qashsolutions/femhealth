package com.maa.health.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.maa.health.ui.theme.MaaColors
import com.maa.health.ui.theme.MaaShapes
import com.maa.health.ui.theme.MaaSpacing
import com.maa.health.ui.theme.MaaTypography

/**
 * Standard text field component
 */
@Composable
fun MaaTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String = "",
    helperText: String? = null,
    errorText: String? = null,
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
    onTrailingIconClick: (() -> Unit)? = null,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    singleLine: Boolean = true,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    val hasError = errorText != null

    val borderColor by animateColorAsState(
        targetValue = when {
            hasError -> MaaColors.danger
            isFocused -> MaaColors.accent
            else -> MaaColors.border
        },
        animationSpec = tween(200),
        label = "borderColor"
    )

    val backgroundColor = when {
        !enabled -> MaaColors.surfaceVariant.copy(alpha = 0.5f)
        else -> MaaColors.surfaceVariant
    }

    Column(modifier = modifier.fillMaxWidth()) {
        // Label
        if (label != null) {
            Text(
                text = label,
                style = MaaTypography.labelMedium,
                color = if (hasError) MaaColors.danger else MaaColors.textSecondary
            )
            Spacer(modifier = Modifier.height(MaaSpacing.small))
        }

        // Input field
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .clip(MaaShapes.medium)
                .background(backgroundColor)
                .border(
                    width = 1.dp,
                    color = borderColor,
                    shape = MaaShapes.medium
                )
                .padding(horizontal = MaaSpacing.medium, vertical = MaaSpacing.mediumLarge),
            enabled = enabled,
            readOnly = readOnly,
            textStyle = MaaTypography.bodyMedium.copy(color = MaaColors.textPrimary),
            cursorBrush = SolidColor(MaaColors.accent),
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            singleLine = singleLine,
            maxLines = maxLines,
            visualTransformation = visualTransformation,
            interactionSource = interactionSource,
            decorationBox = { innerTextField ->
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (leadingIcon != null) {
                        Icon(
                            imageVector = leadingIcon,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = MaaColors.textTertiary
                        )
                        Spacer(modifier = Modifier.width(MaaSpacing.small))
                    }

                    Box(modifier = Modifier.weight(1f)) {
                        if (value.isEmpty()) {
                            Text(
                                text = placeholder,
                                style = MaaTypography.bodyMedium,
                                color = MaaColors.textTertiary
                            )
                        }
                        innerTextField()
                    }

                    if (trailingIcon != null) {
                        Spacer(modifier = Modifier.width(MaaSpacing.small))
                        if (onTrailingIconClick != null) {
                            IconButton(
                                onClick = onTrailingIconClick,
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = trailingIcon,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp),
                                    tint = MaaColors.textTertiary
                                )
                            }
                        } else {
                            Icon(
                                imageVector = trailingIcon,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = MaaColors.textTertiary
                            )
                        }
                    }
                }
            }
        )

        // Helper/Error text
        if (errorText != null || helperText != null) {
            Spacer(modifier = Modifier.height(MaaSpacing.extraSmall))
            Text(
                text = errorText ?: helperText ?: "",
                style = MaaTypography.labelSmall,
                color = if (hasError) MaaColors.danger else MaaColors.textTertiary
            )
        }
    }
}

/**
 * Password field with visibility toggle
 */
@Composable
fun MaaPasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String = "Enter password",
    errorText: String? = null
) {
    var passwordVisible by remember { mutableStateOf(false) }

    MaaTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        label = label,
        placeholder = placeholder,
        errorText = errorText,
        trailingIcon = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
        onTrailingIconClick = { passwordVisible = !passwordVisible },
        visualTransformation = if (passwordVisible) {
            VisualTransformation.None
        } else {
            PasswordVisualTransformation()
        },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Password,
            imeAction = ImeAction.Done
        )
    )
}

/**
 * Phone number field with country picker
 */
@Composable
fun MaaPhoneField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    selectedCountry: com.maa.health.data.model.SupportedCountry = com.maa.health.data.model.SupportedCountry.INDIA,
    onCountryClick: () -> Unit = {},
    label: String? = null,
    placeholder: String = "Phone number",
    errorText: String? = null,
    maxLength: Int = 15  // Flexible for different country formats
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    val hasError = errorText != null

    val borderColor by animateColorAsState(
        targetValue = when {
            hasError -> MaaColors.danger
            isFocused -> MaaColors.accent
            else -> MaaColors.border
        },
        animationSpec = tween(200),
        label = "borderColor"
    )

    Column(modifier = modifier.fillMaxWidth()) {
        if (label != null) {
            Text(
                text = label,
                style = MaaTypography.labelMedium,
                color = if (hasError) MaaColors.danger else MaaColors.textSecondary
            )
            Spacer(modifier = Modifier.height(MaaSpacing.small))
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Country picker button
            Box(
                modifier = Modifier
                    .clip(MaaShapes.medium)
                    .background(MaaColors.surfaceVariant)
                    .border(1.dp, borderColor, MaaShapes.medium)
                    .clickable { onCountryClick() }
                    .padding(horizontal = MaaSpacing.medium, vertical = MaaSpacing.mediumLarge),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(MaaSpacing.extraSmall)
                ) {
                    Text(
                        text = selectedCountry.flag,
                        style = MaaTypography.bodyMedium
                    )
                    Text(
                        text = selectedCountry.dialCode,
                        style = MaaTypography.bodyMedium,
                        color = MaaColors.textPrimary
                    )
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Select country",
                        modifier = Modifier.size(16.dp),
                        tint = MaaColors.textTertiary
                    )
                }
            }

            Spacer(modifier = Modifier.width(MaaSpacing.small))

            // Phone number input
            BasicTextField(
                value = value,
                onValueChange = { newValue ->
                    // Only allow digits, flexible max length for different countries
                    if (newValue.all { it.isDigit() } && newValue.length <= maxLength) {
                        onValueChange(newValue)
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .clip(MaaShapes.medium)
                    .background(MaaColors.surfaceVariant)
                    .border(1.dp, borderColor, MaaShapes.medium)
                    .padding(horizontal = MaaSpacing.medium, vertical = MaaSpacing.mediumLarge),
                textStyle = MaaTypography.bodyMedium.copy(color = MaaColors.textPrimary),
                cursorBrush = SolidColor(MaaColors.accent),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Phone,
                    imeAction = ImeAction.Done
                ),
                singleLine = true,
                interactionSource = interactionSource,
                decorationBox = { innerTextField ->
                    Box {
                        if (value.isEmpty()) {
                            Text(
                                text = placeholder,
                                style = MaaTypography.bodyMedium,
                                color = MaaColors.textTertiary
                            )
                        }
                        innerTextField()
                    }
                }
            )
        }

        if (errorText != null) {
            Spacer(modifier = Modifier.height(MaaSpacing.extraSmall))
            Text(
                text = errorText,
                style = MaaTypography.labelSmall,
                color = MaaColors.danger
            )
        }
    }
}

/**
 * OTP input field
 */
@Composable
fun MaaOtpField(
    otpLength: Int = 6,
    otpValue: String,
    onOtpChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    errorText: String? = null
) {
    val hasError = errorText != null

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(MaaSpacing.small)
        ) {
            repeat(otpLength) { index ->
                val char = otpValue.getOrNull(index)?.toString() ?: ""
                val isFilled = char.isNotEmpty()

                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(MaaShapes.small)
                        .background(MaaColors.surfaceVariant)
                        .border(
                            width = 1.5.dp,
                            color = when {
                                hasError -> MaaColors.danger
                                isFilled -> MaaColors.accent
                                else -> MaaColors.border
                            },
                            shape = MaaShapes.small
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = char,
                        style = MaaTypography.headlineSmall,
                        color = MaaColors.textPrimary
                    )
                }
            }
        }

        // Hidden text field for actual input
        BasicTextField(
            value = otpValue,
            onValueChange = { newValue ->
                if (newValue.all { it.isDigit() } && newValue.length <= otpLength) {
                    onOtpChange(newValue)
                }
            },
            modifier = Modifier
                .size(1.dp)
                .background(Color.Transparent),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done
            )
        )

        if (errorText != null) {
            Spacer(modifier = Modifier.height(MaaSpacing.small))
            Text(
                text = errorText,
                style = MaaTypography.labelSmall,
                color = MaaColors.danger
            )
        }
    }
}

/**
 * Multiline text area
 */
@Composable
fun MaaTextArea(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String = "",
    helperText: String? = null,
    errorText: String? = null,
    minLines: Int = 3,
    maxLines: Int = 6,
    maxLength: Int? = null
) {
    Column(modifier = modifier.fillMaxWidth()) {
        MaaTextField(
            value = value,
            onValueChange = { newValue ->
                if (maxLength == null || newValue.length <= maxLength) {
                    onValueChange(newValue)
                }
            },
            label = label,
            placeholder = placeholder,
            helperText = helperText,
            errorText = errorText,
            singleLine = false,
            maxLines = maxLines
        )

        if (maxLength != null) {
            Spacer(modifier = Modifier.height(MaaSpacing.extraSmall))
            Text(
                text = "${value.length}/$maxLength",
                style = MaaTypography.labelSmall,
                color = MaaColors.textTertiary,
                modifier = Modifier.align(Alignment.End)
            )
        }
    }
}

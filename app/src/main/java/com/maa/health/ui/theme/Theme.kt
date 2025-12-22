package com.maa.health.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * Maa Theme
 *
 * Design Philosophy:
 * - Warm, dignified, medical-reference quality
 * - NO dark theme (medical apps typically stay light for readability)
 * - Consistent warm earth tones throughout
 */

// Material 3 color scheme mapped to Maa colors
private val MaaLightColorScheme = lightColorScheme(
    primary = MaaColors.accent,
    onPrimary = MaaColors.surfaceElevated,
    primaryContainer = MaaColors.accentLight,
    onPrimaryContainer = MaaColors.accentDark,

    secondary = MaaColors.info,
    onSecondary = MaaColors.surfaceElevated,
    secondaryContainer = MaaColors.infoLight,
    onSecondaryContainer = MaaColors.textPrimary,

    tertiary = MaaColors.caution,
    onTertiary = MaaColors.surfaceElevated,
    tertiaryContainer = MaaColors.cautionLight,
    onTertiaryContainer = MaaColors.textPrimary,

    error = MaaColors.danger,
    onError = MaaColors.surfaceElevated,
    errorContainer = MaaColors.dangerLight,
    onErrorContainer = MaaColors.danger,

    background = MaaColors.background,
    onBackground = MaaColors.textPrimary,

    surface = MaaColors.surface,
    onSurface = MaaColors.textPrimary,

    surfaceVariant = MaaColors.surfaceVariant,
    onSurfaceVariant = MaaColors.textSecondary,

    outline = MaaColors.border,
    outlineVariant = MaaColors.divider,

    scrim = MaaColors.textPrimary.copy(alpha = 0.32f)
)

/**
 * CompositionLocal for providing Maa-specific design tokens
 */
val LocalMaaColors = staticCompositionLocalOf { MaaColors }
val LocalMaaTypography = staticCompositionLocalOf { MaaTypography }
val LocalMaaShapes = staticCompositionLocalOf { MaaShapes }
val LocalMaaSpacing = staticCompositionLocalOf { MaaSpacing }

/**
 * Main theme composable for Maa app
 *
 * @param content The composable content to be themed
 */
@Composable
fun MaaTheme(
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // Set status bar color to match background
            window.statusBarColor = MaaColors.background.toArgb()
            // Set navigation bar color to match background
            window.navigationBarColor = MaaColors.background.toArgb()
            // Light status bar icons (dark icons on light background)
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = true
                isAppearanceLightNavigationBars = true
            }
        }
    }

    CompositionLocalProvider(
        LocalMaaColors provides MaaColors,
        LocalMaaTypography provides MaaTypography,
        LocalMaaShapes provides MaaShapes,
        LocalMaaSpacing provides MaaSpacing
    ) {
        MaterialTheme(
            colorScheme = MaaLightColorScheme,
            content = content
        )
    }
}

/**
 * Object to access Maa design tokens within composables
 */
object MaaTheme {
    val colors: MaaColors
        @Composable
        get() = LocalMaaColors.current

    val typography: MaaTypography
        @Composable
        get() = LocalMaaTypography.current

    val shapes: MaaShapes
        @Composable
        get() = LocalMaaShapes.current

    val spacing: MaaSpacing
        @Composable
        get() = LocalMaaSpacing.current
}

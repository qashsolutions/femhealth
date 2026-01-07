package com.maa.health.ui.theme

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.maa.health.R

/**
 * Maa Typography System
 *
 * Design Philosophy:
 * - Serif headings for dignity, trustworthiness, medical authority
 * - Sans-serif body for clarity, readability, modern feel
 * - Monospace for medical data, measurements, scores
 *
 * Font Families:
 * - Source Serif Pro: Headings (supports Devanagari)
 * - Source Sans Pro: Body text (supports Devanagari)
 * - JetBrains Mono: Data display
 */
object MaaTypography {

    // ═══════════════════════════════════════════════════════════════════════════
    // FONT FAMILIES
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Heading font family - Serif for medical authority
     * Uses system serif as fallback until custom fonts are added
     */
    val headingFamily = FontFamily.Serif

    /**
     * Body font family - Sans-serif for readability
     * Uses system sans-serif as fallback until custom fonts are added
     */
    val bodyFamily = FontFamily.SansSerif

    /**
     * Data font family - Monospace for measurements
     * Uses system monospace as fallback until custom fonts are added
     */
    val dataFamily = FontFamily.Monospace

    // ═══════════════════════════════════════════════════════════════════════════
    // DISPLAY STYLES - Major screens, welcome messages
    // ═══════════════════════════════════════════════════════════════════════════

    val displayLarge = TextStyle(
        fontFamily = headingFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = (-0.5).sp,
        color = MaaColors.textPrimary
    )

    val displayMedium = TextStyle(
        fontFamily = headingFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = (-0.25).sp,
        color = MaaColors.textPrimary
    )

    val displaySmall = TextStyle(
        fontFamily = headingFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        color = MaaColors.textPrimary
    )

    // ═══════════════════════════════════════════════════════════════════════════
    // HEADLINE STYLES - Section titles, screen titles
    // ═══════════════════════════════════════════════════════════════════════════

    val headlineLarge = TextStyle(
        fontFamily = headingFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        color = MaaColors.textPrimary
    )

    val headlineMedium = TextStyle(
        fontFamily = headingFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        color = MaaColors.textPrimary
    )

    val headlineSmall = TextStyle(
        fontFamily = headingFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 28.sp,
        color = MaaColors.textPrimary
    )

    // ═══════════════════════════════════════════════════════════════════════════
    // TITLE STYLES - Card titles, list headers
    // ═══════════════════════════════════════════════════════════════════════════

    val titleLarge = TextStyle(
        fontFamily = bodyFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 26.sp,
        color = MaaColors.textPrimary
    )

    val titleMedium = TextStyle(
        fontFamily = bodyFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        color = MaaColors.textPrimary
    )

    val titleSmall = TextStyle(
        fontFamily = bodyFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        color = MaaColors.textPrimary
    )

    // ═══════════════════════════════════════════════════════════════════════════
    // BODY STYLES - Main content
    // ═══════════════════════════════════════════════════════════════════════════

    val bodyLarge = TextStyle(
        fontFamily = bodyFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        color = MaaColors.textPrimary
    )

    val bodyMedium = TextStyle(
        fontFamily = bodyFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        color = MaaColors.textSecondary
    )

    val bodySmall = TextStyle(
        fontFamily = bodyFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        color = MaaColors.textSecondary
    )

    // ═══════════════════════════════════════════════════════════════════════════
    // LABEL STYLES - Buttons, chips, small UI elements
    // ═══════════════════════════════════════════════════════════════════════════

    val labelLarge = TextStyle(
        fontFamily = bodyFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp,
        color = MaaColors.textPrimary
    )

    val labelMedium = TextStyle(
        fontFamily = bodyFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp,
        color = MaaColors.textSecondary
    )

    val labelSmall = TextStyle(
        fontFamily = bodyFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 10.sp,
        lineHeight = 14.sp,
        letterSpacing = 0.5.sp,
        color = MaaColors.textTertiary
    )

    // ═══════════════════════════════════════════════════════════════════════════
    // DATA STYLES - Medical values, scores, measurements
    // ═══════════════════════════════════════════════════════════════════════════

    val dataLarge = TextStyle(
        fontFamily = dataFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        color = MaaColors.textPrimary
    )

    val dataMedium = TextStyle(
        fontFamily = dataFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        color = MaaColors.textPrimary
    )

    val dataSmall = TextStyle(
        fontFamily = dataFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        color = MaaColors.textSecondary
    )

    // ═══════════════════════════════════════════════════════════════════════════
    // SPECIAL STYLES
    // ═══════════════════════════════════════════════════════════════════════════

    /** For button text */
    val button = TextStyle(
        fontFamily = bodyFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.1.sp
    )

    /** For input fields */
    val input = TextStyle(
        fontFamily = bodyFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        color = MaaColors.textPrimary
    )

    /** For input hints/placeholders */
    val inputHint = TextStyle(
        fontFamily = bodyFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        color = MaaColors.textTertiary
    )

    /** For OTP/PIN input digits */
    val otpDigit = TextStyle(
        fontFamily = dataFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        color = MaaColors.textPrimary
    )

    /** For countdown timers */
    val timer = TextStyle(
        fontFamily = dataFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 48.sp,
        lineHeight = 56.sp,
        color = MaaColors.textPrimary
    )

    /** For kick/breath counters */
    val counter = TextStyle(
        fontFamily = dataFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 64.sp,
        lineHeight = 72.sp,
        color = MaaColors.textPrimary
    )

    /** For urgency/alert text */
    val urgency = TextStyle(
        fontFamily = bodyFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        lineHeight = 26.sp
    )
}

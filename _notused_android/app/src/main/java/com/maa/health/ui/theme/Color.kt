package com.maa.health.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Maa Color System
 *
 * Design Philosophy:
 * - Warm colors over cold (earth tones, not clinical blues)
 * - NO pure white backgrounds
 * - NO neon colors
 * - Medical textbook quality, not consumer wellness app
 */
object MaaColors {

    // ═══════════════════════════════════════════════════════════════════════════
    // BACKGROUNDS (Warm, not clinical white)
    // ═══════════════════════════════════════════════════════════════════════════

    /** Warm cream - primary background */
    val background = Color(0xFFFAF7F2)

    /** Warm bone - cards, panels */
    val surface = Color(0xFFF5F0EB)

    /** Warm linen - secondary surfaces */
    val surfaceVariant = Color(0xFFEDE6DD)

    /** Pure white - modals only */
    val surfaceElevated = Color(0xFFFFFFFF)

    // ═══════════════════════════════════════════════════════════════════════════
    // TEXT (Warm blacks and grays, never pure black)
    // ═══════════════════════════════════════════════════════════════════════════

    /** Warm charcoal - headings */
    val textPrimary = Color(0xFF2D2926)

    /** Warm gray - body text */
    val textSecondary = Color(0xFF5C5550)

    /** Warm silver - captions */
    val textTertiary = Color(0xFF8A837C)

    /** Light warm gray - disabled text */
    val textDisabled = Color(0xFFB8B2AB)

    // ═══════════════════════════════════════════════════════════════════════════
    // SVG BODY ILLUSTRATION COLORS
    // ═══════════════════════════════════════════════════════════════════════════

    /** Warm skin tone - body fill */
    val bodyFill = Color(0xFFF5EDE5)

    /** Warm charcoal - outlines */
    val bodyStroke = Color(0xFF3D3632)

    /** Light stroke for details */
    val bodyStrokeLight = Color(0xFF8A837C)

    /** Slightly darker for organs */
    val organFill = Color(0xFFE8DFD4)

    /** Highlighted organ */
    val organHighlight = Color(0xFFD4C4B0)

    // ═══════════════════════════════════════════════════════════════════════════
    // SEMANTIC COLORS (Warm versions of standard colors)
    // ═══════════════════════════════════════════════════════════════════════════

    /** Forest green - healthy/normal */
    val safe = Color(0xFF4A7C59)

    /** Light green background */
    val safeLight = Color(0xFFE8F0EA)

    /** Warm slate blue - informational */
    val info = Color(0xFF5B7B8C)

    /** Light blue background */
    val infoLight = Color(0xFFE8EEF1)

    /** Amber gold - needs attention */
    val caution = Color(0xFFD4A84B)

    /** Light amber background */
    val cautionLight = Color(0xFFF8F3E6)

    /** Terracotta - concerning */
    val warning = Color(0xFFCC7B4A)

    /** Light terracotta background */
    val warningLight = Color(0xFFFAF0EB)

    /** Brick red - urgent/emergency */
    val danger = Color(0xFFB54D4D)

    /** Light red background */
    val dangerLight = Color(0xFFFAEBEB)

    // ═══════════════════════════════════════════════════════════════════════════
    // ACCENT & INTERACTIVE
    // ═══════════════════════════════════════════════════════════════════════════

    /** Warm taupe - primary accent */
    val accent = Color(0xFF8B6F5C)

    /** Sand - secondary accent */
    val accentLight = Color(0xFFC4A98C)

    /** Dark taupe - pressed states */
    val accentDark = Color(0xFF5C4A3D)

    /** 10% taupe - ripple effect */
    val touchFeedback = Color(0x1A8B6F5C)

    /** 20% amber - selected body region */
    val selectedRegion = Color(0x33D4A84B)

    /** 30% taupe - active/focused */
    val activeRegion = Color(0x4D8B6F5C)

    // ═══════════════════════════════════════════════════════════════════════════
    // LIFECYCLE STAGE COLORS (Subtle differentiation)
    // ═══════════════════════════════════════════════════════════════════════════

    /** Soft peach - Adolescence (13-19) */
    val stageAdolescence = Color(0xFFE8D4C8)

    /** Warm sand - Reproductive (18-40) */
    val stageReproductive = Color(0xFFD4C4B0)

    /** Cream rose - Pregnancy */
    val stagePregnancy = Color(0xFFE2D5C7)

    /** Warm dove - Postpartum */
    val stagePostpartum = Color(0xFFD8CFC4)

    /** Soft sage - Child Care */
    val stageChildCare = Color(0xFFDCE4D8)

    /** Warm stone - Midlife (40-55) */
    val stageMidlife = Color(0xFFD4D0C8)

    /** Silver birch - Elder (55+) */
    val stageElder = Color(0xFFE0DCD6)

    // ═══════════════════════════════════════════════════════════════════════════
    // URGENCY COLORS (For triage results)
    // ═══════════════════════════════════════════════════════════════════════════

    /** Emergency - immediate action required */
    val urgencyEmergency = danger

    /** Urgent - action needed today */
    val urgencyUrgent = warning

    /** Routine - schedule appointment */
    val urgencyRoutine = caution

    /** Self-care - home management */
    val urgencySelfCare = safe

    // ═══════════════════════════════════════════════════════════════════════════
    // DIVIDERS AND BORDERS
    // ═══════════════════════════════════════════════════════════════════════════

    /** Subtle divider line */
    val divider = Color(0xFFE5DFD8)

    /** Border for input fields */
    val border = Color(0xFFD4CEC6)

    /** Focused border */
    val borderFocused = accent
}

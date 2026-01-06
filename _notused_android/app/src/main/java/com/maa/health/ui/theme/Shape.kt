package com.maa.health.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

/**
 * Maa Shape System
 *
 * Design Philosophy:
 * - Subtle, refined corners - NOT rounded bubbly UI
 * - Medical/professional aesthetic
 * - Consistent across all components
 */
object MaaShapes {

    // ═══════════════════════════════════════════════════════════════════════════
    // CORNER RADII
    // ═══════════════════════════════════════════════════════════════════════════

    /** No rounding - for data displays, tables */
    val none = RoundedCornerShape(0.dp)

    /** Minimal rounding - for inline elements */
    val extraSmall = RoundedCornerShape(4.dp)

    /** Small rounding - for chips, tags */
    val small = RoundedCornerShape(8.dp)

    /** Medium rounding - for cards, buttons */
    val medium = RoundedCornerShape(12.dp)

    /** Large rounding - for modals, bottom sheets */
    val large = RoundedCornerShape(16.dp)

    /** Extra large - for floating action elements */
    val extraLarge = RoundedCornerShape(24.dp)

    /** Pill shape - for special buttons, chips */
    val pill = RoundedCornerShape(50)

    // ═══════════════════════════════════════════════════════════════════════════
    // COMPONENT-SPECIFIC SHAPES
    // ═══════════════════════════════════════════════════════════════════════════

    /** For standard buttons */
    val button = medium

    /** For icon buttons */
    val iconButton = small

    /** For cards */
    val card = medium

    /** For input fields */
    val input = small

    /** For bottom sheets */
    val bottomSheet = RoundedCornerShape(
        topStart = 24.dp,
        topEnd = 24.dp,
        bottomStart = 0.dp,
        bottomEnd = 0.dp
    )

    /** For dialogs */
    val dialog = large

    /** For mood/symptom selection items */
    val selectionItem = small

    /** For body region highlights */
    val bodyRegion = extraSmall

    /** For navigation bar items */
    val navItem = small

    /** For OTP input boxes */
    val otpBox = small

    /** For number pad keys */
    val numberPadKey = medium

    /** For quick action cards */
    val actionCard = medium

    /** For alert/warning cards */
    val alertCard = medium

    /** For floating elements */
    val floating = large
}

/**
 * Standard spacing values for consistent layouts
 */
object MaaSpacing {

    /** 4dp - Minimal spacing */
    val xxs = 4.dp

    /** 8dp - Tight spacing */
    val xs = 8.dp

    /** 12dp - Compact spacing */
    val sm = 12.dp

    /** 16dp - Standard spacing */
    val md = 16.dp

    /** 20dp - Comfortable spacing */
    val lg = 20.dp

    /** 24dp - Generous spacing */
    val xl = 24.dp

    /** 32dp - Section spacing */
    val xxl = 32.dp

    /** 48dp - Major section spacing */
    val xxxl = 48.dp

    // Screen edge padding
    val screenHorizontal = 24.dp
    val screenVertical = 16.dp

    // Card internal padding
    val cardPadding = 16.dp

    // Button internal padding
    val buttonPaddingHorizontal = 24.dp
    val buttonPaddingVertical = 14.dp

    // Icon sizes
    val iconSmall = 16.dp
    val iconMedium = 24.dp
    val iconLarge = 32.dp
    val iconXLarge = 48.dp

    // Touch target minimum (accessibility)
    val touchTarget = 48.dp
}

/**
 * Standard elevation values
 */
object MaaElevation {

    /** No elevation - flat elements */
    val none = 0.dp

    /** Subtle elevation - cards on surface */
    val low = 1.dp

    /** Medium elevation - floating cards */
    val medium = 4.dp

    /** High elevation - modals, dialogs */
    val high = 8.dp

    /** Maximum elevation - overlays */
    val overlay = 16.dp
}

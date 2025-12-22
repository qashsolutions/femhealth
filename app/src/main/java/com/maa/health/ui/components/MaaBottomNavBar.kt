package com.maa.health.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.maa.health.ui.theme.MaaColors

/**
 * Navigation items for bottom navigation bar
 */
enum class NavItem(
    val route: String,
    val label: String,
    val labelHindi: String
) {
    HOME("home", "Home", "होम"),
    TRACK("track", "Track", "ट्रैक"),
    LEARN("learn", "Learn", "सीखें"),
    CARE("care", "Care", "देखभाल"),
    YOU("you", "You", "आप")
}

/**
 * Premium Bottom Navigation Bar
 *
 * Flagship quality navigation with custom SVG-style icons,
 * smooth animations, and accessibility support.
 */
@Composable
fun MaaBottomNavBar(
    currentRoute: String,
    onNavigate: (NavItem) -> Unit,
    modifier: Modifier = Modifier,
    showHindiLabels: Boolean = false
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaaColors.surface,
        shadowElevation = 16.dp,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        // Subtle top gradient line
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                MaaColors.stagePregnancy.copy(alpha = 0.3f),
                                MaaColors.accent.copy(alpha = 0.5f),
                                MaaColors.stageChildCare.copy(alpha = 0.3f)
                            )
                        )
                    )
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                NavItem.entries.forEach { item ->
                    NavBarItem(
                        item = item,
                        isSelected = currentRoute == item.route,
                        onClick = { onNavigate(item) },
                        showHindiLabel = showHindiLabels
                    )
                }
            }
        }
    }
}

@Composable
private fun NavBarItem(
    item: NavItem,
    isSelected: Boolean,
    onClick: () -> Unit,
    showHindiLabel: Boolean
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.1f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )

    val iconColor by animateColorAsState(
        targetValue = if (isSelected) MaaColors.accent else MaaColors.textTertiary,
        label = "iconColor"
    )

    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) MaaColors.accent.copy(alpha = 0.12f) else Color.Transparent,
        label = "backgroundColor"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { onClick() }
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .scale(scale),
            contentAlignment = Alignment.Center
        ) {
            NavIcon(
                item = item,
                color = iconColor,
                isSelected = isSelected
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = if (showHindiLabel) item.labelHindi else item.label,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (isSelected) MaaColors.accent else MaaColors.textTertiary
        )
    }
}

/**
 * Custom SVG-style navigation icons
 * Hand-crafted for premium look
 */
@Composable
private fun NavIcon(
    item: NavItem,
    color: Color,
    isSelected: Boolean
) {
    Canvas(modifier = Modifier.size(24.dp)) {
        val strokeWidth = if (isSelected) 2.2f else 1.8f
        when (item) {
            NavItem.HOME -> drawHomeIcon(color, strokeWidth, isSelected)
            NavItem.TRACK -> drawTrackIcon(color, strokeWidth, isSelected)
            NavItem.LEARN -> drawLearnIcon(color, strokeWidth, isSelected)
            NavItem.CARE -> drawCareIcon(color, strokeWidth, isSelected)
            NavItem.YOU -> drawYouIcon(color, strokeWidth, isSelected)
        }
    }
}

/**
 * Home icon - House with heart inside
 * Represents the welcoming dashboard
 */
private fun DrawScope.drawHomeIcon(color: Color, strokeWidth: Float, filled: Boolean) {
    val w = size.width
    val h = size.height

    // House outline
    val housePath = Path().apply {
        // Roof peak
        moveTo(w * 0.5f, h * 0.1f)
        lineTo(w * 0.9f, h * 0.4f)
        lineTo(w * 0.9f, h * 0.9f)
        lineTo(w * 0.1f, h * 0.9f)
        lineTo(w * 0.1f, h * 0.4f)
        close()
    }

    if (filled) {
        drawPath(housePath, color.copy(alpha = 0.15f))
    }
    drawPath(housePath, color, style = Stroke(strokeWidth.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))

    // Small heart inside
    val heartPath = Path().apply {
        val cx = w * 0.5f
        val cy = h * 0.58f
        val heartSize = w * 0.18f

        moveTo(cx, cy + heartSize * 0.8f)
        cubicTo(
            cx - heartSize * 1.2f, cy - heartSize * 0.2f,
            cx - heartSize * 0.6f, cy - heartSize * 1.0f,
            cx, cy - heartSize * 0.3f
        )
        cubicTo(
            cx + heartSize * 0.6f, cy - heartSize * 1.0f,
            cx + heartSize * 1.2f, cy - heartSize * 0.2f,
            cx, cy + heartSize * 0.8f
        )
        close()
    }

    if (filled) {
        drawPath(heartPath, color)
    } else {
        drawPath(heartPath, color, style = Stroke(strokeWidth.dp.toPx() * 0.8f, cap = StrokeCap.Round, join = StrokeJoin.Round))
    }
}

/**
 * Track icon - Calendar with pulse/heartbeat line
 * Represents health tracking and logging
 */
private fun DrawScope.drawTrackIcon(color: Color, strokeWidth: Float, filled: Boolean) {
    val w = size.width
    val h = size.height

    // Calendar base
    val calendarPath = Path().apply {
        moveTo(w * 0.15f, h * 0.25f)
        lineTo(w * 0.85f, h * 0.25f)
        lineTo(w * 0.85f, h * 0.9f)
        lineTo(w * 0.15f, h * 0.9f)
        close()
    }

    if (filled) {
        drawPath(calendarPath, color.copy(alpha = 0.15f))
    }
    drawPath(calendarPath, color, style = Stroke(strokeWidth.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))

    // Calendar tabs
    drawLine(
        color = color,
        start = Offset(w * 0.35f, h * 0.12f),
        end = Offset(w * 0.35f, h * 0.32f),
        strokeWidth = strokeWidth.dp.toPx(),
        cap = StrokeCap.Round
    )
    drawLine(
        color = color,
        start = Offset(w * 0.65f, h * 0.12f),
        end = Offset(w * 0.65f, h * 0.32f),
        strokeWidth = strokeWidth.dp.toPx(),
        cap = StrokeCap.Round
    )

    // Pulse line inside calendar
    val pulsePath = Path().apply {
        moveTo(w * 0.2f, h * 0.6f)
        lineTo(w * 0.35f, h * 0.6f)
        lineTo(w * 0.42f, h * 0.45f)
        lineTo(w * 0.5f, h * 0.75f)
        lineTo(w * 0.58f, h * 0.5f)
        lineTo(w * 0.65f, h * 0.6f)
        lineTo(w * 0.8f, h * 0.6f)
    }
    drawPath(pulsePath, color, style = Stroke(strokeWidth.dp.toPx() * 0.9f, cap = StrokeCap.Round, join = StrokeJoin.Round))
}

/**
 * Learn icon - Open book with lightbulb
 * Represents education and knowledge
 */
private fun DrawScope.drawLearnIcon(color: Color, strokeWidth: Float, filled: Boolean) {
    val w = size.width
    val h = size.height

    // Open book - left page
    val leftPage = Path().apply {
        moveTo(w * 0.5f, h * 0.25f)
        quadraticTo(w * 0.25f, h * 0.22f, w * 0.1f, h * 0.3f)
        lineTo(w * 0.1f, h * 0.85f)
        quadraticTo(w * 0.25f, h * 0.78f, w * 0.5f, h * 0.8f)
        close()
    }

    // Open book - right page
    val rightPage = Path().apply {
        moveTo(w * 0.5f, h * 0.25f)
        quadraticTo(w * 0.75f, h * 0.22f, w * 0.9f, h * 0.3f)
        lineTo(w * 0.9f, h * 0.85f)
        quadraticTo(w * 0.75f, h * 0.78f, w * 0.5f, h * 0.8f)
        close()
    }

    if (filled) {
        drawPath(leftPage, color.copy(alpha = 0.15f))
        drawPath(rightPage, color.copy(alpha = 0.15f))
    }
    drawPath(leftPage, color, style = Stroke(strokeWidth.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
    drawPath(rightPage, color, style = Stroke(strokeWidth.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))

    // Book spine
    drawLine(
        color = color,
        start = Offset(w * 0.5f, h * 0.25f),
        end = Offset(w * 0.5f, h * 0.8f),
        strokeWidth = strokeWidth.dp.toPx(),
        cap = StrokeCap.Round
    )

    // Small lightbulb/star at top
    val starCx = w * 0.5f
    val starCy = h * 0.12f
    val starSize = w * 0.08f
    if (filled) {
        drawCircle(color, starSize, Offset(starCx, starCy))
    } else {
        drawCircle(color, starSize, Offset(starCx, starCy), style = Stroke(strokeWidth.dp.toPx() * 0.8f))
    }
    // Light rays
    for (i in 0 until 4) {
        val angle = (i * 90 + 45) * (Math.PI / 180).toFloat()
        val rayStart = starSize * 1.3f
        val rayEnd = starSize * 2f
        drawLine(
            color = color,
            start = Offset(starCx + kotlin.math.cos(angle) * rayStart, starCy + kotlin.math.sin(angle) * rayStart),
            end = Offset(starCx + kotlin.math.cos(angle) * rayEnd, starCy + kotlin.math.sin(angle) * rayEnd),
            strokeWidth = strokeWidth.dp.toPx() * 0.6f,
            cap = StrokeCap.Round
        )
    }
}

/**
 * Care icon - Hands cradling/holding (nurturing)
 * Represents child care and family health
 */
private fun DrawScope.drawCareIcon(color: Color, strokeWidth: Float, filled: Boolean) {
    val w = size.width
    val h = size.height

    // Left hand curve
    val leftHand = Path().apply {
        moveTo(w * 0.1f, h * 0.5f)
        quadraticTo(w * 0.15f, h * 0.85f, w * 0.5f, h * 0.9f)
    }

    // Right hand curve
    val rightHand = Path().apply {
        moveTo(w * 0.9f, h * 0.5f)
        quadraticTo(w * 0.85f, h * 0.85f, w * 0.5f, h * 0.9f)
    }

    drawPath(leftHand, color, style = Stroke(strokeWidth.dp.toPx() * 1.2f, cap = StrokeCap.Round, join = StrokeJoin.Round))
    drawPath(rightHand, color, style = Stroke(strokeWidth.dp.toPx() * 1.2f, cap = StrokeCap.Round, join = StrokeJoin.Round))

    // Small figure/heart being held
    val figurePath = Path().apply {
        // Head
        val headCy = h * 0.35f
        val headRadius = w * 0.12f

        // Body as a simple curved shape
        moveTo(w * 0.5f, h * 0.75f)
        cubicTo(
            w * 0.25f, h * 0.65f,
            w * 0.3f, h * 0.4f,
            w * 0.5f, h * 0.35f
        )
        cubicTo(
            w * 0.7f, h * 0.4f,
            w * 0.75f, h * 0.65f,
            w * 0.5f, h * 0.75f
        )
        close()
    }

    if (filled) {
        drawPath(figurePath, color)
        drawCircle(color, w * 0.1f, Offset(w * 0.5f, h * 0.25f))
    } else {
        drawPath(figurePath, color, style = Stroke(strokeWidth.dp.toPx() * 0.9f, cap = StrokeCap.Round, join = StrokeJoin.Round))
        drawCircle(color, w * 0.1f, Offset(w * 0.5f, h * 0.25f), style = Stroke(strokeWidth.dp.toPx() * 0.9f))
    }
}

/**
 * You icon - Person silhouette with gear
 * Represents profile and settings
 */
private fun DrawScope.drawYouIcon(color: Color, strokeWidth: Float, filled: Boolean) {
    val w = size.width
    val h = size.height

    // Person head
    val headCx = w * 0.4f
    val headCy = h * 0.28f
    val headRadius = w * 0.15f

    if (filled) {
        drawCircle(color, headRadius, Offset(headCx, headCy))
    } else {
        drawCircle(color, headRadius, Offset(headCx, headCy), style = Stroke(strokeWidth.dp.toPx()))
    }

    // Person body (shoulders arc)
    val bodyPath = Path().apply {
        moveTo(w * 0.08f, h * 0.85f)
        quadraticTo(w * 0.4f, h * 0.42f, w * 0.72f, h * 0.85f)
    }

    if (filled) {
        val bodyFillPath = Path().apply {
            moveTo(w * 0.08f, h * 0.85f)
            quadraticTo(w * 0.4f, h * 0.42f, w * 0.72f, h * 0.85f)
            lineTo(w * 0.72f, h * 0.9f)
            lineTo(w * 0.08f, h * 0.9f)
            close()
        }
        drawPath(bodyFillPath, color.copy(alpha = 0.15f))
    }
    drawPath(bodyPath, color, style = Stroke(strokeWidth.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))

    // Small gear at bottom-right
    val gearCx = w * 0.78f
    val gearCy = h * 0.72f
    val gearRadius = w * 0.14f
    val teethCount = 6

    // Gear teeth
    for (i in 0 until teethCount) {
        val angle = (i * 360f / teethCount) * (Math.PI / 180f).toFloat()
        val innerR = gearRadius * 0.6f
        val outerR = gearRadius
        drawLine(
            color = color,
            start = Offset(
                gearCx + kotlin.math.cos(angle) * innerR,
                gearCy + kotlin.math.sin(angle) * innerR
            ),
            end = Offset(
                gearCx + kotlin.math.cos(angle) * outerR,
                gearCy + kotlin.math.sin(angle) * outerR
            ),
            strokeWidth = strokeWidth.dp.toPx() * 1.1f,
            cap = StrokeCap.Round
        )
    }

    // Gear center
    if (filled) {
        drawCircle(color, gearRadius * 0.4f, Offset(gearCx, gearCy))
    } else {
        drawCircle(color, gearRadius * 0.4f, Offset(gearCx, gearCy), style = Stroke(strokeWidth.dp.toPx() * 0.8f))
    }
    drawCircle(color.copy(alpha = 0.5f), gearRadius * 0.15f, Offset(gearCx, gearCy))
}

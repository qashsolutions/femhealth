package com.maa.health.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.maa.health.data.model.BodyRegion
import com.maa.health.ui.theme.MaaColors

/**
 * Body view mode for different perspectives
 */
enum class BodyViewMode {
    FRONT,
    BACK,
    SIDE
}

/**
 * Body type for different silhouettes
 */
enum class BodyType {
    ADULT_FEMALE,
    CHILD,
    PREGNANT
}

/**
 * Interactive body region with touch detection
 */
data class InteractiveRegion(
    val region: BodyRegion,
    val bounds: Rect,
    val path: Path? = null,
    val label: String
)

/**
 * Interactive SVG-style body map component
 *
 * Features:
 * - Touch detection for body regions
 * - Visual feedback on selection
 * - Different body types (female adult, pregnant, child)
 * - Front/back views
 * - Warm color palette per design system
 */
@Composable
fun InteractiveBodySvg(
    modifier: Modifier = Modifier,
    bodyType: BodyType = BodyType.ADULT_FEMALE,
    viewMode: BodyViewMode = BodyViewMode.FRONT,
    selectedRegion: BodyRegion? = null,
    highlightedRegions: Set<BodyRegion> = emptySet(),
    dangerRegions: Set<BodyRegion> = emptySet(),
    onRegionTap: (BodyRegion) -> Unit
) {
    var tappedRegion by remember { mutableStateOf<BodyRegion?>(null) }

    val regions = remember(bodyType, viewMode) {
        getRegionsForBodyType(bodyType, viewMode)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(0.45f)  // Body proportions
            .padding(16.dp)
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(regions) {
                    detectTapGestures { offset ->
                        val tapped = findRegionAtPoint(regions, offset, size)
                        tapped?.let {
                            tappedRegion = it.region
                            onRegionTap(it.region)
                        }
                    }
                }
        ) {
            val canvasWidth = size.width
            val canvasHeight = size.height

            // Draw body outline
            drawBodyOutline(bodyType, viewMode, canvasWidth, canvasHeight)

            // Draw regions with appropriate colors
            regions.forEach { region ->
                val isSelected = selectedRegion == region.region
                val isHighlighted = region.region in highlightedRegions
                val isDanger = region.region in dangerRegions
                val isTapped = tappedRegion == region.region

                val fillColor = when {
                    isDanger -> MaaColors.dangerLight
                    isSelected -> MaaColors.selectedRegion
                    isHighlighted -> MaaColors.cautionLight
                    isTapped -> MaaColors.touchFeedback
                    else -> Color.Transparent
                }

                val strokeColor = when {
                    isDanger -> MaaColors.danger
                    isSelected -> MaaColors.accent
                    isHighlighted -> MaaColors.caution
                    else -> MaaColors.bodyStrokeLight
                }

                drawRegion(region, canvasWidth, canvasHeight, fillColor, strokeColor)
            }
        }
    }
}

/**
 * Draw the main body outline
 */
private fun DrawScope.drawBodyOutline(
    bodyType: BodyType,
    viewMode: BodyViewMode,
    width: Float,
    height: Float
) {
    val outlinePath = when (bodyType) {
        BodyType.ADULT_FEMALE -> createAdultFemalePath(width, height, viewMode)
        BodyType.PREGNANT -> createPregnantPath(width, height, viewMode)
        BodyType.CHILD -> createChildPath(width, height, viewMode)
    }

    // Fill with body color
    drawPath(
        path = outlinePath,
        color = MaaColors.bodyFill,
        style = Fill
    )

    // Draw outline
    drawPath(
        path = outlinePath,
        color = MaaColors.bodyStroke,
        style = Stroke(width = 2.dp.toPx())
    )
}

/**
 * Draw an interactive region
 */
private fun DrawScope.drawRegion(
    region: InteractiveRegion,
    canvasWidth: Float,
    canvasHeight: Float,
    fillColor: Color,
    strokeColor: Color
) {
    val scaledBounds = Rect(
        left = region.bounds.left * canvasWidth,
        top = region.bounds.top * canvasHeight,
        right = region.bounds.right * canvasWidth,
        bottom = region.bounds.bottom * canvasHeight
    )

    if (region.path != null) {
        // Use custom path
        drawPath(
            path = region.path,
            color = fillColor,
            style = Fill
        )
        drawPath(
            path = region.path,
            color = strokeColor,
            style = Stroke(width = 1.dp.toPx())
        )
    } else {
        // Use bounds as oval
        drawOval(
            color = fillColor,
            topLeft = Offset(scaledBounds.left, scaledBounds.top),
            size = Size(scaledBounds.width, scaledBounds.height)
        )
        drawOval(
            color = strokeColor,
            topLeft = Offset(scaledBounds.left, scaledBounds.top),
            size = Size(scaledBounds.width, scaledBounds.height),
            style = Stroke(width = 1.dp.toPx())
        )
    }
}

/**
 * Find which region contains the tap point
 */
private fun findRegionAtPoint(
    regions: List<InteractiveRegion>,
    point: Offset,
    size: androidx.compose.ui.unit.IntSize
): InteractiveRegion? {
    val normalizedX = point.x / size.width
    val normalizedY = point.y / size.height

    return regions.find { region ->
        normalizedX >= region.bounds.left &&
        normalizedX <= region.bounds.right &&
        normalizedY >= region.bounds.top &&
        normalizedY <= region.bounds.bottom
    }
}

/**
 * Get interactive regions for body type
 */
private fun getRegionsForBodyType(
    bodyType: BodyType,
    viewMode: BodyViewMode
): List<InteractiveRegion> {
    return when (bodyType) {
        BodyType.ADULT_FEMALE -> getAdultFemaleRegions(viewMode)
        BodyType.PREGNANT -> getPregnantRegions(viewMode)
        BodyType.CHILD -> getChildRegions(viewMode)
    }
}

/**
 * Adult female body regions
 */
private fun getAdultFemaleRegions(viewMode: BodyViewMode): List<InteractiveRegion> {
    return when (viewMode) {
        BodyViewMode.FRONT -> listOf(
            InteractiveRegion(
                region = BodyRegion.HEAD,
                bounds = Rect(0.35f, 0.02f, 0.65f, 0.12f),
                label = "Head"
            ),
            InteractiveRegion(
                region = BodyRegion.EYES,
                bounds = Rect(0.40f, 0.05f, 0.60f, 0.08f),
                label = "Eyes"
            ),
            InteractiveRegion(
                region = BodyRegion.MOUTH,
                bounds = Rect(0.42f, 0.08f, 0.58f, 0.11f),
                label = "Mouth"
            ),
            InteractiveRegion(
                region = BodyRegion.CHEST,
                bounds = Rect(0.30f, 0.18f, 0.70f, 0.32f),
                label = "Chest"
            ),
            InteractiveRegion(
                region = BodyRegion.ABDOMEN_UPPER,
                bounds = Rect(0.32f, 0.32f, 0.68f, 0.42f),
                label = "Upper Abdomen"
            ),
            InteractiveRegion(
                region = BodyRegion.ABDOMEN,
                bounds = Rect(0.32f, 0.42f, 0.68f, 0.52f),
                label = "Abdomen"
            ),
            InteractiveRegion(
                region = BodyRegion.PELVIS,
                bounds = Rect(0.30f, 0.52f, 0.70f, 0.62f),
                label = "Pelvis"
            ),
            InteractiveRegion(
                region = BodyRegion.HANDS,
                bounds = Rect(0.05f, 0.42f, 0.25f, 0.55f),
                label = "Hands"
            ),
            InteractiveRegion(
                region = BodyRegion.HANDS,
                bounds = Rect(0.75f, 0.42f, 0.95f, 0.55f),
                label = "Hands"
            ),
            InteractiveRegion(
                region = BodyRegion.FEET,
                bounds = Rect(0.28f, 0.88f, 0.42f, 0.98f),
                label = "Feet"
            ),
            InteractiveRegion(
                region = BodyRegion.FEET,
                bounds = Rect(0.58f, 0.88f, 0.72f, 0.98f),
                label = "Feet"
            ),
            InteractiveRegion(
                region = BodyRegion.SKIN,
                bounds = Rect(0.15f, 0.20f, 0.85f, 0.85f),
                label = "Skin"
            )
        )
        BodyViewMode.BACK -> listOf(
            InteractiveRegion(
                region = BodyRegion.HEAD,
                bounds = Rect(0.35f, 0.02f, 0.65f, 0.12f),
                label = "Head"
            ),
            InteractiveRegion(
                region = BodyRegion.JOINTS,
                bounds = Rect(0.30f, 0.14f, 0.70f, 0.18f),
                label = "Neck/Shoulders"
            ),
            InteractiveRegion(
                region = BodyRegion.JOINTS,
                bounds = Rect(0.32f, 0.28f, 0.68f, 0.45f),
                label = "Back"
            ),
            InteractiveRegion(
                region = BodyRegion.PELVIS,
                bounds = Rect(0.32f, 0.45f, 0.68f, 0.58f),
                label = "Lower Back"
            )
        )
        BodyViewMode.SIDE -> getAdultFemaleRegions(BodyViewMode.FRONT)  // Simplified
    }
}

/**
 * Pregnant body regions
 */
private fun getPregnantRegions(viewMode: BodyViewMode): List<InteractiveRegion> {
    val baseRegions = getAdultFemaleRegions(viewMode).toMutableList()

    // Add uterus region for pregnant body
    if (viewMode == BodyViewMode.FRONT) {
        baseRegions.add(
            InteractiveRegion(
                region = BodyRegion.UTERUS,
                bounds = Rect(0.30f, 0.38f, 0.70f, 0.58f),
                label = "Uterus"
            )
        )
    }

    return baseRegions
}

/**
 * Child body regions
 */
private fun getChildRegions(viewMode: BodyViewMode): List<InteractiveRegion> {
    return when (viewMode) {
        BodyViewMode.FRONT -> listOf(
            InteractiveRegion(
                region = BodyRegion.HEAD,
                bounds = Rect(0.30f, 0.02f, 0.70f, 0.18f),  // Larger head proportion
                label = "Head"
            ),
            InteractiveRegion(
                region = BodyRegion.EYES,
                bounds = Rect(0.35f, 0.06f, 0.65f, 0.10f),
                label = "Eyes"
            ),
            InteractiveRegion(
                region = BodyRegion.MOUTH,
                bounds = Rect(0.40f, 0.12f, 0.60f, 0.15f),
                label = "Mouth"
            ),
            InteractiveRegion(
                region = BodyRegion.CHEST,
                bounds = Rect(0.32f, 0.22f, 0.68f, 0.38f),
                label = "Chest"
            ),
            InteractiveRegion(
                region = BodyRegion.ABDOMEN,
                bounds = Rect(0.34f, 0.38f, 0.66f, 0.52f),
                label = "Tummy"
            ),
            InteractiveRegion(
                region = BodyRegion.DIAPER_AREA,
                bounds = Rect(0.35f, 0.52f, 0.65f, 0.62f),
                label = "Diaper Area"
            ),
            InteractiveRegion(
                region = BodyRegion.SKIN,
                bounds = Rect(0.20f, 0.20f, 0.80f, 0.85f),
                label = "Skin"
            )
        )
        else -> getChildRegions(BodyViewMode.FRONT)
    }
}

/**
 * Create adult female body path
 */
private fun createAdultFemalePath(width: Float, height: Float, viewMode: BodyViewMode): Path {
    return Path().apply {
        // Head
        addOval(
            Rect(
                left = width * 0.38f,
                top = height * 0.02f,
                right = width * 0.62f,
                bottom = height * 0.12f
            )
        )

        // Neck
        moveTo(width * 0.45f, height * 0.12f)
        lineTo(width * 0.45f, height * 0.15f)
        lineTo(width * 0.55f, height * 0.15f)
        lineTo(width * 0.55f, height * 0.12f)

        // Torso
        moveTo(width * 0.30f, height * 0.15f)
        lineTo(width * 0.30f, height * 0.55f)
        quadraticBezierTo(width * 0.35f, height * 0.58f, width * 0.50f, height * 0.58f)
        quadraticBezierTo(width * 0.65f, height * 0.58f, width * 0.70f, height * 0.55f)
        lineTo(width * 0.70f, height * 0.15f)
        close()

        // Left arm
        moveTo(width * 0.30f, height * 0.16f)
        lineTo(width * 0.15f, height * 0.22f)
        lineTo(width * 0.10f, height * 0.45f)
        lineTo(width * 0.15f, height * 0.45f)
        lineTo(width * 0.18f, height * 0.25f)
        lineTo(width * 0.30f, height * 0.20f)
        close()

        // Right arm
        moveTo(width * 0.70f, height * 0.16f)
        lineTo(width * 0.85f, height * 0.22f)
        lineTo(width * 0.90f, height * 0.45f)
        lineTo(width * 0.85f, height * 0.45f)
        lineTo(width * 0.82f, height * 0.25f)
        lineTo(width * 0.70f, height * 0.20f)
        close()

        // Left leg
        moveTo(width * 0.35f, height * 0.58f)
        lineTo(width * 0.30f, height * 0.95f)
        lineTo(width * 0.42f, height * 0.95f)
        lineTo(width * 0.48f, height * 0.58f)
        close()

        // Right leg
        moveTo(width * 0.52f, height * 0.58f)
        lineTo(width * 0.58f, height * 0.95f)
        lineTo(width * 0.70f, height * 0.95f)
        lineTo(width * 0.65f, height * 0.58f)
        close()
    }
}

/**
 * Create pregnant body path
 */
private fun createPregnantPath(width: Float, height: Float, viewMode: BodyViewMode): Path {
    return Path().apply {
        // Head
        addOval(
            Rect(
                left = width * 0.38f,
                top = height * 0.02f,
                right = width * 0.62f,
                bottom = height * 0.12f
            )
        )

        // Neck
        moveTo(width * 0.45f, height * 0.12f)
        lineTo(width * 0.45f, height * 0.15f)
        lineTo(width * 0.55f, height * 0.15f)
        lineTo(width * 0.55f, height * 0.12f)

        // Torso with pregnant belly
        moveTo(width * 0.30f, height * 0.15f)
        lineTo(width * 0.30f, height * 0.35f)
        quadraticBezierTo(width * 0.25f, height * 0.48f, width * 0.32f, height * 0.58f)
        quadraticBezierTo(width * 0.40f, height * 0.62f, width * 0.50f, height * 0.62f)
        quadraticBezierTo(width * 0.60f, height * 0.62f, width * 0.68f, height * 0.58f)
        quadraticBezierTo(width * 0.75f, height * 0.48f, width * 0.70f, height * 0.35f)
        lineTo(width * 0.70f, height * 0.15f)
        close()

        // Arms and legs similar to adult female
        // Left arm
        moveTo(width * 0.30f, height * 0.16f)
        lineTo(width * 0.15f, height * 0.22f)
        lineTo(width * 0.10f, height * 0.45f)
        lineTo(width * 0.15f, height * 0.45f)
        lineTo(width * 0.18f, height * 0.25f)
        lineTo(width * 0.30f, height * 0.20f)
        close()

        // Right arm
        moveTo(width * 0.70f, height * 0.16f)
        lineTo(width * 0.85f, height * 0.22f)
        lineTo(width * 0.90f, height * 0.45f)
        lineTo(width * 0.85f, height * 0.45f)
        lineTo(width * 0.82f, height * 0.25f)
        lineTo(width * 0.70f, height * 0.20f)
        close()

        // Legs
        moveTo(width * 0.35f, height * 0.60f)
        lineTo(width * 0.30f, height * 0.95f)
        lineTo(width * 0.42f, height * 0.95f)
        lineTo(width * 0.48f, height * 0.60f)
        close()

        moveTo(width * 0.52f, height * 0.60f)
        lineTo(width * 0.58f, height * 0.95f)
        lineTo(width * 0.70f, height * 0.95f)
        lineTo(width * 0.65f, height * 0.60f)
        close()
    }
}

/**
 * Create child body path
 */
private fun createChildPath(width: Float, height: Float, viewMode: BodyViewMode): Path {
    return Path().apply {
        // Larger head (proportionally)
        addOval(
            Rect(
                left = width * 0.32f,
                top = height * 0.02f,
                right = width * 0.68f,
                bottom = height * 0.18f
            )
        )

        // Neck (shorter)
        moveTo(width * 0.44f, height * 0.18f)
        lineTo(width * 0.44f, height * 0.20f)
        lineTo(width * 0.56f, height * 0.20f)
        lineTo(width * 0.56f, height * 0.18f)

        // Torso (chubbier)
        moveTo(width * 0.32f, height * 0.20f)
        quadraticBezierTo(width * 0.28f, height * 0.40f, width * 0.32f, height * 0.55f)
        lineTo(width * 0.68f, height * 0.55f)
        quadraticBezierTo(width * 0.72f, height * 0.40f, width * 0.68f, height * 0.20f)
        close()

        // Arms (shorter, chubbier)
        moveTo(width * 0.32f, height * 0.22f)
        lineTo(width * 0.20f, height * 0.28f)
        lineTo(width * 0.16f, height * 0.42f)
        lineTo(width * 0.22f, height * 0.42f)
        lineTo(width * 0.24f, height * 0.30f)
        lineTo(width * 0.32f, height * 0.26f)
        close()

        moveTo(width * 0.68f, height * 0.22f)
        lineTo(width * 0.80f, height * 0.28f)
        lineTo(width * 0.84f, height * 0.42f)
        lineTo(width * 0.78f, height * 0.42f)
        lineTo(width * 0.76f, height * 0.30f)
        lineTo(width * 0.68f, height * 0.26f)
        close()

        // Legs (shorter)
        moveTo(width * 0.36f, height * 0.55f)
        lineTo(width * 0.32f, height * 0.90f)
        lineTo(width * 0.44f, height * 0.90f)
        lineTo(width * 0.48f, height * 0.55f)
        close()

        moveTo(width * 0.52f, height * 0.55f)
        lineTo(width * 0.56f, height * 0.90f)
        lineTo(width * 0.68f, height * 0.90f)
        lineTo(width * 0.64f, height * 0.55f)
        close()
    }
}

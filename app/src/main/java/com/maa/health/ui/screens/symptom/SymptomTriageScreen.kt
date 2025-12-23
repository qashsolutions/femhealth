package com.maa.health.ui.screens.symptom

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.maa.health.data.model.Action
import com.maa.health.data.model.BodyRegion
import com.maa.health.data.model.Severity
import com.maa.health.data.model.SymptomType
import com.maa.health.data.model.TriageResult
import com.maa.health.data.model.Urgency
import com.maa.health.service.VoiceAction
import com.maa.health.service.VoiceService
import com.maa.health.ui.components.FloatingVoiceFAB
import com.maa.health.ui.components.MaaButton
import com.maa.health.ui.components.MaaButtonStyle
import com.maa.health.ui.theme.MaaColors
import com.maa.health.ui.theme.MaaShapes
import com.maa.health.ui.theme.MaaSpacing
import com.maa.health.ui.theme.MaaTypography

/**
 * Symptom Triage Screen with Interactive Body Map
 *
 * Features:
 * - Tap on body regions to select
 * - Context-aware symptom suggestions (pregnancy, child)
 * - Danger sign detection with immediate alerts
 * - AI-powered triage recommendations
 * - WHO/IMCI-based protocols
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SymptomTriageScreen(
    onBack: () -> Unit,
    onEmergency: () -> Unit,
    viewModel: SymptomTriageViewModel = hiltViewModel(),
    voiceService: VoiceService? = null,
    onVoiceInput: ((String) -> Unit)? = null
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaaColors.background)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
        TopAppBar(
            title = {
                Text(
                    text = when (uiState.currentStep) {
                        TriageStep.SELECT_REGION -> "Where does it hurt?"
                        TriageStep.SELECT_SYMPTOMS -> "What are you feeling?"
                        TriageStep.SELECT_SEVERITY -> "How severe is it?"
                        TriageStep.PROCESSING -> "Analyzing..."
                        TriageStep.RESULT -> "Recommendation"
                    },
                    style = MaaTypography.titleLarge,
                    color = MaaColors.textPrimary
                )
            },
            navigationIcon = {
                IconButton(onClick = {
                    if (uiState.currentStep == TriageStep.SELECT_REGION) {
                        onBack()
                    } else {
                        viewModel.goBack()
                    }
                }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaaColors.textPrimary
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaaColors.background
            )
        )

        // Danger sign alert
        AnimatedVisibility(
            visible = uiState.hasDangerSigns,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            DangerSignAlert(
                dangerSigns = uiState.dangerSignsDetected,
                immediateAction = uiState.immediateAction,
                onEmergency = onEmergency
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = MaaSpacing.medium)
        ) {
            // Context indicator
            if (uiState.isPregnant || uiState.isForChild) {
                ContextIndicator(
                    isPregnant = uiState.isPregnant,
                    gestationalWeek = uiState.gestationalWeek,
                    isForChild = uiState.isForChild,
                    childAgeMonths = uiState.childAgeMonths
                )
                Spacer(modifier = Modifier.height(MaaSpacing.small))
            }

            when (uiState.currentStep) {
                TriageStep.SELECT_REGION -> {
                    BodyMapSection(
                        selectedRegion = uiState.selectedRegion,
                        isPregnant = uiState.isPregnant,
                        isChild = uiState.isForChild,
                        onRegionSelected = { viewModel.selectBodyRegion(it) }
                    )
                }

                TriageStep.SELECT_SYMPTOMS -> {
                    SymptomSelectionSection(
                        selectedRegion = uiState.selectedRegion,
                        suggestedSymptoms = uiState.suggestedSymptoms,
                        selectedSymptoms = uiState.selectedSymptoms,
                        onToggleSymptom = { viewModel.toggleSymptom(it) },
                        onContinue = { viewModel.proceedToSeverity() }
                    )
                }

                TriageStep.SELECT_SEVERITY -> {
                    SeveritySelectionSection(
                        selectedSeverity = uiState.selectedSeverity,
                        durationHours = uiState.symptomDurationHours,
                        onSeveritySelected = { viewModel.selectSeverity(it) },
                        onDurationChanged = { viewModel.setDuration(it) },
                        onContinue = { viewModel.proceedToTriage() }
                    )
                }

                TriageStep.PROCESSING -> {
                    ProcessingSection()
                }

                TriageStep.RESULT -> {
                    TriageResultSection(
                        result = uiState.triageResult,
                        onNewSymptom = { viewModel.reset() },
                        onEmergency = onEmergency
                    )
                }
            }

            Spacer(modifier = Modifier.height(80.dp))
        }
        }

        // Voice FAB for symptom description
        voiceService?.let { service ->
            FloatingVoiceFAB(
                voiceService = service,
                voiceAction = VoiceAction.LOG_SYMPTOM,
                onTranscriptionConfirmed = { text ->
                    onVoiceInput?.invoke(text)
                },
                onEditRequested = { text ->
                    onVoiceInput?.invoke(text)
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(MaaSpacing.large)
            )
        }
    }
}

@Composable
private fun ContextIndicator(
    isPregnant: Boolean,
    gestationalWeek: Int?,
    isForChild: Boolean,
    childAgeMonths: Int?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isPregnant) {
                MaaColors.stagePregnancy.copy(alpha = 0.1f)
            } else {
                MaaColors.stageChildCare.copy(alpha = 0.1f)
            }
        ),
        shape = MaaShapes.small
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(MaaSpacing.small),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = when {
                    isPregnant && gestationalWeek != null -> "Pregnancy Mode • Week $gestationalWeek"
                    isForChild && childAgeMonths != null -> "Child Mode • ${childAgeMonths} months old"
                    isPregnant -> "Pregnancy Mode"
                    isForChild -> "Child Mode"
                    else -> ""
                },
                style = MaaTypography.labelMedium,
                color = if (isPregnant) MaaColors.stagePregnancy else MaaColors.stageChildCare
            )
        }
    }
}

@Composable
private fun DangerSignAlert(
    dangerSigns: List<String>,
    immediateAction: String?,
    onEmergency: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = MaaSpacing.medium),
        colors = CardDefaults.cardColors(containerColor = MaaColors.error),
        shape = MaaShapes.medium
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(MaaSpacing.medium)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(MaaSpacing.small))
                Text(
                    text = "DANGER SIGN DETECTED",
                    style = MaaTypography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(MaaSpacing.small))

            dangerSigns.forEach { sign ->
                Text(
                    text = "• $sign",
                    style = MaaTypography.bodySmall,
                    color = Color.White.copy(alpha = 0.9f)
                )
            }

            if (immediateAction != null) {
                Spacer(modifier = Modifier.height(MaaSpacing.small))
                Text(
                    text = immediateAction,
                    style = MaaTypography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(MaaSpacing.small))

            MaaButton(
                text = "Get Emergency Help",
                onClick = onEmergency,
                style = MaaButtonStyle.SECONDARY,
                fullWidth = true
            )
        }
    }
}

@Composable
private fun BodyMapSection(
    selectedRegion: BodyRegion?,
    isPregnant: Boolean,
    isChild: Boolean,
    onRegionSelected: (BodyRegion) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Tap on the body part that hurts",
            style = MaaTypography.bodyMedium,
            color = MaaColors.textSecondary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(MaaSpacing.medium))

        // Interactive body map
        InteractiveBodyMap(
            selectedRegion = selectedRegion,
            isPregnant = isPregnant,
            isChild = isChild,
            onRegionTapped = onRegionSelected,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(0.5f)
        )

        Spacer(modifier = Modifier.height(MaaSpacing.medium))

        // Quick region buttons
        Text(
            text = "Or select a region:",
            style = MaaTypography.labelMedium,
            color = MaaColors.textTertiary
        )

        Spacer(modifier = Modifier.height(MaaSpacing.small))

        BodyRegionChips(
            selectedRegion = selectedRegion,
            isPregnant = isPregnant,
            isChild = isChild,
            onRegionSelected = onRegionSelected
        )
    }
}

@Composable
private fun InteractiveBodyMap(
    selectedRegion: BodyRegion?,
    isPregnant: Boolean,
    isChild: Boolean,
    onRegionTapped: (BodyRegion) -> Unit,
    modifier: Modifier = Modifier
) {
    val regionColors = remember(selectedRegion) {
        BodyRegion.entries.associateWith { region ->
            if (region == selectedRegion) {
                MaaColors.accent
            } else {
                MaaColors.surfaceVariant
            }
        }
    }

    Canvas(
        modifier = modifier
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    val region = detectBodyRegion(
                        offset = offset,
                        canvasWidth = size.width.toFloat(),
                        canvasHeight = size.height.toFloat(),
                        isPregnant = isPregnant,
                        isChild = isChild
                    )
                    region?.let { onRegionTapped(it) }
                }
            }
    ) {
        val width = size.width
        val height = size.height
        val centerX = width / 2

        // Draw simplified human body outline
        val bodyColor = MaaColors.textTertiary.copy(alpha = 0.3f)
        val strokeWidth = 3.dp.toPx()

        // Head
        val headRadius = width * 0.12f
        val headCenterY = height * 0.08f
        drawCircle(
            color = regionColors[BodyRegion.HEAD] ?: bodyColor,
            radius = headRadius,
            center = Offset(centerX, headCenterY)
        )
        if (selectedRegion == BodyRegion.HEAD) {
            drawCircle(
                color = MaaColors.accent,
                radius = headRadius,
                center = Offset(centerX, headCenterY),
                style = Stroke(strokeWidth)
            )
        }

        // Neck
        val neckTop = headCenterY + headRadius
        val neckBottom = height * 0.15f
        drawRect(
            color = bodyColor,
            topLeft = Offset(centerX - width * 0.04f, neckTop),
            size = Size(width * 0.08f, neckBottom - neckTop)
        )

        // Torso/Chest
        val chestTop = neckBottom
        val chestBottom = height * 0.35f
        val chestWidth = width * 0.3f
        drawRect(
            color = regionColors[BodyRegion.CHEST] ?: bodyColor,
            topLeft = Offset(centerX - chestWidth / 2, chestTop),
            size = Size(chestWidth, chestBottom - chestTop)
        )

        // Abdomen (larger if pregnant)
        val abdomenTop = chestBottom
        val abdomenBottom = height * 0.52f
        val abdomenWidth = if (isPregnant) width * 0.38f else width * 0.28f
        drawOval(
            color = regionColors[if (isPregnant) BodyRegion.UTERUS else BodyRegion.ABDOMEN] ?: bodyColor,
            topLeft = Offset(centerX - abdomenWidth / 2, abdomenTop),
            size = Size(abdomenWidth, abdomenBottom - abdomenTop)
        )

        // Pelvis
        val pelvisTop = abdomenBottom
        val pelvisBottom = height * 0.58f
        drawRect(
            color = regionColors[BodyRegion.PELVIS] ?: bodyColor,
            topLeft = Offset(centerX - width * 0.18f, pelvisTop),
            size = Size(width * 0.36f, pelvisBottom - pelvisTop)
        )

        // Arms
        val armWidth = width * 0.08f
        val shoulderY = chestTop + (chestBottom - chestTop) * 0.1f
        val handY = height * 0.48f

        // Left arm
        drawRect(
            color = regionColors[BodyRegion.HANDS] ?: bodyColor,
            topLeft = Offset(centerX - chestWidth / 2 - armWidth, shoulderY),
            size = Size(armWidth, handY - shoulderY)
        )
        // Right arm
        drawRect(
            color = regionColors[BodyRegion.HANDS] ?: bodyColor,
            topLeft = Offset(centerX + chestWidth / 2, shoulderY),
            size = Size(armWidth, handY - shoulderY)
        )

        // Legs
        val legTop = pelvisBottom
        val legBottom = height * 0.92f
        val legWidth = width * 0.1f
        val legGap = width * 0.04f

        // Left leg
        drawRect(
            color = bodyColor,
            topLeft = Offset(centerX - legGap / 2 - legWidth, legTop),
            size = Size(legWidth, legBottom - legTop)
        )
        // Right leg
        drawRect(
            color = bodyColor,
            topLeft = Offset(centerX + legGap / 2, legTop),
            size = Size(legWidth, legBottom - legTop)
        )

        // Feet
        val feetTop = legBottom
        val feetBottom = height * 0.98f
        val footWidth = width * 0.12f

        drawOval(
            color = regionColors[BodyRegion.FEET] ?: bodyColor,
            topLeft = Offset(centerX - legGap / 2 - footWidth, feetTop),
            size = Size(footWidth, feetBottom - feetTop)
        )
        drawOval(
            color = regionColors[BodyRegion.FEET] ?: bodyColor,
            topLeft = Offset(centerX + legGap / 2, feetTop),
            size = Size(footWidth, feetBottom - feetTop)
        )
    }
}

private fun detectBodyRegion(
    offset: Offset,
    canvasWidth: Float,
    canvasHeight: Float,
    isPregnant: Boolean,
    isChild: Boolean
): BodyRegion? {
    val x = offset.x
    val y = offset.y
    val centerX = canvasWidth / 2

    // Normalized coordinates
    val relX = x / canvasWidth
    val relY = y / canvasHeight

    return when {
        // Head region
        relY < 0.14f && relX in 0.3f..0.7f -> BodyRegion.HEAD

        // Chest region
        relY in 0.15f..0.35f && relX in 0.3f..0.7f -> BodyRegion.CHEST

        // Abdomen/Uterus region
        relY in 0.35f..0.52f && relX in 0.25f..0.75f -> {
            if (isPregnant) BodyRegion.UTERUS else BodyRegion.ABDOMEN
        }

        // Pelvis region
        relY in 0.52f..0.58f && relX in 0.3f..0.7f -> BodyRegion.PELVIS

        // Arms/Hands
        relY in 0.18f..0.5f && (relX < 0.3f || relX > 0.7f) -> BodyRegion.HANDS

        // Feet
        relY > 0.9f -> BodyRegion.FEET

        else -> null
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun BodyRegionChips(
    selectedRegion: BodyRegion?,
    isPregnant: Boolean,
    isChild: Boolean,
    onRegionSelected: (BodyRegion) -> Unit
) {
    val regions = remember(isPregnant, isChild) {
        buildList {
            add(BodyRegion.HEAD)
            add(BodyRegion.EYES)
            add(BodyRegion.MOUTH)
            add(BodyRegion.CHEST)
            add(BodyRegion.ABDOMEN)
            if (isPregnant) {
                add(BodyRegion.UTERUS)
            }
            add(BodyRegion.PELVIS)
            add(BodyRegion.HANDS)
            add(BodyRegion.FEET)
            add(BodyRegion.SKIN)
            if (isChild) {
                add(BodyRegion.DIAPER_AREA)
            }
        }
    }

    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(MaaSpacing.small),
        verticalArrangement = Arrangement.spacedBy(MaaSpacing.small)
    ) {
        regions.forEach { region ->
            RegionChip(
                region = region,
                isSelected = region == selectedRegion,
                onClick = { onRegionSelected(region) }
            )
        }
    }
}

@Composable
private fun RegionChip(
    region: BodyRegion,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(MaaShapes.small)
            .background(
                if (isSelected) MaaColors.accent else MaaColors.surfaceVariant
            )
            .clickable(onClick = onClick)
            .padding(horizontal = MaaSpacing.small, vertical = MaaSpacing.extraSmall)
    ) {
        Text(
            text = region.displayName,
            style = MaaTypography.labelMedium,
            color = if (isSelected) Color.White else MaaColors.textSecondary
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SymptomSelectionSection(
    selectedRegion: BodyRegion?,
    suggestedSymptoms: List<SymptomType>,
    selectedSymptoms: Set<SymptomType>,
    onToggleSymptom: (SymptomType) -> Unit,
    onContinue: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaaColors.surface),
            shape = MaaShapes.medium
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(MaaSpacing.medium)
            ) {
                Text(
                    text = "Symptoms in ${selectedRegion?.displayName ?: "selected area"}",
                    style = MaaTypography.titleSmall,
                    color = MaaColors.textPrimary
                )

                Spacer(modifier = Modifier.height(MaaSpacing.small))

                Text(
                    text = "Select all symptoms you're experiencing",
                    style = MaaTypography.bodySmall,
                    color = MaaColors.textSecondary
                )

                Spacer(modifier = Modifier.height(MaaSpacing.medium))

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(MaaSpacing.small),
                    verticalArrangement = Arrangement.spacedBy(MaaSpacing.small)
                ) {
                    suggestedSymptoms.forEach { symptom ->
                        SymptomChip(
                            symptom = symptom,
                            isSelected = symptom in selectedSymptoms,
                            onClick = { onToggleSymptom(symptom) }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(MaaSpacing.medium))

        MaaButton(
            text = "Continue",
            onClick = onContinue,
            enabled = selectedSymptoms.isNotEmpty(),
            fullWidth = true
        )
    }
}

@Composable
private fun SymptomChip(
    symptom: SymptomType,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = when {
        isSelected && symptom.isDangerSign -> MaaColors.error
        isSelected -> MaaColors.accent
        symptom.isDangerSign -> MaaColors.error.copy(alpha = 0.1f)
        else -> MaaColors.surfaceVariant
    }

    val textColor = when {
        isSelected -> Color.White
        symptom.isDangerSign -> MaaColors.error
        else -> MaaColors.textSecondary
    }

    Row(
        modifier = Modifier
            .clip(MaaShapes.small)
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(horizontal = MaaSpacing.small, vertical = MaaSpacing.extraSmall),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (symptom.isDangerSign) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
        }
        Text(
            text = symptom.displayName,
            style = MaaTypography.labelMedium,
            color = textColor
        )
        if (isSelected) {
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

@Composable
private fun SeveritySelectionSection(
    selectedSeverity: Severity?,
    durationHours: Int?,
    onSeveritySelected: (Severity) -> Unit,
    onDurationChanged: (Int) -> Unit,
    onContinue: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaaColors.surface),
            shape = MaaShapes.medium
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(MaaSpacing.medium)
            ) {
                Text(
                    text = "How severe is it?",
                    style = MaaTypography.titleSmall,
                    color = MaaColors.textPrimary
                )

                Spacer(modifier = Modifier.height(MaaSpacing.medium))

                Severity.entries.forEach { severity ->
                    SeverityOption(
                        severity = severity,
                        isSelected = severity == selectedSeverity,
                        onClick = { onSeveritySelected(severity) }
                    )
                    if (severity != Severity.entries.last()) {
                        Spacer(modifier = Modifier.height(MaaSpacing.small))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(MaaSpacing.medium))

        // Duration selector
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaaColors.surface),
            shape = MaaShapes.medium
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(MaaSpacing.medium)
            ) {
                Text(
                    text = "How long have you had this?",
                    style = MaaTypography.titleSmall,
                    color = MaaColors.textPrimary
                )

                Spacer(modifier = Modifier.height(MaaSpacing.small))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    listOf(
                        1 to "< 1 hour",
                        6 to "Few hours",
                        24 to "1 day",
                        72 to "Few days",
                        168 to "1 week+"
                    ).forEach { (hours, label) ->
                        DurationChip(
                            label = label,
                            isSelected = durationHours == hours,
                            onClick = { onDurationChanged(hours) }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(MaaSpacing.medium))

        MaaButton(
            text = "Get Recommendation",
            onClick = onContinue,
            enabled = selectedSeverity != null,
            fullWidth = true
        )
    }
}

@Composable
private fun SeverityOption(
    severity: Severity,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val (label, description, color) = when (severity) {
        Severity.MILD -> Triple("Mild", "I can continue my daily activities", MaaColors.success)
        Severity.MODERATE -> Triple("Moderate", "It bothers me but I can manage", MaaColors.warning)
        Severity.MODERATELY_SEVERE -> Triple("Moderately Severe", "It's affecting my activities", MaaColors.warning)
        Severity.SEVERE -> Triple("Severe", "I can barely function", MaaColors.error)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaaShapes.small)
            .background(if (isSelected) color.copy(alpha = 0.15f) else MaaColors.surfaceVariant)
            .border(
                width = if (isSelected) 2.dp else 0.dp,
                color = if (isSelected) color else Color.Transparent,
                shape = MaaShapes.small
            )
            .clickable(onClick = onClick)
            .padding(MaaSpacing.small),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(CircleShape)
                .background(if (isSelected) color else Color.Transparent)
                .border(2.dp, color, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(12.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(MaaSpacing.small))

        Column {
            Text(
                text = label,
                style = MaaTypography.labelMedium,
                color = MaaColors.textPrimary
            )
            Text(
                text = description,
                style = MaaTypography.bodySmall,
                color = MaaColors.textTertiary
            )
        }
    }
}

@Composable
private fun DurationChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(MaaShapes.small)
            .background(if (isSelected) MaaColors.accent else MaaColors.surfaceVariant)
            .clickable(onClick = onClick)
            .padding(horizontal = MaaSpacing.small, vertical = MaaSpacing.extraSmall)
    ) {
        Text(
            text = label,
            style = MaaTypography.labelSmall,
            color = if (isSelected) Color.White else MaaColors.textSecondary
        )
    }
}

@Composable
private fun ProcessingSection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = MaaSpacing.extraLarge),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator(
            color = MaaColors.accent,
            modifier = Modifier.size(64.dp)
        )

        Spacer(modifier = Modifier.height(MaaSpacing.medium))

        Text(
            text = "Analyzing your symptoms...",
            style = MaaTypography.titleMedium,
            color = MaaColors.textPrimary
        )

        Spacer(modifier = Modifier.height(MaaSpacing.small))

        Text(
            text = "Using AI to provide personalized guidance",
            style = MaaTypography.bodyMedium,
            color = MaaColors.textSecondary
        )
    }
}

@Composable
private fun TriageResultSection(
    result: TriageResult?,
    onNewSymptom: () -> Unit,
    onEmergency: () -> Unit
) {
    if (result == null) return

    val (urgencyColor, urgencyLabel) = when (result.urgency) {
        Urgency.EMERGENCY -> MaaColors.error to "EMERGENCY"
        Urgency.URGENT -> MaaColors.warning to "Urgent"
        Urgency.ROUTINE -> MaaColors.success to "Routine"
        Urgency.SELF_CARE -> MaaColors.textSecondary to "Self-Care"
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        // Urgency card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = urgencyColor.copy(alpha = 0.15f)
            ),
            shape = MaaShapes.medium
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(MaaSpacing.medium),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = urgencyLabel,
                    style = MaaTypography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = urgencyColor
                )

                Spacer(modifier = Modifier.height(MaaSpacing.small))

                Text(
                    text = result.classification,
                    style = MaaTypography.bodyLarge,
                    color = MaaColors.textPrimary,
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(MaaSpacing.medium))

        // Action card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaaColors.surface),
            shape = MaaShapes.medium
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(MaaSpacing.medium)
            ) {
                Text(
                    text = "Recommended Action",
                    style = MaaTypography.titleSmall,
                    color = MaaColors.textPrimary
                )

                Spacer(modifier = Modifier.height(MaaSpacing.small))

                Text(
                    text = result.action.displayText,
                    style = MaaTypography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = urgencyColor
                )

                Spacer(modifier = Modifier.height(MaaSpacing.medium))

                Text(
                    text = "Instructions:",
                    style = MaaTypography.labelMedium,
                    color = MaaColors.textSecondary
                )

                result.instructions.forEach { instruction ->
                    Text(
                        text = "• $instruction",
                        style = MaaTypography.bodyMedium,
                        color = MaaColors.textPrimary,
                        modifier = Modifier.padding(start = MaaSpacing.small, top = MaaSpacing.extraSmall)
                    )
                }
            }
        }

        // Warning signs to watch
        if (result.warningSignsToWatch.isNotEmpty()) {
            Spacer(modifier = Modifier.height(MaaSpacing.medium))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaaColors.warning.copy(alpha = 0.1f)
                ),
                shape = MaaShapes.medium
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(MaaSpacing.medium)
                ) {
                    Text(
                        text = "Watch for these warning signs:",
                        style = MaaTypography.titleSmall,
                        color = MaaColors.warning
                    )

                    Spacer(modifier = Modifier.height(MaaSpacing.small))

                    result.warningSignsToWatch.forEach { sign ->
                        Text(
                            text = "⚠ $sign",
                            style = MaaTypography.bodySmall,
                            color = MaaColors.textPrimary
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(MaaSpacing.medium))

        // Actions
        if (result.urgency == Urgency.EMERGENCY) {
            MaaButton(
                text = "Get Emergency Help Now",
                onClick = onEmergency,
                style = MaaButtonStyle.PRIMARY,
                fullWidth = true
            )
            Spacer(modifier = Modifier.height(MaaSpacing.small))
        }

        MaaButton(
            text = "Log Another Symptom",
            onClick = onNewSymptom,
            style = MaaButtonStyle.OUTLINE,
            fullWidth = true
        )
    }
}

// Extension properties for display
private val BodyRegion.displayName: String
    get() = when (this) {
        BodyRegion.HEAD -> "Head"
        BodyRegion.EYES -> "Eyes"
        BodyRegion.MOUTH -> "Mouth"
        BodyRegion.CHEST -> "Chest"
        BodyRegion.ABDOMEN -> "Abdomen"
        BodyRegion.ABDOMEN_UPPER -> "Upper Abdomen"
        BodyRegion.PELVIS -> "Pelvis"
        BodyRegion.UTERUS -> "Uterus"
        BodyRegion.HANDS -> "Hands/Arms"
        BodyRegion.FEET -> "Feet/Legs"
        BodyRegion.SKIN -> "Skin"
        BodyRegion.DIAPER_AREA -> "Diaper Area"
        BodyRegion.JOINTS -> "Joints"
    }

private val SymptomType.displayName: String
    get() = name.replace("_", " ").lowercase()
        .replaceFirstChar { it.uppercase() }

private val Action.displayText: String
    get() = when (this) {
        Action.GO_TO_HOSPITAL_NOW -> "Go to Hospital Immediately"
        Action.GO_TO_HOSPITAL_TODAY -> "Visit Hospital Today"
        Action.SCHEDULE_VISIT -> "Schedule a Doctor Visit"
        Action.HOME_MANAGEMENT -> "Manage at Home"
        Action.CALL_HELPLINE -> "Call Health Helpline"
    }

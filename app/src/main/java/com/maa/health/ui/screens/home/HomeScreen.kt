package com.maa.health.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChildCare
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.MoodBad
import androidx.compose.material.icons.filled.PregnantWoman
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.maa.health.data.model.BodyRegion
import com.maa.health.ui.components.BodyType
import com.maa.health.ui.components.BodyViewMode
import com.maa.health.ui.components.InteractiveBodySvg
import com.maa.health.ui.components.MaaButton
import com.maa.health.ui.components.MaaButtonVariant
import com.maa.health.ui.components.MaaCard
import com.maa.health.ui.components.MaaCardVariant
import com.maa.health.ui.components.MaaDangerSignCard
import com.maa.health.ui.components.VoiceInputButton
import com.maa.health.ui.components.VoiceInputState
import com.maa.health.ui.theme.MaaColors
import com.maa.health.ui.theme.MaaShapes
import com.maa.health.ui.theme.MaaSpacing
import com.maa.health.ui.theme.MaaTypography

/**
 * Main home screen with body map
 *
 * Features:
 * - Interactive body map for symptom logging
 * - Quick access to modules based on lifecycle stage
 * - Voice input for hands-free interaction
 * - Emergency access
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToBodyMap: () -> Unit,
    onNavigateToPregnancy: () -> Unit,
    onNavigateToChildCare: () -> Unit,
    onNavigateToCycle: () -> Unit,
    onNavigateToMentalHealth: () -> Unit,
    onNavigateToEmergency: () -> Unit
) {
    var selectedBodyRegion by remember { mutableStateOf<BodyRegion?>(null) }
    var voiceInputState by remember { mutableStateOf(VoiceInputState.IDLE) }
    var bodyViewMode by remember { mutableStateOf(BodyViewMode.FRONT) }

    val scrollState = rememberScrollState()

    // Quick access modules based on lifecycle stage
    val quickAccessModules = remember {
        listOf(
            QuickAccessModule(
                title = "Pregnancy",
                icon = Icons.Default.PregnantWoman,
                color = MaaColors.stagePregnancy,
                onClick = onNavigateToPregnancy
            ),
            QuickAccessModule(
                title = "Child Care",
                icon = Icons.Default.ChildCare,
                color = MaaColors.stageChildCare,
                onClick = onNavigateToChildCare
            ),
            QuickAccessModule(
                title = "Cycle",
                icon = Icons.Default.Timeline,
                color = MaaColors.stageReproductive,
                onClick = onNavigateToCycle
            ),
            QuickAccessModule(
                title = "Mood",
                icon = Icons.Default.MoodBad,
                color = MaaColors.info,
                onClick = onNavigateToMentalHealth
            )
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaaColors.background)
    ) {
        // Top app bar
        TopAppBar(
            title = {
                Column {
                    Text(
                        text = "Maa",
                        style = MaaTypography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaaColors.textPrimary
                    )
                    Text(
                        text = "How are you feeling today?",
                        style = MaaTypography.bodySmall,
                        color = MaaColors.textSecondary
                    )
                }
            },
            actions = {
                IconButton(onClick = { /* Settings */ }) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = MaaColors.textSecondary
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaaColors.background
            )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            // Quick access modules
            Text(
                text = "Quick Access",
                style = MaaTypography.titleSmall,
                color = MaaColors.textPrimary,
                modifier = Modifier.padding(horizontal = MaaSpacing.large)
            )

            Spacer(modifier = Modifier.height(MaaSpacing.small))

            LazyRow(
                contentPadding = PaddingValues(horizontal = MaaSpacing.large),
                horizontalArrangement = Arrangement.spacedBy(MaaSpacing.medium)
            ) {
                items(quickAccessModules) { module ->
                    QuickAccessCard(module = module)
                }
            }

            Spacer(modifier = Modifier.height(MaaSpacing.large))

            // Body map section
            MaaCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = MaaSpacing.large),
                variant = MaaCardVariant.ELEVATED
            ) {
                Column(
                    modifier = Modifier.padding(MaaSpacing.medium)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Log a Symptom",
                            style = MaaTypography.titleMedium,
                            color = MaaColors.textPrimary
                        )

                        // View toggle
                        Row {
                            MaaButton(
                                text = "Front",
                                onClick = { bodyViewMode = BodyViewMode.FRONT },
                                variant = if (bodyViewMode == BodyViewMode.FRONT) {
                                    MaaButtonVariant.PRIMARY
                                } else {
                                    MaaButtonVariant.TERTIARY
                                },
                                size = com.maa.health.ui.components.MaaButtonSize.SMALL
                            )
                            Spacer(modifier = Modifier.width(MaaSpacing.small))
                            MaaButton(
                                text = "Back",
                                onClick = { bodyViewMode = BodyViewMode.BACK },
                                variant = if (bodyViewMode == BodyViewMode.BACK) {
                                    MaaButtonVariant.PRIMARY
                                } else {
                                    MaaButtonVariant.TERTIARY
                                },
                                size = com.maa.health.ui.components.MaaButtonSize.SMALL
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(MaaSpacing.small))

                    Text(
                        text = "Tap where you're experiencing discomfort",
                        style = MaaTypography.bodySmall,
                        color = MaaColors.textSecondary
                    )

                    // Interactive body map
                    InteractiveBodySvg(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(320.dp),
                        bodyType = BodyType.ADULT_FEMALE,
                        viewMode = bodyViewMode,
                        selectedRegion = selectedBodyRegion,
                        onRegionTap = { region ->
                            selectedBodyRegion = region
                            // TODO: Navigate to symptom entry
                        }
                    )

                    // Selected region info
                    if (selectedBodyRegion != null) {
                        Spacer(modifier = Modifier.height(MaaSpacing.medium))
                        MaaCard(
                            modifier = Modifier.fillMaxWidth(),
                            variant = MaaCardVariant.DEFAULT,
                            backgroundColor = MaaColors.surfaceVariant
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(MaaSpacing.medium),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Selected: ${selectedBodyRegion?.name?.replace("_", " ")}",
                                        style = MaaTypography.labelMedium,
                                        color = MaaColors.textPrimary
                                    )
                                    Text(
                                        text = "Tap to describe your symptoms",
                                        style = MaaTypography.bodySmall,
                                        color = MaaColors.textSecondary
                                    )
                                }
                                MaaButton(
                                    text = "Continue",
                                    onClick = onNavigateToBodyMap,
                                    size = com.maa.health.ui.components.MaaButtonSize.SMALL
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(MaaSpacing.large))

            // Voice input section
            MaaCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = MaaSpacing.large),
                variant = MaaCardVariant.DEFAULT
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(MaaSpacing.medium),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Speak Your Symptoms",
                            style = MaaTypography.titleSmall,
                            color = MaaColors.textPrimary
                        )
                        Spacer(modifier = Modifier.height(MaaSpacing.extraSmall))
                        Text(
                            text = "Describe how you're feeling in your language",
                            style = MaaTypography.bodySmall,
                            color = MaaColors.textSecondary
                        )
                    }
                    Spacer(modifier = Modifier.width(MaaSpacing.medium))
                    VoiceInputButton(
                        state = voiceInputState,
                        onClick = {
                            voiceInputState = when (voiceInputState) {
                                VoiceInputState.IDLE -> VoiceInputState.LISTENING
                                VoiceInputState.LISTENING -> VoiceInputState.IDLE
                                else -> VoiceInputState.IDLE
                            }
                        },
                        showHint = false
                    )
                }
            }

            Spacer(modifier = Modifier.height(MaaSpacing.large))

            // Emergency access
            MaaDangerSignCard(
                title = "Recognize Danger Signs",
                signs = listOf(
                    "Heavy bleeding",
                    "Severe headache with blurred vision",
                    "Difficulty breathing",
                    "High fever not responding to medicine"
                ),
                modifier = Modifier.padding(horizontal = MaaSpacing.large),
                onEmergencyClick = onNavigateToEmergency
            )

            Spacer(modifier = Modifier.height(MaaSpacing.extraLarge))
        }
    }
}

private data class QuickAccessModule(
    val title: String,
    val icon: ImageVector,
    val color: androidx.compose.ui.graphics.Color,
    val onClick: () -> Unit
)

@Composable
private fun QuickAccessCard(
    module: QuickAccessModule
) {
    MaaCard(
        modifier = Modifier.width(100.dp),
        variant = MaaCardVariant.DEFAULT,
        backgroundColor = module.color,
        onClick = module.onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(MaaSpacing.medium),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(MaaShapes.small)
                    .background(MaaColors.surface.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = module.icon,
                    contentDescription = module.title,
                    modifier = Modifier.size(24.dp),
                    tint = MaaColors.textPrimary
                )
            }
            Spacer(modifier = Modifier.height(MaaSpacing.small))
            Text(
                text = module.title,
                style = MaaTypography.labelSmall,
                color = MaaColors.textPrimary
            )
        }
    }
}

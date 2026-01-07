package com.maa.health.ui.screens.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.maa.health.data.model.LifecycleStage
import com.maa.health.ui.components.MaaButton
import com.maa.health.ui.components.MaaButtonSize
import com.maa.health.ui.theme.MaaColors
import com.maa.health.ui.theme.MaaShapes
import com.maa.health.ui.theme.MaaSpacing
import com.maa.health.ui.theme.MaaTypography

/**
 * Lifecycle stage selection screen
 *
 * User selects their current life stage to personalize the app experience.
 * Multiple stages can be selected (e.g., Pregnancy + Child Care)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LifecycleStageSelectionScreen(
    onStageSelected: () -> Unit,
    onBack: () -> Unit
) {
    var selectedStages by remember { mutableStateOf(setOf<LifecycleStage>()) }
    var isLoading by remember { mutableStateOf(false) }

    val lifecycleOptions = remember {
        listOf(
            LifecycleOption(
                stage = LifecycleStage.ADOLESCENCE,
                title = "Teen Health",
                description = "Ages 13-19 | Puberty, periods, body changes",
                color = MaaColors.stageAdolescence
            ),
            LifecycleOption(
                stage = LifecycleStage.REPRODUCTIVE,
                title = "Reproductive Health",
                description = "Ages 18-40 | Cycle tracking, fertility awareness",
                color = MaaColors.stageReproductive
            ),
            LifecycleOption(
                stage = LifecycleStage.PREGNANCY,
                title = "Pregnancy",
                description = "Currently pregnant | Week-by-week guidance",
                color = MaaColors.stagePregnancy
            ),
            LifecycleOption(
                stage = LifecycleStage.POSTPARTUM,
                title = "Postpartum",
                description = "After delivery | Recovery and newborn care",
                color = MaaColors.stagePostpartum
            ),
            LifecycleOption(
                stage = LifecycleStage.CHILD_CARE,
                title = "Mother of Young Child",
                description = "Child 0-5 years | Growth, vaccines, development",
                color = MaaColors.stageChildCare
            ),
            LifecycleOption(
                stage = LifecycleStage.MIDLIFE,
                title = "Midlife Health",
                description = "Ages 40-55 | Perimenopause, bone health",
                color = MaaColors.stageMidlife
            ),
            LifecycleOption(
                stage = LifecycleStage.ELDER,
                title = "Elder Care",
                description = "Ages 55+ | Managing health conditions",
                color = MaaColors.stageElder
            )
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaaColors.background)
    ) {
        TopAppBar(
            title = {},
            navigationIcon = {
                IconButton(onClick = onBack) {
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

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = MaaSpacing.large)
        ) {
            // Header
            Text(
                text = "What Best Describes You?",
                style = MaaTypography.headlineMedium,
                color = MaaColors.textPrimary
            )

            Spacer(modifier = Modifier.height(MaaSpacing.small))

            Text(
                text = "Select all that apply. You can change this anytime.",
                style = MaaTypography.bodyMedium,
                color = MaaColors.textSecondary
            )

            Spacer(modifier = Modifier.height(MaaSpacing.large))

            // Lifecycle options
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(MaaSpacing.medium)
            ) {
                items(lifecycleOptions) { option ->
                    LifecycleStageCard(
                        option = option,
                        isSelected = option.stage in selectedStages,
                        onClick = {
                            selectedStages = if (option.stage in selectedStages) {
                                selectedStages - option.stage
                            } else {
                                selectedStages + option.stage
                            }
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(MaaSpacing.medium))

            // Continue button
            MaaButton(
                text = "Get Started",
                onClick = {
                    isLoading = true
                    // TODO: Save selected stages to preferences
                    onStageSelected()
                },
                enabled = selectedStages.isNotEmpty() && !isLoading,
                loading = isLoading,
                fullWidth = true,
                size = MaaButtonSize.LARGE
            )

            Spacer(modifier = Modifier.height(MaaSpacing.large))
        }
    }
}

private data class LifecycleOption(
    val stage: LifecycleStage,
    val title: String,
    val description: String,
    val color: Color
)

@Composable
private fun LifecycleStageCard(
    option: LifecycleOption,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaaShapes.medium)
            .background(option.color)
            .border(
                width = if (isSelected) 2.dp else 0.dp,
                color = if (isSelected) MaaColors.accent else Color.Transparent,
                shape = MaaShapes.medium
            )
            .clickable(onClick = onClick)
            .padding(MaaSpacing.medium)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = option.title,
                    style = MaaTypography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = MaaColors.textPrimary
                )
                Spacer(modifier = Modifier.height(MaaSpacing.extraSmall))
                Text(
                    text = option.description,
                    style = MaaTypography.bodySmall,
                    color = MaaColors.textSecondary
                )
            }

            Spacer(modifier = Modifier.width(MaaSpacing.medium))

            // Selection indicator
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(MaaShapes.extraSmall)
                    .background(
                        if (isSelected) MaaColors.accent else MaaColors.surface
                    )
                    .border(
                        width = if (isSelected) 0.dp else 1.dp,
                        color = MaaColors.border,
                        shape = MaaShapes.extraSmall
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Selected",
                        modifier = Modifier.size(16.dp),
                        tint = Color.White
                    )
                }
            }
        }
    }
}

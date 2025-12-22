package com.maa.health.ui.screens.cycle

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.maa.health.data.model.FlowIntensity
import com.maa.health.ui.components.MaaButton
import com.maa.health.ui.components.MaaButtonStyle
import com.maa.health.ui.theme.MaaColors
import com.maa.health.ui.theme.MaaShapes
import com.maa.health.ui.theme.MaaSpacing
import com.maa.health.ui.theme.MaaTypography
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

/**
 * Cycle Tracking Home Screen
 *
 * Features:
 * - Monthly calendar with period days marked
 * - Current cycle phase indicator
 * - Quick period logging
 * - Cycle predictions
 * - Fertile window (optional)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CycleHomeScreen(
    onBack: () -> Unit,
    viewModel: CycleViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaaColors.background)
    ) {
        TopAppBar(
            title = {
                Text(
                    text = "Cycle Tracker",
                    style = MaaTypography.titleLarge,
                    color = MaaColors.textPrimary
                )
            },
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
                .verticalScroll(scrollState)
                .padding(horizontal = MaaSpacing.medium)
        ) {
            // Current phase card
            CurrentPhaseCard(
                phase = uiState.currentPhase,
                daysUntilPeriod = uiState.daysUntilNextPeriod,
                cycleDay = uiState.currentCycleDay
            )

            Spacer(modifier = Modifier.height(MaaSpacing.medium))

            // Calendar
            CycleCalendar(
                currentMonth = uiState.selectedMonth,
                periodDays = uiState.periodDaysInMonth,
                predictedDays = uiState.predictedPeriodDays,
                fertileDays = uiState.fertileDays,
                ovulationDay = uiState.ovulationDay,
                selectedDate = uiState.selectedDate,
                onDateSelected = { viewModel.selectDate(it) },
                onPreviousMonth = { viewModel.previousMonth() },
                onNextMonth = { viewModel.nextMonth() }
            )

            Spacer(modifier = Modifier.height(MaaSpacing.medium))

            // Quick actions
            QuickActionsSection(
                isPeriodActive = uiState.isPeriodActive,
                onLogPeriodStart = { viewModel.logPeriodStart() },
                onLogPeriodEnd = { viewModel.logPeriodEnd() },
                onLogSymptom = { /* Navigate to symptom log */ }
            )

            Spacer(modifier = Modifier.height(MaaSpacing.medium))

            // Flow intensity selector (when period is active)
            if (uiState.isPeriodActive) {
                FlowIntensitySelector(
                    selectedIntensity = uiState.todayFlowIntensity,
                    onIntensitySelected = { viewModel.logFlowIntensity(it) }
                )
                Spacer(modifier = Modifier.height(MaaSpacing.medium))
            }

            // Cycle insights
            CycleInsightsCard(
                averageCycleLength = uiState.averageCycleLength,
                averagePeriodLength = uiState.averagePeriodLength,
                cyclesTracked = uiState.cyclesTracked
            )

            Spacer(modifier = Modifier.height(MaaSpacing.medium))

            // Legend
            CalendarLegend()

            Spacer(modifier = Modifier.height(MaaSpacing.large))
        }
    }
}

@Composable
private fun CurrentPhaseCard(
    phase: CyclePhase,
    daysUntilPeriod: Int?,
    cycleDay: Int?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = phase.color.copy(alpha = 0.15f)
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
                text = phase.displayName,
                style = MaaTypography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = phase.color
            )

            Spacer(modifier = Modifier.height(MaaSpacing.small))

            Text(
                text = phase.description,
                style = MaaTypography.bodyMedium,
                color = MaaColors.textSecondary,
                textAlign = TextAlign.Center
            )

            if (cycleDay != null) {
                Spacer(modifier = Modifier.height(MaaSpacing.small))
                Text(
                    text = "Day $cycleDay of your cycle",
                    style = MaaTypography.bodySmall,
                    color = MaaColors.textTertiary
                )
            }

            if (daysUntilPeriod != null && phase != CyclePhase.MENSTRUAL) {
                Spacer(modifier = Modifier.height(MaaSpacing.extraSmall))
                Text(
                    text = when {
                        daysUntilPeriod == 0 -> "Period expected today"
                        daysUntilPeriod == 1 -> "Period expected tomorrow"
                        daysUntilPeriod > 0 -> "Period in $daysUntilPeriod days"
                        else -> ""
                    },
                    style = MaaTypography.labelMedium,
                    color = MaaColors.stageReproductive
                )
            }
        }
    }
}

@Composable
private fun CycleCalendar(
    currentMonth: YearMonth,
    periodDays: Set<LocalDate>,
    predictedDays: Set<LocalDate>,
    fertileDays: Set<LocalDate>,
    ovulationDay: LocalDate?,
    selectedDate: LocalDate?,
    onDateSelected: (LocalDate) -> Unit,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit
) {
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
            // Month navigation
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onPreviousMonth) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Previous month",
                        tint = MaaColors.textPrimary
                    )
                }

                Text(
                    text = "${currentMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault())} ${currentMonth.year}",
                    style = MaaTypography.titleMedium,
                    color = MaaColors.textPrimary
                )

                IconButton(onClick = onNextMonth) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Next month",
                        tint = MaaColors.textPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(MaaSpacing.small))

            // Day headers
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                listOf("S", "M", "T", "W", "T", "F", "S").forEach { day ->
                    Text(
                        text = day,
                        style = MaaTypography.labelSmall,
                        color = MaaColors.textTertiary,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(MaaSpacing.small))

            // Calendar grid
            val firstDayOfMonth = currentMonth.atDay(1)
            val daysInMonth = currentMonth.lengthOfMonth()
            val startDayOfWeek = firstDayOfMonth.dayOfWeek.value % 7

            val calendarDays = buildList {
                repeat(startDayOfWeek) { add(null) }
                for (day in 1..daysInMonth) {
                    add(currentMonth.atDay(day))
                }
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(7),
                modifier = Modifier.height(((calendarDays.size / 7 + 1) * 44).dp),
                userScrollEnabled = false
            ) {
                items(calendarDays) { date ->
                    CalendarDay(
                        date = date,
                        isPeriod = date in periodDays,
                        isPredicted = date in predictedDays,
                        isFertile = date in fertileDays,
                        isOvulation = date == ovulationDay,
                        isSelected = date == selectedDate,
                        isToday = date == LocalDate.now(),
                        onClick = { date?.let { onDateSelected(it) } }
                    )
                }
            }
        }
    }
}

@Composable
private fun CalendarDay(
    date: LocalDate?,
    isPeriod: Boolean,
    isPredicted: Boolean,
    isFertile: Boolean,
    isOvulation: Boolean,
    isSelected: Boolean,
    isToday: Boolean,
    onClick: () -> Unit
) {
    if (date == null) {
        Box(modifier = Modifier.aspectRatio(1f))
        return
    }

    val backgroundColor = when {
        isPeriod -> MaaColors.stageReproductive
        isPredicted -> MaaColors.stageReproductive.copy(alpha = 0.3f)
        isOvulation -> MaaColors.stagePregnancy
        isFertile -> MaaColors.stagePregnancy.copy(alpha = 0.3f)
        else -> Color.Transparent
    }

    val textColor = when {
        isPeriod || isOvulation -> Color.White
        isToday -> MaaColors.accent
        else -> MaaColors.textPrimary
    }

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .padding(2.dp)
            .clip(CircleShape)
            .background(backgroundColor)
            .then(
                if (isSelected) {
                    Modifier.border(2.dp, MaaColors.accent, CircleShape)
                } else if (isToday && !isPeriod) {
                    Modifier.border(1.dp, MaaColors.accent, CircleShape)
                } else {
                    Modifier
                }
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = date.dayOfMonth.toString(),
            style = MaaTypography.bodySmall,
            color = textColor
        )
    }
}

@Composable
private fun QuickActionsSection(
    isPeriodActive: Boolean,
    onLogPeriodStart: () -> Unit,
    onLogPeriodEnd: () -> Unit,
    onLogSymptom: () -> Unit
) {
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
                text = "Quick Log",
                style = MaaTypography.titleSmall,
                color = MaaColors.textPrimary
            )

            Spacer(modifier = Modifier.height(MaaSpacing.small))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(MaaSpacing.small)
            ) {
                if (isPeriodActive) {
                    MaaButton(
                        text = "End Period",
                        onClick = onLogPeriodEnd,
                        style = MaaButtonStyle.SECONDARY,
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    MaaButton(
                        text = "Start Period",
                        onClick = onLogPeriodStart,
                        style = MaaButtonStyle.PRIMARY,
                        modifier = Modifier.weight(1f)
                    )
                }

                MaaButton(
                    text = "Log Symptom",
                    onClick = onLogSymptom,
                    style = MaaButtonStyle.OUTLINE,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun FlowIntensitySelector(
    selectedIntensity: FlowIntensity?,
    onIntensitySelected: (FlowIntensity) -> Unit
) {
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
                text = "Today's Flow",
                style = MaaTypography.titleSmall,
                color = MaaColors.textPrimary
            )

            Spacer(modifier = Modifier.height(MaaSpacing.small))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                FlowIntensity.entries.forEach { intensity ->
                    FlowIntensityChip(
                        intensity = intensity,
                        isSelected = intensity == selectedIntensity,
                        onClick = { onIntensitySelected(intensity) }
                    )
                }
            }
        }
    }
}

@Composable
private fun FlowIntensityChip(
    intensity: FlowIntensity,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val label = when (intensity) {
        FlowIntensity.SPOTTING -> "Spotting"
        FlowIntensity.LIGHT -> "Light"
        FlowIntensity.MEDIUM -> "Medium"
        FlowIntensity.HEAVY -> "Heavy"
    }

    Box(
        modifier = Modifier
            .clip(MaaShapes.small)
            .background(
                if (isSelected) MaaColors.stageReproductive else MaaColors.surfaceVariant
            )
            .clickable(onClick = onClick)
            .padding(horizontal = MaaSpacing.small, vertical = MaaSpacing.extraSmall),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaaTypography.labelSmall,
            color = if (isSelected) Color.White else MaaColors.textSecondary
        )
    }
}

@Composable
private fun CycleInsightsCard(
    averageCycleLength: Int?,
    averagePeriodLength: Int?,
    cyclesTracked: Int
) {
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
                text = "Your Cycle Insights",
                style = MaaTypography.titleSmall,
                color = MaaColors.textPrimary
            )

            Spacer(modifier = Modifier.height(MaaSpacing.medium))

            if (cyclesTracked < 2) {
                Text(
                    text = "Track at least 2 cycles to see your patterns",
                    style = MaaTypography.bodyMedium,
                    color = MaaColors.textSecondary
                )
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    InsightItem(
                        label = "Cycle Length",
                        value = "${averageCycleLength ?: "--"} days"
                    )
                    InsightItem(
                        label = "Period Length",
                        value = "${averagePeriodLength ?: "--"} days"
                    )
                    InsightItem(
                        label = "Cycles Tracked",
                        value = cyclesTracked.toString()
                    )
                }
            }
        }
    }
}

@Composable
private fun InsightItem(
    label: String,
    value: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaaTypography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaaColors.stageReproductive
        )
        Text(
            text = label,
            style = MaaTypography.labelSmall,
            color = MaaColors.textTertiary
        )
    }
}

@Composable
private fun CalendarLegend() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        LegendItem(color = MaaColors.stageReproductive, label = "Period")
        LegendItem(color = MaaColors.stageReproductive.copy(alpha = 0.3f), label = "Predicted")
        LegendItem(color = MaaColors.stagePregnancy.copy(alpha = 0.3f), label = "Fertile")
        LegendItem(color = MaaColors.stagePregnancy, label = "Ovulation")
    }
}

@Composable
private fun LegendItem(
    color: Color,
    label: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            style = MaaTypography.labelSmall,
            color = MaaColors.textTertiary
        )
    }
}

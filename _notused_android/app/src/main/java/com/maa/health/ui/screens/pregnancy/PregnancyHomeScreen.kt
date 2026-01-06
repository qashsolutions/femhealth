package com.maa.health.ui.screens.pregnancy

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.ChildCare
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.maa.health.ui.components.MaaButton
import com.maa.health.ui.components.MaaButtonSize
import com.maa.health.ui.components.MaaButtonVariant
import com.maa.health.ui.components.MaaCard
import com.maa.health.ui.components.MaaCardVariant
import com.maa.health.ui.components.MaaDangerSignCard
import com.maa.health.ui.theme.MaaColors
import com.maa.health.ui.theme.MaaShapes
import com.maa.health.ui.theme.MaaSpacing
import com.maa.health.ui.theme.MaaTypography
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/**
 * Pregnancy Home Screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PregnancyHomeScreen(
    onBack: () -> Unit,
    onNavigateToWeek: (Int) -> Unit,
    onNavigateToKickCounter: () -> Unit,
    onNavigateToContractionTimer: () -> Unit,
    onNavigateToDangerSigns: () -> Unit,
    onNavigateToChecklist: () -> Unit,
    viewModel: PregnancyViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showDatePicker by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaaColors.background)
    ) {
        TopAppBar(
            title = {
                Text(
                    text = "Pregnancy",
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

        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaaColors.accent)
            }
        } else if (!uiState.hasActivePregnancy) {
            // Setup pregnancy
            PregnancySetupContent(
                onSetupClick = { showDatePicker = true }
            )
        } else {
            // Pregnancy dashboard
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(MaaSpacing.large),
                verticalArrangement = Arrangement.spacedBy(MaaSpacing.medium)
            ) {
                // Week progress card
                item {
                    WeekProgressCard(
                        week = uiState.gestationalWeek,
                        day = uiState.gestationalDay,
                        trimester = uiState.trimester,
                        daysUntilDue = uiState.daysUntilDue,
                        babySize = uiState.weekInfo?.babySize ?: "",
                        onClick = { onNavigateToWeek(uiState.gestationalWeek) }
                    )
                }

                // Quick actions
                item {
                    Text(
                        text = "Quick Actions",
                        style = MaaTypography.titleSmall,
                        color = MaaColors.textPrimary
                    )
                    Spacer(modifier = Modifier.height(MaaSpacing.small))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(MaaSpacing.medium)
                    ) {
                        item {
                            QuickActionCard(
                                title = "Kick Counter",
                                icon = Icons.Default.TouchApp,
                                onClick = onNavigateToKickCounter
                            )
                        }
                        item {
                            QuickActionCard(
                                title = "Contractions",
                                icon = Icons.Default.Timer,
                                onClick = onNavigateToContractionTimer
                            )
                        }
                        item {
                            QuickActionCard(
                                title = "Checklist",
                                icon = Icons.Default.CalendarToday,
                                onClick = onNavigateToChecklist
                            )
                        }
                    }
                }

                // Week by week
                item {
                    Spacer(modifier = Modifier.height(MaaSpacing.small))
                    Text(
                        text = "This Week",
                        style = MaaTypography.titleSmall,
                        color = MaaColors.textPrimary
                    )
                    Spacer(modifier = Modifier.height(MaaSpacing.small))

                    uiState.weekInfo?.let { info ->
                        WeekInfoCard(
                            weekInfo = info,
                            onClick = { onNavigateToWeek(uiState.gestationalWeek) }
                        )
                    }
                }

                // Danger signs
                item {
                    Spacer(modifier = Modifier.height(MaaSpacing.small))
                    MaaDangerSignCard(
                        title = "Know the Danger Signs",
                        signs = listOf(
                            "Heavy vaginal bleeding",
                            "Severe headache with blurred vision",
                            "Reduced baby movements",
                            "Leaking fluid",
                            "Convulsions"
                        ),
                        onEmergencyClick = onNavigateToDangerSigns
                    )
                }
            }
        }
    }

    // Date picker dialog
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val date = Instant.ofEpochMilli(millis)
                                .atZone(ZoneId.systemDefault())
                                .toLocalDate()
                            viewModel.setLmpDate(date)
                            viewModel.createPregnancy()
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // Danger sign alert
    if (uiState.dangerSignDetected) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissDangerSignAlert() },
            title = { Text("Danger Sign Detected") },
            text = { Text(uiState.dangerSignMessage ?: "Please seek medical care immediately.") },
            confirmButton = {
                MaaButton(
                    text = "Get Help Now",
                    onClick = { /* Navigate to emergency */ },
                    variant = MaaButtonVariant.EMERGENCY
                )
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissDangerSignAlert() }) {
                    Text("Dismiss")
                }
            }
        )
    }
}

@Composable
private fun PregnancySetupContent(
    onSetupClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(MaaSpacing.large),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.ChildCare,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaaColors.stagePregnancy
        )

        Spacer(modifier = Modifier.height(MaaSpacing.large))

        Text(
            text = "Track Your Pregnancy",
            style = MaaTypography.headlineSmall,
            color = MaaColors.textPrimary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(MaaSpacing.small))

        Text(
            text = "Get week-by-week guidance, track your baby's growth, and monitor your health throughout pregnancy.",
            style = MaaTypography.bodyMedium,
            color = MaaColors.textSecondary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(MaaSpacing.extraLarge))

        MaaButton(
            text = "Start Tracking",
            onClick = onSetupClick,
            leadingIcon = Icons.Default.Add,
            size = MaaButtonSize.LARGE
        )

        Spacer(modifier = Modifier.height(MaaSpacing.medium))

        Text(
            text = "You'll need to know the date of your last menstrual period (LMP)",
            style = MaaTypography.labelSmall,
            color = MaaColors.textTertiary,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun WeekProgressCard(
    week: Int,
    day: Int,
    trimester: Int,
    daysUntilDue: Int,
    babySize: String,
    onClick: () -> Unit
) {
    MaaCard(
        modifier = Modifier.fillMaxWidth(),
        variant = MaaCardVariant.ELEVATED,
        backgroundColor = MaaColors.stagePregnancy,
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.padding(MaaSpacing.large)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        text = "Week $week, Day $day",
                        style = MaaTypography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaaColors.textPrimary
                    )
                    Text(
                        text = "Trimester $trimester",
                        style = MaaTypography.bodyMedium,
                        color = MaaColors.textSecondary
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "$daysUntilDue",
                        style = MaaTypography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaaColors.accent
                    )
                    Text(
                        text = "days to go",
                        style = MaaTypography.labelSmall,
                        color = MaaColors.textSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(MaaSpacing.medium))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.ChildCare,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = MaaColors.accent
                )
                Spacer(modifier = Modifier.width(MaaSpacing.small))
                Text(
                    text = "Baby is the size of a $babySize",
                    style = MaaTypography.bodyMedium,
                    color = MaaColors.textPrimary
                )
            }
        }
    }
}

@Composable
private fun QuickActionCard(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    MaaCard(
        modifier = Modifier.width(100.dp),
        variant = MaaCardVariant.DEFAULT,
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(MaaSpacing.medium),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(MaaShapes.small)
                    .background(MaaColors.stagePregnancy),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    modifier = Modifier.size(24.dp),
                    tint = MaaColors.textPrimary
                )
            }
            Spacer(modifier = Modifier.height(MaaSpacing.small))
            Text(
                text = title,
                style = MaaTypography.labelSmall,
                color = MaaColors.textPrimary,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun WeekInfoCard(
    weekInfo: com.maa.health.data.repository.WeekInfo,
    onClick: () -> Unit
) {
    MaaCard(
        modifier = Modifier.fillMaxWidth(),
        variant = MaaCardVariant.DEFAULT,
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.padding(MaaSpacing.medium)
        ) {
            Text(
                text = "Baby's Development",
                style = MaaTypography.titleSmall,
                color = MaaColors.textPrimary
            )
            Spacer(modifier = Modifier.height(MaaSpacing.small))

            weekInfo.developments.take(2).forEach { development ->
                Row(
                    modifier = Modifier.padding(vertical = MaaSpacing.extraSmall)
                ) {
                    Text(
                        text = "\u2022",
                        style = MaaTypography.bodySmall,
                        color = MaaColors.accent
                    )
                    Spacer(modifier = Modifier.width(MaaSpacing.small))
                    Text(
                        text = development,
                        style = MaaTypography.bodySmall,
                        color = MaaColors.textSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(MaaSpacing.medium))

            Text(
                text = "Your Body",
                style = MaaTypography.titleSmall,
                color = MaaColors.textPrimary
            )
            Spacer(modifier = Modifier.height(MaaSpacing.small))

            weekInfo.motherChanges.take(2).forEach { change ->
                Row(
                    modifier = Modifier.padding(vertical = MaaSpacing.extraSmall)
                ) {
                    Text(
                        text = "\u2022",
                        style = MaaTypography.bodySmall,
                        color = MaaColors.accent
                    )
                    Spacer(modifier = Modifier.width(MaaSpacing.small))
                    Text(
                        text = change,
                        style = MaaTypography.bodySmall,
                        color = MaaColors.textSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(MaaSpacing.small))
            Text(
                text = "View full week details",
                style = MaaTypography.labelMedium,
                color = MaaColors.accent
            )
        }
    }
}

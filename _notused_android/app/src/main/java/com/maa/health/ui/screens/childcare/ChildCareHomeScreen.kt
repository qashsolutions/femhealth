package com.maa.health.ui.screens.childcare

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
import androidx.compose.material.icons.filled.ChildCare
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Vaccines
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.maa.health.data.model.Child
import com.maa.health.data.model.VaccinationRecord
import com.maa.health.ui.components.MaaButton
import com.maa.health.ui.components.MaaButtonSize
import com.maa.health.ui.components.MaaCard
import com.maa.health.ui.components.MaaCardVariant
import com.maa.health.ui.components.UrgencyLevel
import com.maa.health.ui.theme.MaaColors
import com.maa.health.ui.theme.MaaShapes
import com.maa.health.ui.theme.MaaSpacing
import com.maa.health.ui.theme.MaaTypography

/**
 * Child Care Home Screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChildCareHomeScreen(
    onBack: () -> Unit,
    onNavigateToChild: (String) -> Unit,
    onNavigateToVaccinations: (String) -> Unit,
    onNavigateToGrowth: (String) -> Unit,
    onNavigateToSymptomCheck: (String) -> Unit,
    viewModel: ChildCareViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Child Care",
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
        },
        floatingActionButton = {
            if (uiState.children.isNotEmpty()) {
                FloatingActionButton(
                    onClick = { viewModel.showAddChildDialog() },
                    containerColor = MaaColors.accent
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add child",
                        tint = Color.White
                    )
                }
            }
        },
        containerColor = MaaColors.background
    ) { padding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaaColors.accent)
            }
        } else if (uiState.children.isEmpty()) {
            // No children - show setup
            NoChildrenContent(
                onAddChild = { viewModel.showAddChildDialog() },
                modifier = Modifier.padding(padding)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(MaaSpacing.large),
                verticalArrangement = Arrangement.spacedBy(MaaSpacing.medium)
            ) {
                // Child selector (if multiple children)
                if (uiState.children.size > 1) {
                    item {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(MaaSpacing.small)
                        ) {
                            items(uiState.children) { child ->
                                ChildChip(
                                    child = child,
                                    isSelected = child.id == uiState.selectedChild?.id,
                                    onClick = { viewModel.selectChild(child) }
                                )
                            }
                        }
                    }
                }

                // Selected child info
                uiState.selectedChild?.let { child ->
                    item {
                        ChildInfoCard(
                            child = child,
                            onClick = { onNavigateToChild(child.id) }
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
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(MaaSpacing.medium)
                        ) {
                            QuickActionCard(
                                title = "Vaccinations",
                                icon = Icons.Default.Vaccines,
                                badgeCount = uiState.pendingVaccinations.size,
                                onClick = { onNavigateToVaccinations(child.id) },
                                modifier = Modifier.weight(1f)
                            )
                            QuickActionCard(
                                title = "Growth",
                                icon = Icons.Default.MonitorWeight,
                                onClick = { onNavigateToGrowth(child.id) },
                                modifier = Modifier.weight(1f)
                            )
                            QuickActionCard(
                                title = "Symptom Check",
                                icon = Icons.Default.Warning,
                                onClick = { onNavigateToSymptomCheck(child.id) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    // Pending vaccinations
                    if (uiState.pendingVaccinations.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(MaaSpacing.small))
                            Text(
                                text = "Due Vaccinations",
                                style = MaaTypography.titleSmall,
                                color = MaaColors.textPrimary
                            )
                        }

                        items(uiState.pendingVaccinations.take(3)) { vaccine ->
                            VaccinationDueCard(
                                record = vaccine,
                                onClick = { viewModel.selectVaccination(vaccine) }
                            )
                        }

                        if (uiState.pendingVaccinations.size > 3) {
                            item {
                                TextButton(
                                    onClick = { onNavigateToVaccinations(child.id) }
                                ) {
                                    Text(
                                        text = "View all ${uiState.pendingVaccinations.size} due vaccinations",
                                        color = MaaColors.accent
                                    )
                                }
                            }
                        }
                    }

                    // Upcoming vaccinations
                    if (uiState.upcomingVaccinations.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(MaaSpacing.small))
                            Text(
                                text = "Upcoming Vaccinations",
                                style = MaaTypography.titleSmall,
                                color = MaaColors.textPrimary
                            )
                        }

                        items(uiState.upcomingVaccinations.take(2)) { vaccine ->
                            VaccinationUpcomingCard(
                                record = vaccine,
                                onClick = { onNavigateToVaccinations(child.id) }
                            )
                        }
                    }

                    // Growth chart preview
                    if (uiState.growthHistory.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(MaaSpacing.small))
                            GrowthPreviewCard(
                                latestMeasurement = uiState.growthHistory.first(),
                                onClick = { onNavigateToGrowth(child.id) }
                            )
                        }
                    }
                }
            }
        }
    }

    // Add child dialog would go here
    if (uiState.showAddChildDialog) {
        // AddChildDialog composable
    }

    // Danger sign alert
    if (uiState.dangerSignDetected) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissDangerSignAlert() },
            title = { Text("Danger Sign Detected") },
            text = {
                Column {
                    Text(uiState.dangerSignMessage ?: "Please seek medical care for your child immediately.")
                    Spacer(modifier = Modifier.height(MaaSpacing.small))
                    uiState.dangerSigns.forEach { sign ->
                        Text("• $sign", color = MaaColors.danger)
                    }
                }
            },
            confirmButton = {
                MaaButton(
                    text = "Get Help Now",
                    onClick = { /* Navigate to emergency */ },
                    variant = com.maa.health.ui.components.MaaButtonVariant.EMERGENCY
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
private fun NoChildrenContent(
    onAddChild: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(MaaSpacing.large),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.ChildCare,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaaColors.stageChildCare
        )

        Spacer(modifier = Modifier.height(MaaSpacing.large))

        Text(
            text = "Track Your Child's Health",
            style = MaaTypography.headlineSmall,
            color = MaaColors.textPrimary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(MaaSpacing.small))

        Text(
            text = "Add your child to track vaccinations, growth milestones, and get health guidance.",
            style = MaaTypography.bodyMedium,
            color = MaaColors.textSecondary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(MaaSpacing.extraLarge))

        MaaButton(
            text = "Add Child",
            onClick = onAddChild,
            leadingIcon = Icons.Default.Add,
            size = MaaButtonSize.LARGE
        )
    }
}

@Composable
private fun ChildChip(
    child: Child,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    MaaCard(
        variant = if (isSelected) MaaCardVariant.ELEVATED else MaaCardVariant.OUTLINED,
        backgroundColor = if (isSelected) MaaColors.stageChildCare else MaaColors.surface,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(horizontal = MaaSpacing.medium, vertical = MaaSpacing.small),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaaColors.textPrimary
            )
            Spacer(modifier = Modifier.width(MaaSpacing.small))
            Text(
                text = child.name ?: "Child",
                style = MaaTypography.labelMedium,
                color = MaaColors.textPrimary
            )
        }
    }
}

@Composable
private fun ChildInfoCard(
    child: Child,
    onClick: () -> Unit
) {
    MaaCard(
        modifier = Modifier.fillMaxWidth(),
        variant = MaaCardVariant.ELEVATED,
        backgroundColor = MaaColors.stageChildCare,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(MaaSpacing.medium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(MaaShapes.small)
                    .background(MaaColors.surface),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ChildCare,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = MaaColors.accent
                )
            }

            Spacer(modifier = Modifier.width(MaaSpacing.medium))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = child.name ?: "Your Child",
                    style = MaaTypography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaaColors.textPrimary
                )
                Text(
                    text = "${child.ageMonths} months old",
                    style = MaaTypography.bodyMedium,
                    color = MaaColors.textSecondary
                )
                child.currentWeight?.let { weight ->
                    Text(
                        text = "Weight: ${String.format("%.1f", weight)} kg",
                        style = MaaTypography.bodySmall,
                        color = MaaColors.textTertiary
                    )
                }
            }
        }
    }
}

@Composable
private fun QuickActionCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    badgeCount: Int = 0
) {
    MaaCard(
        modifier = modifier,
        variant = MaaCardVariant.DEFAULT,
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(MaaSpacing.medium),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    modifier = Modifier.size(28.dp),
                    tint = MaaColors.accent
                )
                if (badgeCount > 0) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .size(16.dp)
                            .clip(MaaShapes.extraSmall)
                            .background(MaaColors.danger),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = badgeCount.toString(),
                            style = MaaTypography.labelSmall,
                            color = Color.White
                        )
                    }
                }
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
private fun VaccinationDueCard(
    record: VaccinationRecord,
    onClick: () -> Unit
) {
    MaaCard(
        modifier = Modifier.fillMaxWidth(),
        variant = MaaCardVariant.URGENCY,
        backgroundColor = MaaColors.warningLight,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(MaaSpacing.medium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Vaccines,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaaColors.warning
            )
            Spacer(modifier = Modifier.width(MaaSpacing.medium))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = record.vaccineType.name.replace("_", " "),
                    style = MaaTypography.titleSmall,
                    color = MaaColors.textPrimary
                )
                Text(
                    text = "Due: ${record.dueDate}",
                    style = MaaTypography.bodySmall,
                    color = MaaColors.warning
                )
            }
        }
    }
}

@Composable
private fun VaccinationUpcomingCard(
    record: VaccinationRecord,
    onClick: () -> Unit
) {
    MaaCard(
        modifier = Modifier.fillMaxWidth(),
        variant = MaaCardVariant.DEFAULT,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(MaaSpacing.medium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Vaccines,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaaColors.info
            )
            Spacer(modifier = Modifier.width(MaaSpacing.medium))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = record.vaccineType.name.replace("_", " "),
                    style = MaaTypography.titleSmall,
                    color = MaaColors.textPrimary
                )
                Text(
                    text = "Scheduled: ${record.dueDate}",
                    style = MaaTypography.bodySmall,
                    color = MaaColors.textSecondary
                )
            }
        }
    }
}

@Composable
private fun GrowthPreviewCard(
    latestMeasurement: com.maa.health.data.model.GrowthMeasurement,
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
                text = "Latest Growth",
                style = MaaTypography.titleSmall,
                color = MaaColors.textPrimary
            )
            Spacer(modifier = Modifier.height(MaaSpacing.small))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${String.format("%.1f", latestMeasurement.weight)} kg",
                        style = MaaTypography.titleMedium,
                        color = MaaColors.accent
                    )
                    Text(
                        text = "Weight",
                        style = MaaTypography.labelSmall,
                        color = MaaColors.textSecondary
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${String.format("%.1f", latestMeasurement.height)} cm",
                        style = MaaTypography.titleMedium,
                        color = MaaColors.accent
                    )
                    Text(
                        text = "Height",
                        style = MaaTypography.labelSmall,
                        color = MaaColors.textSecondary
                    )
                }
            }
            Spacer(modifier = Modifier.height(MaaSpacing.small))
            Text(
                text = "Recorded: ${latestMeasurement.date}",
                style = MaaTypography.labelSmall,
                color = MaaColors.textTertiary
            )
        }
    }
}

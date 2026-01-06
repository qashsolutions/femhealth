package com.maa.health.ui.screens.care

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChildCare
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.LocalPharmacy
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PregnantWoman
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Vaccines
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.maa.health.ui.theme.MaaColors
import com.maa.health.ui.theme.MaaShapes
import com.maa.health.ui.theme.MaaSpacing
import com.maa.health.ui.theme.MaaTypography

/**
 * Care Hub Screen - Central hub for care management
 *
 * Features:
 * - Pregnancy care tracking
 * - Child profiles and care
 * - Vaccination schedules
 * - Medication reminders
 * - Appointment scheduling
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CareHubScreen(
    onNavigateToPregnancy: () -> Unit,
    onNavigateToChildCare: () -> Unit,
    onNavigateToVaccinations: () -> Unit,
    onNavigateToMedications: () -> Unit,
    onNavigateToAppointments: () -> Unit,
    onAddChild: () -> Unit
) {
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
                    Column {
                        Text(
                            text = "Care",
                            style = MaaTypography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaaColors.textPrimary
                        )
                        Text(
                            text = "Manage your family's health",
                            style = MaaTypography.bodySmall,
                            color = MaaColors.textSecondary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaaColors.background
                )
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(MaaSpacing.medium),
                verticalArrangement = Arrangement.spacedBy(MaaSpacing.medium)
            ) {
                // Care modules
                item {
                    Text(
                        text = "Care Modules",
                        style = MaaTypography.titleMedium,
                        color = MaaColors.textPrimary
                    )
                    Spacer(modifier = Modifier.height(MaaSpacing.small))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(MaaSpacing.medium)
                    ) {
                        CareModuleCard(
                            modifier = Modifier.weight(1f),
                            title = "Pregnancy",
                            titleHindi = "गर्भावस्था",
                            subtitle = "Track your journey",
                            icon = Icons.Default.PregnantWoman,
                            color = MaaColors.stagePregnancy,
                            onClick = onNavigateToPregnancy
                        )
                        CareModuleCard(
                            modifier = Modifier.weight(1f),
                            title = "Child Care",
                            titleHindi = "शिशु देखभाल",
                            subtitle = "Track growth & health",
                            icon = Icons.Default.ChildCare,
                            color = MaaColors.stageChildCare,
                            onClick = onNavigateToChildCare
                        )
                    }
                }

                // Children profiles
                item {
                    Spacer(modifier = Modifier.height(MaaSpacing.small))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Children",
                            style = MaaTypography.titleMedium,
                            color = MaaColors.textPrimary
                        )
                        Text(
                            text = "+ Add child",
                            style = MaaTypography.labelMedium,
                            color = MaaColors.accent,
                            modifier = Modifier
                                .clip(MaaShapes.small)
                                .padding(MaaSpacing.extraSmall)
                        )
                    }
                    Spacer(modifier = Modifier.height(MaaSpacing.small))

                    // Empty state or children list
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaaColors.surface),
                        shape = MaaShapes.medium
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(MaaSpacing.large),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.ChildCare,
                                contentDescription = null,
                                tint = MaaColors.textTertiary,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(MaaSpacing.small))
                            Text(
                                text = "No children added yet",
                                style = MaaTypography.bodyMedium,
                                color = MaaColors.textSecondary
                            )
                            Text(
                                text = "Add your child to track vaccinations and growth",
                                style = MaaTypography.bodySmall,
                                color = MaaColors.textTertiary
                            )
                        }
                    }
                }

                // Quick actions
                item {
                    Spacer(modifier = Modifier.height(MaaSpacing.small))
                    Text(
                        text = "Quick Actions",
                        style = MaaTypography.titleMedium,
                        color = MaaColors.textPrimary
                    )
                    Spacer(modifier = Modifier.height(MaaSpacing.small))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(MaaSpacing.medium)
                    ) {
                        items(
                            listOf(
                                QuickAction("Vaccinations", "टीकाकरण", Icons.Default.Vaccines, MaaColors.success, onNavigateToVaccinations),
                                QuickAction("Medications", "दवाइयाँ", Icons.Default.LocalPharmacy, MaaColors.info, onNavigateToMedications),
                                QuickAction("Appointments", "अपॉइंटमेंट", Icons.Default.CalendarMonth, MaaColors.warning, onNavigateToAppointments)
                            )
                        ) { action ->
                            QuickActionCard(action = action)
                        }
                    }
                }

                // Upcoming reminders
                item {
                    Spacer(modifier = Modifier.height(MaaSpacing.small))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Upcoming Reminders",
                            style = MaaTypography.titleMedium,
                            color = MaaColors.textPrimary
                        )
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = null,
                            tint = MaaColors.textTertiary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(MaaSpacing.small))

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
                            ReminderItem(
                                title = "Iron supplement",
                                time = "Today, 8:00 PM",
                                type = ReminderType.MEDICATION
                            )
                            Spacer(modifier = Modifier.height(MaaSpacing.small))
                            ReminderItem(
                                title = "Prenatal checkup",
                                time = "Tomorrow, 10:00 AM",
                                type = ReminderType.APPOINTMENT
                            )
                            Spacer(modifier = Modifier.height(MaaSpacing.small))
                            ReminderItem(
                                title = "Folic acid",
                                time = "Daily, 9:00 AM",
                                type = ReminderType.MEDICATION
                            )
                        }
                    }
                }

                // Bottom spacing for FAB
                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }

        // FAB for adding new item
        FloatingActionButton(
            onClick = onAddChild,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(MaaSpacing.large),
            containerColor = MaaColors.accent,
            contentColor = MaaColors.onAccent
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add child or reminder"
            )
        }
    }
}

@Composable
private fun CareModuleCard(
    modifier: Modifier = Modifier,
    title: String,
    titleHindi: String,
    subtitle: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.15f)),
        shape = MaaShapes.medium
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(MaaSpacing.medium)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = color,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.height(MaaSpacing.medium))
            Text(
                text = title,
                style = MaaTypography.titleSmall,
                color = MaaColors.textPrimary
            )
            Text(
                text = titleHindi,
                style = MaaTypography.bodySmall,
                color = MaaColors.textSecondary
            )
            Spacer(modifier = Modifier.height(MaaSpacing.extraSmall))
            Text(
                text = subtitle,
                style = MaaTypography.labelSmall,
                color = MaaColors.textTertiary
            )
        }
    }
}

private data class QuickAction(
    val title: String,
    val titleHindi: String,
    val icon: ImageVector,
    val color: Color,
    val onClick: () -> Unit
)

@Composable
private fun QuickActionCard(action: QuickAction) {
    Card(
        onClick = action.onClick,
        modifier = Modifier.width(120.dp),
        colors = CardDefaults.cardColors(containerColor = MaaColors.surface),
        shape = MaaShapes.medium
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(MaaSpacing.medium),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = action.icon,
                contentDescription = action.title,
                tint = action.color,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.height(MaaSpacing.small))
            Text(
                text = action.title,
                style = MaaTypography.labelMedium,
                color = MaaColors.textPrimary
            )
            Text(
                text = action.titleHindi,
                style = MaaTypography.bodySmall,
                color = MaaColors.textSecondary
            )
        }
    }
}

private enum class ReminderType {
    MEDICATION,
    APPOINTMENT,
    VACCINATION
}

@Composable
private fun ReminderItem(
    title: String,
    time: String,
    type: ReminderType
) {
    val (icon, color) = when (type) {
        ReminderType.MEDICATION -> Icons.Default.LocalPharmacy to MaaColors.info
        ReminderType.APPOINTMENT -> Icons.Default.CalendarMonth to MaaColors.warning
        ReminderType.VACCINATION -> Icons.Default.Vaccines to MaaColors.success
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(modifier = Modifier.width(MaaSpacing.medium))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaaTypography.labelMedium,
                color = MaaColors.textPrimary
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = null,
                    tint = MaaColors.textTertiary,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(MaaSpacing.extraSmall))
                Text(
                    text = time,
                    style = MaaTypography.bodySmall,
                    color = MaaColors.textSecondary
                )
            }
        }
    }
}

package com.maa.health.ui.screens.track

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
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MoodBad
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.maa.health.service.VoiceAction
import com.maa.health.service.VoiceService
import com.maa.health.ui.components.FloatingVoiceFAB
import com.maa.health.ui.theme.MaaColors
import com.maa.health.ui.theme.MaaShapes
import com.maa.health.ui.theme.MaaSpacing
import com.maa.health.ui.theme.MaaTypography

/**
 * Track Hub Screen - Central hub for all health tracking
 *
 * Features:
 * - Quick access to symptom logging (body map)
 * - Cycle tracking entry point
 * - Mood logging
 * - Recent logs summary
 * - Voice-first input via FAB
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackHubScreen(
    onNavigateToBodyMap: () -> Unit,
    onNavigateToCycle: () -> Unit,
    onNavigateToMood: () -> Unit,
    onNavigateToSymptomHistory: () -> Unit,
    voiceService: VoiceService? = null,
    onVoiceInput: ((String) -> Unit)? = null
) {
    var showVoiceHint by remember { mutableStateOf(true) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaaColors.background)
    ) {
        TopAppBar(
            title = {
                Column {
                    Text(
                        text = "Track",
                        style = MaaTypography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaaColors.textPrimary
                    )
                    Text(
                        text = "Log symptoms, cycle & mood",
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
            // Quick actions
            item {
                Text(
                    text = "Quick Log",
                    style = MaaTypography.titleMedium,
                    color = MaaColors.textPrimary
                )
                Spacer(modifier = Modifier.height(MaaSpacing.small))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(MaaSpacing.medium)
                ) {
                    items(
                        listOf(
                            TrackAction("Body Map", "Log symptoms", Icons.Default.TouchApp, MaaColors.accent, onNavigateToBodyMap),
                            TrackAction("Cycle", "Period tracker", Icons.Default.Timeline, MaaColors.stageReproductive, onNavigateToCycle),
                            TrackAction("Mood", "How are you?", Icons.Default.MoodBad, MaaColors.stageMentalHealth, onNavigateToMood)
                        )
                    ) { action ->
                        QuickActionCard(action = action)
                    }
                }
            }

            // Today's summary
            item {
                Spacer(modifier = Modifier.height(MaaSpacing.medium))
                Text(
                    text = "Today's Summary",
                    style = MaaTypography.titleMedium,
                    color = MaaColors.textPrimary
                )
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
                        SummaryItem(
                            label = "Symptoms logged",
                            value = "0",
                            icon = Icons.Default.TouchApp
                        )
                        Spacer(modifier = Modifier.height(MaaSpacing.small))
                        SummaryItem(
                            label = "Cycle day",
                            value = "Day 14",
                            icon = Icons.Default.Timeline
                        )
                        Spacer(modifier = Modifier.height(MaaSpacing.small))
                        SummaryItem(
                            label = "Mood",
                            value = "Not logged",
                            icon = Icons.Default.FavoriteBorder
                        )
                    }
                }
            }

            // Recent activity
            item {
                Spacer(modifier = Modifier.height(MaaSpacing.medium))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recent Activity",
                        style = MaaTypography.titleMedium,
                        color = MaaColors.textPrimary
                    )
                    Text(
                        text = "See all →",
                        style = MaaTypography.labelMedium,
                        color = MaaColors.accent,
                        modifier = Modifier
                            .clip(MaaShapes.small)
                            .padding(MaaSpacing.extraSmall)
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
                            .padding(MaaSpacing.medium),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = null,
                            tint = MaaColors.textTertiary,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(MaaSpacing.small))
                        Text(
                            text = "No recent activity",
                            style = MaaTypography.bodyMedium,
                            color = MaaColors.textSecondary
                        )
                        Text(
                            text = "Start tracking to see your health patterns",
                            style = MaaTypography.bodySmall,
                            color = MaaColors.textTertiary
                        )
                    }
                }
            }
        }

        // Voice FAB
        voiceService?.let { service ->
            FloatingVoiceFAB(
                voiceService = service,
                voiceAction = VoiceAction.LOG_SYMPTOM,
                onTranscriptionConfirmed = { text ->
                    onVoiceInput?.invoke(text)
                },
                onEditRequested = { text ->
                    // Allow user to edit before submitting
                    onVoiceInput?.invoke(text)
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(MaaSpacing.large)
            )
        }
    }
}

private data class TrackAction(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val color: Color,
    val onClick: () -> Unit
)

@Composable
private fun QuickActionCard(action: TrackAction) {
    Card(
        onClick = action.onClick,
        modifier = Modifier.width(140.dp),
        colors = CardDefaults.cardColors(containerColor = action.color.copy(alpha = 0.15f)),
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
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(MaaSpacing.small))
            Text(
                text = action.title,
                style = MaaTypography.labelLarge,
                color = MaaColors.textPrimary
            )
            Text(
                text = action.subtitle,
                style = MaaTypography.bodySmall,
                color = MaaColors.textSecondary
            )
        }
    }
}

@Composable
private fun SummaryItem(
    label: String,
    value: String,
    icon: ImageVector
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaaColors.textTertiary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(MaaSpacing.small))
        Text(
            text = label,
            style = MaaTypography.bodyMedium,
            color = MaaColors.textSecondary,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            style = MaaTypography.labelMedium,
            color = MaaColors.textPrimary
        )
    }
}

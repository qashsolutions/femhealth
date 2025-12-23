package com.maa.health.ui.screens.mentalhealth

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.maa.health.data.model.Mood
import com.maa.health.service.VoiceAction
import com.maa.health.service.VoiceService
import com.maa.health.ui.components.FloatingVoiceFAB
import com.maa.health.ui.components.MaaButton
import com.maa.health.ui.components.MaaButtonStyle
import com.maa.health.ui.theme.MaaColors
import com.maa.health.ui.theme.MaaShapes
import com.maa.health.ui.theme.MaaSpacing
import com.maa.health.ui.theme.MaaTypography
import kotlinx.coroutines.delay

/**
 * Mental Health Home Screen
 *
 * Features:
 * - Quick mood logging with emoji-based UI
 * - Breathing exercise for relaxation
 * - Mental health screenings (EPDS, PHQ-9, GAD-7)
 * - Mood history and patterns
 * - Crisis resources
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MentalHealthHomeScreen(
    onBack: () -> Unit,
    onCrisisSupport: () -> Unit,
    viewModel: MentalHealthViewModel = hiltViewModel(),
    voiceService: VoiceService? = null,
    onVoiceMoodInput: ((String) -> Unit)? = null
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
                    text = "Mental Wellbeing",
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
            actions = {
                IconButton(onClick = onCrisisSupport) {
                    Icon(
                        imageVector = Icons.Default.Call,
                        contentDescription = "Crisis Support",
                        tint = MaaColors.error
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
            // Daily check-in greeting
            GreetingCard(userName = uiState.userName)

            Spacer(modifier = Modifier.height(MaaSpacing.medium))

            // Mood logger
            MoodLoggerCard(
                selectedMood = uiState.selectedMood,
                onMoodSelected = { viewModel.selectMood(it) },
                onLogMood = { viewModel.logMood() }
            )

            Spacer(modifier = Modifier.height(MaaSpacing.medium))

            // Breathing exercise
            if (uiState.isBreathingExerciseActive) {
                BreathingExerciseCard(
                    phase = uiState.breathingPhase,
                    secondsRemaining = uiState.breathingSecondsRemaining,
                    onStop = { viewModel.stopBreathingExercise() }
                )
            } else {
                QuickExerciseCard(
                    onStartBreathing = { viewModel.startBreathingExercise() }
                )
            }

            Spacer(modifier = Modifier.height(MaaSpacing.medium))

            // Screening section
            ScreeningsCard(
                availableScreenings = uiState.availableScreenings,
                onStartScreening = { viewModel.startScreening(it) }
            )

            // Active screening
            if (uiState.isScreeningActive && uiState.currentQuestion != null) {
                Spacer(modifier = Modifier.height(MaaSpacing.medium))
                ScreeningQuestionCard(
                    screeningType = uiState.activeScreeningType ?: "",
                    question = uiState.currentQuestion!!,
                    currentIndex = uiState.currentQuestionIndex,
                    totalQuestions = uiState.totalQuestions,
                    onAnswer = { viewModel.answerQuestion(it) }
                )
            }

            // Screening result
            if (uiState.screeningResult != null) {
                Spacer(modifier = Modifier.height(MaaSpacing.medium))
                ScreeningResultCard(
                    result = uiState.screeningResult!!,
                    onDismiss = { viewModel.dismissResult() }
                )
            }

            Spacer(modifier = Modifier.height(MaaSpacing.medium))

            // Mood history
            MoodHistoryCard(
                recentMoods = uiState.recentMoods
            )

            Spacer(modifier = Modifier.height(MaaSpacing.medium))

            // Crisis support reminder
            CrisisSupportCard(onTap = onCrisisSupport)

            Spacer(modifier = Modifier.height(80.dp))
        }
        }

        // Voice FAB for mood input
        voiceService?.let { service ->
            FloatingVoiceFAB(
                voiceService = service,
                voiceAction = VoiceAction.LOG_MOOD,
                onTranscriptionConfirmed = { text ->
                    onVoiceMoodInput?.invoke(text)
                },
                onEditRequested = { text ->
                    onVoiceMoodInput?.invoke(text)
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(MaaSpacing.large)
            )
        }
    }
}

@Composable
private fun GreetingCard(userName: String?) {
    val greeting = remember {
        val hour = java.time.LocalTime.now().hour
        when {
            hour < 12 -> "Good Morning"
            hour < 17 -> "Good Afternoon"
            else -> "Good Evening"
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaaColors.stageMentalHealth.copy(alpha = 0.15f)
        ),
        shape = MaaShapes.medium
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(MaaSpacing.medium)
        ) {
            Text(
                text = if (userName != null) "$greeting, $userName" else greeting,
                style = MaaTypography.titleMedium,
                color = MaaColors.textPrimary
            )
            Spacer(modifier = Modifier.height(MaaSpacing.extraSmall))
            Text(
                text = "How are you feeling today?",
                style = MaaTypography.bodyMedium,
                color = MaaColors.textSecondary
            )
        }
    }
}

@Composable
private fun MoodLoggerCard(
    selectedMood: Mood?,
    onMoodSelected: (Mood) -> Unit,
    onLogMood: () -> Unit
) {
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
            Text(
                text = "Tap to log your mood",
                style = MaaTypography.titleSmall,
                color = MaaColors.textPrimary
            )

            Spacer(modifier = Modifier.height(MaaSpacing.medium))

            // Mood emoji row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Mood.entries.forEach { mood ->
                    MoodButton(
                        mood = mood,
                        isSelected = mood == selectedMood,
                        onClick = { onMoodSelected(mood) }
                    )
                }
            }

            if (selectedMood != null) {
                Spacer(modifier = Modifier.height(MaaSpacing.medium))

                Text(
                    text = "Feeling ${selectedMood.label.lowercase()}",
                    style = MaaTypography.bodyMedium,
                    color = MaaColors.textSecondary
                )

                Spacer(modifier = Modifier.height(MaaSpacing.small))

                MaaButton(
                    text = "Log Mood",
                    onClick = onLogMood,
                    style = MaaButtonStyle.PRIMARY
                )
            }
        }
    }
}

@Composable
private fun MoodButton(
    mood: Mood,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.2f else 1f,
        animationSpec = tween(200),
        label = "mood_scale"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(MaaShapes.small)
            .clickable(onClick = onClick)
            .padding(MaaSpacing.small)
    ) {
        Text(
            text = mood.emoji,
            fontSize = 32.sp,
            modifier = Modifier.scale(scale)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = mood.label,
            style = MaaTypography.labelSmall,
            color = if (isSelected) MaaColors.accent else MaaColors.textTertiary
        )
    }
}

@Composable
private fun QuickExerciseCard(
    onStartBreathing: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaaColors.surface),
        shape = MaaShapes.medium
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onStartBreathing)
                .padding(MaaSpacing.medium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaaColors.stageMentalHealth.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = null,
                    tint = MaaColors.stageMentalHealth,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(MaaSpacing.medium))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Breathing Exercise",
                    style = MaaTypography.titleSmall,
                    color = MaaColors.textPrimary
                )
                Text(
                    text = "Take 1 minute to relax and breathe",
                    style = MaaTypography.bodySmall,
                    color = MaaColors.textSecondary
                )
            }

            Text(
                text = "Start →",
                style = MaaTypography.labelMedium,
                color = MaaColors.accent
            )
        }
    }
}

@Composable
private fun BreathingExerciseCard(
    phase: BreathingPhase,
    secondsRemaining: Int,
    onStop: () -> Unit
) {
    val circleScale by animateFloatAsState(
        targetValue = when (phase) {
            BreathingPhase.INHALE -> 1.3f
            BreathingPhase.HOLD -> 1.3f
            BreathingPhase.EXHALE -> 1f
        },
        animationSpec = tween(phase.durationSeconds * 1000),
        label = "breathing_scale"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaaColors.stageMentalHealth.copy(alpha = 0.15f)
        ),
        shape = MaaShapes.medium
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(MaaSpacing.large),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = phase.instruction,
                style = MaaTypography.headlineSmall,
                color = MaaColors.stageMentalHealth
            )

            Spacer(modifier = Modifier.height(MaaSpacing.large))

            // Breathing circle
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .scale(circleScale)
                    .clip(CircleShape)
                    .background(MaaColors.stageMentalHealth.copy(alpha = 0.3f))
                    .border(3.dp, MaaColors.stageMentalHealth, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = secondsRemaining.toString(),
                    style = MaaTypography.displaySmall,
                    color = MaaColors.stageMentalHealth
                )
            }

            Spacer(modifier = Modifier.height(MaaSpacing.large))

            MaaButton(
                text = "Stop",
                onClick = onStop,
                style = MaaButtonStyle.OUTLINE
            )
        }
    }
}

@Composable
private fun ScreeningsCard(
    availableScreenings: List<String>,
    onStartScreening: (String) -> Unit
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
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Mental Health Check",
                    style = MaaTypography.titleSmall,
                    color = MaaColors.textPrimary
                )
                Spacer(modifier = Modifier.width(MaaSpacing.extraSmall))
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = MaaColors.textTertiary,
                    modifier = Modifier.size(16.dp)
                )
            }

            Spacer(modifier = Modifier.height(MaaSpacing.extraSmall))

            Text(
                text = "Quick screenings to understand how you're feeling",
                style = MaaTypography.bodySmall,
                color = MaaColors.textSecondary
            )

            Spacer(modifier = Modifier.height(MaaSpacing.medium))

            availableScreenings.forEach { screening ->
                ScreeningItem(
                    name = screening,
                    description = getScreeningDescription(screening),
                    onClick = { onStartScreening(screening) }
                )
                if (screening != availableScreenings.last()) {
                    Spacer(modifier = Modifier.height(MaaSpacing.small))
                }
            }
        }
    }
}

@Composable
private fun ScreeningItem(
    name: String,
    description: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaaShapes.small)
            .background(MaaColors.surfaceVariant)
            .clickable(onClick = onClick)
            .padding(MaaSpacing.small),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = name,
                style = MaaTypography.labelMedium,
                color = MaaColors.textPrimary
            )
            Text(
                text = description,
                style = MaaTypography.bodySmall,
                color = MaaColors.textTertiary
            )
        }
        Text(
            text = "Take →",
            style = MaaTypography.labelSmall,
            color = MaaColors.accent
        )
    }
}

@Composable
private fun ScreeningQuestionCard(
    screeningType: String,
    question: ScreeningQuestion,
    currentIndex: Int,
    totalQuestions: Int,
    onAnswer: (Int) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaaColors.stageMentalHealth.copy(alpha = 0.1f)
        ),
        shape = MaaShapes.medium
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(MaaSpacing.medium)
        ) {
            // Progress
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = screeningType,
                    style = MaaTypography.labelMedium,
                    color = MaaColors.stageMentalHealth
                )
                Text(
                    text = "${currentIndex + 1} / $totalQuestions",
                    style = MaaTypography.labelSmall,
                    color = MaaColors.textTertiary
                )
            }

            Spacer(modifier = Modifier.height(MaaSpacing.extraSmall))

            LinearProgressIndicator(
                progress = { (currentIndex + 1).toFloat() / totalQuestions },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(MaaShapes.extraSmall),
                color = MaaColors.stageMentalHealth,
                trackColor = MaaColors.surfaceVariant
            )

            Spacer(modifier = Modifier.height(MaaSpacing.medium))

            Text(
                text = question.text,
                style = MaaTypography.bodyLarge,
                color = MaaColors.textPrimary
            )

            Spacer(modifier = Modifier.height(MaaSpacing.medium))

            // Answer options
            question.options.forEachIndexed { index, option ->
                AnswerOption(
                    text = option,
                    onClick = { onAnswer(index) }
                )
                if (index < question.options.lastIndex) {
                    Spacer(modifier = Modifier.height(MaaSpacing.small))
                }
            }
        }
    }
}

@Composable
private fun AnswerOption(
    text: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaaShapes.small)
            .background(MaaColors.surface)
            .border(1.dp, MaaColors.border, MaaShapes.small)
            .clickable(onClick = onClick)
            .padding(MaaSpacing.small)
    ) {
        Text(
            text = text,
            style = MaaTypography.bodyMedium,
            color = MaaColors.textPrimary
        )
    }
}

@Composable
private fun ScreeningResultCard(
    result: ScreeningResult,
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = result.severity.color.copy(alpha = 0.15f)
        ),
        shape = MaaShapes.medium
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(MaaSpacing.medium)
        ) {
            Text(
                text = "Screening Complete",
                style = MaaTypography.titleSmall,
                color = MaaColors.textPrimary
            )

            Spacer(modifier = Modifier.height(MaaSpacing.small))

            Text(
                text = result.interpretation,
                style = MaaTypography.bodyMedium,
                color = MaaColors.textSecondary
            )

            Spacer(modifier = Modifier.height(MaaSpacing.small))

            Text(
                text = result.recommendation,
                style = MaaTypography.bodySmall,
                color = MaaColors.textTertiary
            )

            Spacer(modifier = Modifier.height(MaaSpacing.medium))

            Text(
                text = "Score: ${result.score}",
                style = MaaTypography.labelMedium,
                color = result.severity.color
            )

            Spacer(modifier = Modifier.height(MaaSpacing.medium))

            MaaButton(
                text = "Done",
                onClick = onDismiss,
                style = MaaButtonStyle.PRIMARY,
                fullWidth = true
            )
        }
    }
}

@Composable
private fun MoodHistoryCard(
    recentMoods: List<MoodEntry>
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
                text = "Recent Moods",
                style = MaaTypography.titleSmall,
                color = MaaColors.textPrimary
            )

            Spacer(modifier = Modifier.height(MaaSpacing.small))

            if (recentMoods.isEmpty()) {
                Text(
                    text = "No moods logged yet. Start tracking today!",
                    style = MaaTypography.bodyMedium,
                    color = MaaColors.textSecondary
                )
            } else {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(MaaSpacing.medium)
                ) {
                    items(recentMoods.take(7)) { entry ->
                        MoodHistoryItem(entry = entry)
                    }
                }
            }
        }
    }
}

@Composable
private fun MoodHistoryItem(entry: MoodEntry) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = entry.mood.emoji,
            fontSize = 24.sp
        )
        Text(
            text = entry.dayLabel,
            style = MaaTypography.labelSmall,
            color = MaaColors.textTertiary
        )
    }
}

@Composable
private fun CrisisSupportCard(onTap: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onTap),
        colors = CardDefaults.cardColors(
            containerColor = MaaColors.error.copy(alpha = 0.1f)
        ),
        shape = MaaShapes.medium
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(MaaSpacing.medium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Call,
                contentDescription = null,
                tint = MaaColors.error,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(MaaSpacing.medium))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Need to talk to someone?",
                    style = MaaTypography.titleSmall,
                    color = MaaColors.textPrimary
                )
                Text(
                    text = "24/7 crisis helpline available",
                    style = MaaTypography.bodySmall,
                    color = MaaColors.textSecondary
                )
            }
        }
    }
}

private fun getScreeningDescription(screening: String): String {
    return when (screening) {
        "EPDS" -> "Postnatal depression screening"
        "PHQ-9" -> "Depression assessment"
        "GAD-7" -> "Anxiety assessment"
        else -> "Mental health check"
    }
}

// Data classes for UI state
data class MoodEntry(
    val mood: Mood,
    val dayLabel: String
)

data class ScreeningResult(
    val score: Int,
    val interpretation: String,
    val recommendation: String,
    val severity: ResultSeverity
)

enum class ResultSeverity(val color: Color) {
    MINIMAL(MaaColors.success),
    MILD(MaaColors.warning),
    MODERATE(MaaColors.warning),
    SEVERE(MaaColors.error)
}

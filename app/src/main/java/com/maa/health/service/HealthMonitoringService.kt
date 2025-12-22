package com.maa.health.service

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.maa.health.data.model.HealthIssue
import com.maa.health.data.model.MonitoredCondition
import com.maa.health.data.model.Severity
import com.maa.health.data.model.SymptomType
import com.maa.health.data.model.Trend
import com.maa.health.data.repository.CycleRepository
import com.maa.health.data.repository.MoodRepository
import com.maa.health.data.repository.SymptomRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Health Monitoring Service
 *
 * Monitors user health data and identifies:
 * - Pattern anomalies
 * - Danger sign triggers
 * - Recurring symptoms
 * - Mental health trends
 * - Cycle irregularities
 *
 * Triggers AI-powered push notifications when issues are detected
 */
@Singleton
class HealthMonitoringService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val notificationHelper: NotificationHelper,
    private val symptomRepository: SymptomRepository,
    private val moodRepository: MoodRepository,
    private val cycleRepository: CycleRepository
) {
    companion object {
        private const val WORK_HEALTH_MONITORING = "health_monitoring_work"
        private const val MONITORING_INTERVAL_HOURS = 6L

        // Thresholds for alerts
        private const val DANGER_SIGN_THRESHOLD = 1  // Any danger sign triggers alert
        private const val RECURRING_SYMPTOM_THRESHOLD = 3  // 3+ occurrences in 7 days
        private const val MOOD_DECLINE_THRESHOLD = 3  // 3+ days of declining mood
        private const val CYCLE_IRREGULARITY_THRESHOLD = 7  // 7+ days variance
    }

    /**
     * Initialize periodic health monitoring
     */
    fun startMonitoring() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .setRequiresBatteryNotLow(true)
            .build()

        val workRequest = PeriodicWorkRequestBuilder<HealthMonitoringWorker>(
            MONITORING_INTERVAL_HOURS, TimeUnit.HOURS
        )
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WORK_HEALTH_MONITORING,
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }

    /**
     * Stop health monitoring
     */
    fun stopMonitoring() {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_HEALTH_MONITORING)
    }

    /**
     * Analyze health data and return detected issues
     */
    suspend fun analyzeHealthData(userId: String): List<HealthIssue> {
        val issues = mutableListOf<HealthIssue>()

        // Check for danger signs
        issues.addAll(checkDangerSigns(userId))

        // Check for recurring symptoms
        issues.addAll(checkRecurringSymptoms(userId))

        // Check mood trends
        issues.addAll(checkMoodTrends(userId))

        // Check cycle irregularities
        issues.addAll(checkCycleIrregularities(userId))

        return issues
    }

    /**
     * Check for any logged danger signs
     */
    private suspend fun checkDangerSigns(userId: String): List<HealthIssue> {
        val issues = mutableListOf<HealthIssue>()
        val last24Hours = Instant.now().minus(Duration.ofHours(24))

        try {
            val recentSymptoms = symptomRepository.getRecentSymptoms(userId, last24Hours).first()

            recentSymptoms.filter { it.symptomType.isDangerSign }.forEach { symptom ->
                issues.add(
                    HealthIssue(
                        type = HealthIssueType.DANGER_SIGN,
                        severity = Severity.SEVERE,
                        title = "Danger Sign Detected",
                        description = "You logged ${symptom.symptomType.name.lowercase().replace("_", " ")}. " +
                                "This may require immediate medical attention.",
                        recommendation = "Please consult a healthcare provider immediately.",
                        timestamp = symptom.timestamp,
                        requiresImmediateAction = true
                    )
                )
            }
        } catch (e: Exception) {
            // Handle repository errors gracefully
        }

        return issues
    }

    /**
     * Check for recurring symptoms
     */
    private suspend fun checkRecurringSymptoms(userId: String): List<HealthIssue> {
        val issues = mutableListOf<HealthIssue>()
        val last7Days = Instant.now().minus(Duration.ofDays(7))

        try {
            val recentSymptoms = symptomRepository.getRecentSymptoms(userId, last7Days).first()

            // Group by symptom type and count
            val symptomCounts = recentSymptoms.groupingBy { it.symptomType }.eachCount()

            symptomCounts.filter { it.value >= RECURRING_SYMPTOM_THRESHOLD }.forEach { (symptomType, count) ->
                issues.add(
                    HealthIssue(
                        type = HealthIssueType.RECURRING_SYMPTOM,
                        severity = Severity.MODERATE,
                        title = "Recurring Symptom Pattern",
                        description = "You've logged ${symptomType.name.lowercase().replace("_", " ")} " +
                                "$count times in the past week.",
                        recommendation = "Consider tracking this symptom and discussing with your doctor.",
                        timestamp = Instant.now(),
                        requiresImmediateAction = false
                    )
                )
            }
        } catch (e: Exception) {
            // Handle repository errors gracefully
        }

        return issues
    }

    /**
     * Check mood trends for concerning patterns
     */
    private suspend fun checkMoodTrends(userId: String): List<HealthIssue> {
        val issues = mutableListOf<HealthIssue>()

        try {
            val pattern = moodRepository.getMoodPattern(userId).first()

            pattern?.let {
                // Check for declining trend
                if (it.moodTrend == Trend.DECLINING) {
                    issues.add(
                        HealthIssue(
                            type = HealthIssueType.MOOD_DECLINE,
                            severity = Severity.MODERATE,
                            title = "Mood Pattern Notice",
                            description = "Your mood has been trending downward recently. " +
                                    "Average mood score: ${String.format("%.1f", it.averageMoodScore)}/5",
                            recommendation = "Consider taking a mental health screening or speaking with a counselor.",
                            timestamp = Instant.now(),
                            requiresImmediateAction = false
                        )
                    )
                }

                // Check for PMDD correlation
                it.cycleMoodCorrelation?.let { correlation ->
                    if (correlation > 0.7f) {
                        issues.add(
                            HealthIssue(
                                type = HealthIssueType.PMDD_INDICATOR,
                                severity = Severity.MILD,
                                title = "Cycle-Mood Correlation",
                                description = "Your mood appears to correlate strongly with your menstrual cycle.",
                                recommendation = "This could indicate PMDD. Consider discussing with your doctor.",
                                timestamp = Instant.now(),
                                requiresImmediateAction = false
                            )
                        )
                    }
                }
            }
        } catch (e: Exception) {
            // Handle repository errors gracefully
        }

        return issues
    }

    /**
     * Check for cycle irregularities
     */
    private suspend fun checkCycleIrregularities(userId: String): List<HealthIssue> {
        val issues = mutableListOf<HealthIssue>()

        try {
            val pattern = cycleRepository.getCyclePattern(userId).first()

            pattern?.let {
                // Check for irregular cycles
                if (it.irregularityFlag) {
                    issues.add(
                        HealthIssue(
                            type = HealthIssueType.CYCLE_IRREGULARITY,
                            severity = Severity.MILD,
                            title = "Cycle Irregularity Detected",
                            description = "Your cycle pattern shows significant variability " +
                                    "(±${it.cycleVariability} days).",
                            recommendation = "Irregular cycles can have many causes. Consider tracking more and discussing with your doctor.",
                            timestamp = Instant.now(),
                            requiresImmediateAction = false
                        )
                    )
                }

                // Check for PCOS risk
                if (it.pcosRiskFlag) {
                    issues.add(
                        HealthIssue(
                            type = HealthIssueType.PCOS_RISK,
                            severity = Severity.MODERATE,
                            title = "PCOS Risk Indicator",
                            description = "Your cycle pattern shows indicators that may suggest PCOS.",
                            recommendation = "Consider scheduling a checkup with your gynecologist.",
                            timestamp = Instant.now(),
                            requiresImmediateAction = false
                        )
                    )
                }
            }
        } catch (e: Exception) {
            // Handle repository errors gracefully
        }

        return issues
    }

    /**
     * Process detected issues and trigger notifications
     */
    suspend fun processAndNotify(userId: String) {
        val issues = analyzeHealthData(userId)

        issues.forEach { issue ->
            when (issue.type) {
                HealthIssueType.DANGER_SIGN -> {
                    notificationHelper.showDangerSignAlert(
                        symptom = issue.title,
                        urgency = issue.severity.name,
                        notificationId = MaaFirebaseMessagingService.getNextNotificationId()
                    )
                }
                HealthIssueType.RECURRING_SYMPTOM,
                HealthIssueType.MOOD_DECLINE,
                HealthIssueType.PMDD_INDICATOR,
                HealthIssueType.CYCLE_IRREGULARITY,
                HealthIssueType.PCOS_RISK -> {
                    notificationHelper.showHealthInsight(
                        title = issue.title,
                        insight = issue.description,
                        notificationId = MaaFirebaseMessagingService.getNextNotificationId()
                    )
                }
            }
        }
    }
}

/**
 * Types of health issues that can be detected
 */
enum class HealthIssueType {
    DANGER_SIGN,
    RECURRING_SYMPTOM,
    MOOD_DECLINE,
    PMDD_INDICATOR,
    CYCLE_IRREGULARITY,
    PCOS_RISK
}

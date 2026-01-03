package com.maa.health.service

import android.content.Context
import com.maa.health.data.model.Severity
import com.maa.health.data.model.SymptomType
import com.maa.health.data.model.Trend
import com.maa.health.data.repository.CycleRepository
import com.maa.health.data.repository.MoodRepository
import com.maa.health.data.repository.SymptomRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for HealthMonitoringService
 */
class HealthMonitoringServiceTest {

    private lateinit var context: Context
    private lateinit var notificationHelper: NotificationHelper
    private lateinit var symptomRepository: SymptomRepository
    private lateinit var moodRepository: MoodRepository
    private lateinit var cycleRepository: CycleRepository
    private lateinit var healthMonitoringService: HealthMonitoringService

    @Before
    fun setup() {
        context = mockk(relaxed = true)
        notificationHelper = mockk(relaxed = true)
        symptomRepository = mockk(relaxed = true)
        moodRepository = mockk(relaxed = true)
        cycleRepository = mockk(relaxed = true)

        healthMonitoringService = HealthMonitoringService(
            context,
            notificationHelper,
            symptomRepository,
            moodRepository,
            cycleRepository
        )
    }

    @Test
    fun `analyzeHealthData returns empty list when no issues`() = runTest {
        // Given
        val userId = "test-user-123"
        coEvery { symptomRepository.getRecentSymptoms(userId, any()) } returns flowOf(emptyList())
        coEvery { moodRepository.getMoodPattern(userId) } returns flowOf(null)
        coEvery { cycleRepository.getCyclePattern(userId) } returns flowOf(null)

        // When
        val issues = healthMonitoringService.analyzeHealthData(userId)

        // Then
        assertTrue(issues.isEmpty())
    }

    @Test
    fun `analyzeHealthData detects danger signs`() = runTest {
        // Given
        val userId = "test-user-123"
        val dangerSymptom = createDangerSymptom()
        coEvery { symptomRepository.getRecentSymptoms(userId, any()) } returns flowOf(listOf(dangerSymptom))
        coEvery { moodRepository.getMoodPattern(userId) } returns flowOf(null)
        coEvery { cycleRepository.getCyclePattern(userId) } returns flowOf(null)

        // When
        val issues = healthMonitoringService.analyzeHealthData(userId)

        // Then
        assertTrue(issues.any { it.type == HealthIssueType.DANGER_SIGN })
        assertTrue(issues.any { it.severity == Severity.SEVERE })
    }

    @Test
    fun `analyzeHealthData detects recurring symptoms`() = runTest {
        // Given
        val userId = "test-user-123"
        // 3+ occurrences triggers recurring symptom alert
        val recurringSymptoms = (1..4).map { createRegularSymptom("log$it") }
        coEvery { symptomRepository.getRecentSymptoms(userId, any()) } returns flowOf(recurringSymptoms)
        coEvery { moodRepository.getMoodPattern(userId) } returns flowOf(null)
        coEvery { cycleRepository.getCyclePattern(userId) } returns flowOf(null)

        // When
        val issues = healthMonitoringService.analyzeHealthData(userId)

        // Then
        assertTrue(issues.any { it.type == HealthIssueType.RECURRING_SYMPTOM })
    }

    @Test
    fun `analyzeHealthData detects mood decline`() = runTest {
        // Given
        val userId = "test-user-123"
        val decliningMoodPattern = createMoodPatternWithTrend(Trend.DECLINING)
        coEvery { symptomRepository.getRecentSymptoms(userId, any()) } returns flowOf(emptyList())
        coEvery { moodRepository.getMoodPattern(userId) } returns flowOf(decliningMoodPattern)
        coEvery { cycleRepository.getCyclePattern(userId) } returns flowOf(null)

        // When
        val issues = healthMonitoringService.analyzeHealthData(userId)

        // Then
        assertTrue(issues.any { it.type == HealthIssueType.MOOD_DECLINE })
    }

    @Test
    fun `analyzeHealthData detects PMDD indicator`() = runTest {
        // Given
        val userId = "test-user-123"
        val pmddPattern = createMoodPatternWithCycleCorrelation(0.8f)
        coEvery { symptomRepository.getRecentSymptoms(userId, any()) } returns flowOf(emptyList())
        coEvery { moodRepository.getMoodPattern(userId) } returns flowOf(pmddPattern)
        coEvery { cycleRepository.getCyclePattern(userId) } returns flowOf(null)

        // When
        val issues = healthMonitoringService.analyzeHealthData(userId)

        // Then
        assertTrue(issues.any { it.type == HealthIssueType.PMDD_INDICATOR })
    }

    @Test
    fun `analyzeHealthData detects cycle irregularity`() = runTest {
        // Given
        val userId = "test-user-123"
        val irregularCyclePattern = createIrregularCyclePattern()
        coEvery { symptomRepository.getRecentSymptoms(userId, any()) } returns flowOf(emptyList())
        coEvery { moodRepository.getMoodPattern(userId) } returns flowOf(null)
        coEvery { cycleRepository.getCyclePattern(userId) } returns flowOf(irregularCyclePattern)

        // When
        val issues = healthMonitoringService.analyzeHealthData(userId)

        // Then
        assertTrue(issues.any { it.type == HealthIssueType.CYCLE_IRREGULARITY })
    }

    @Test
    fun `analyzeHealthData detects PCOS risk`() = runTest {
        // Given
        val userId = "test-user-123"
        val pcosRiskPattern = createPcosRiskCyclePattern()
        coEvery { symptomRepository.getRecentSymptoms(userId, any()) } returns flowOf(emptyList())
        coEvery { moodRepository.getMoodPattern(userId) } returns flowOf(null)
        coEvery { cycleRepository.getCyclePattern(userId) } returns flowOf(pcosRiskPattern)

        // When
        val issues = healthMonitoringService.analyzeHealthData(userId)

        // Then
        assertTrue(issues.any { it.type == HealthIssueType.PCOS_RISK })
    }

    // Helper functions to create test data

    private fun createDangerSymptom(): SymptomLogWithType {
        return SymptomLogWithType(
            id = "danger-1",
            symptomType = SymptomType.BLEEDING,  // A danger sign for pregnancy
            timestamp = java.time.Instant.now()
        )
    }

    private fun createRegularSymptom(id: String): SymptomLogWithType {
        return SymptomLogWithType(
            id = id,
            symptomType = SymptomType.HEADACHE,  // Regular symptom
            timestamp = java.time.Instant.now()
        )
    }

    private fun createMoodPatternWithTrend(trend: Trend): MoodPatternData {
        return MoodPatternData(
            averageMoodScore = 2.5f,
            moodTrend = trend,
            cycleMoodCorrelation = null
        )
    }

    private fun createMoodPatternWithCycleCorrelation(correlation: Float): MoodPatternData {
        return MoodPatternData(
            averageMoodScore = 3.0f,
            moodTrend = Trend.STABLE,
            cycleMoodCorrelation = correlation
        )
    }

    private fun createIrregularCyclePattern(): CyclePatternData {
        return CyclePatternData(
            averageCycleLength = 32,
            cycleVariability = 10,  // High variability
            irregularityFlag = true,
            pcosRiskFlag = false
        )
    }

    private fun createPcosRiskCyclePattern(): CyclePatternData {
        return CyclePatternData(
            averageCycleLength = 45,  // Long cycles
            cycleVariability = 15,
            irregularityFlag = true,
            pcosRiskFlag = true
        )
    }

    // Data classes for test helpers
    data class SymptomLogWithType(
        val id: String,
        val symptomType: SymptomType,
        val timestamp: java.time.Instant
    )

    data class MoodPatternData(
        val averageMoodScore: Float,
        val moodTrend: Trend,
        val cycleMoodCorrelation: Float?
    )

    data class CyclePatternData(
        val averageCycleLength: Int,
        val cycleVariability: Int,
        val irregularityFlag: Boolean,
        val pcosRiskFlag: Boolean
    )
}

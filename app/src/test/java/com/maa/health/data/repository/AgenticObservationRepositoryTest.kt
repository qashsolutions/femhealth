package com.maa.health.data.repository

import com.maa.health.data.local.dao.UserInteractionDao
import com.maa.health.data.local.entity.ContentFeedbackEntity
import com.maa.health.data.local.entity.ObservationHistoryEntity
import com.maa.health.data.local.entity.UserInteractionEntity
import com.maa.health.data.model.LifecycleStage
import com.maa.health.data.remote.ai.GeminiObservationService
import com.maa.health.data.remote.ai.MoodPatternData
import com.maa.health.data.remote.ai.ObservationType
import com.maa.health.data.remote.ai.PatternObservation
import com.maa.health.data.remote.medical.MedlinePlusService
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.time.Instant

/**
 * Unit tests for AgenticObservationRepository
 *
 * Tests the agentic learning system's core functionality:
 * - Recording user interactions
 * - Processing feedback
 * - Generating personalized observations
 * - Learning from user behavior
 */
class AgenticObservationRepositoryTest {

    private lateinit var userInteractionDao: UserInteractionDao
    private lateinit var geminiService: GeminiObservationService
    private lateinit var medlinePlusService: MedlinePlusService
    private lateinit var moodRepository: MoodRepository
    private lateinit var symptomRepository: SymptomRepository
    private lateinit var cycleRepository: CycleRepository
    private lateinit var userRepository: UserRepository
    private lateinit var repository: AgenticObservationRepository

    @Before
    fun setup() {
        userInteractionDao = mockk(relaxed = true)
        geminiService = mockk(relaxed = true)
        medlinePlusService = mockk(relaxed = true)
        moodRepository = mockk(relaxed = true)
        symptomRepository = mockk(relaxed = true)
        cycleRepository = mockk(relaxed = true)
        userRepository = mockk(relaxed = true)

        repository = AgenticObservationRepository(
            userInteractionDao = userInteractionDao,
            geminiService = geminiService,
            medlinePlusService = medlinePlusService,
            moodRepository = moodRepository,
            symptomRepository = symptomRepository,
            cycleRepository = cycleRepository,
            userRepository = userRepository
        )
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // INTERACTION TRACKING TESTS
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun `recordInteraction stores interaction in database`() = runTest {
        // Given
        val userId = "user-123"
        val interactionType = "SYMPTOM_LOG"
        val targetId = "symptom-456"

        // When
        repository.recordInteraction(
            userId = userId,
            interactionType = interactionType,
            targetId = targetId,
            targetType = "symptom",
            metadata = mapOf("severity" to "MODERATE")
        )

        // Then
        coVerify {
            userInteractionDao.insertInteraction(match {
                it.userId == userId &&
                it.interactionType == interactionType &&
                it.targetId == targetId
            })
        }
    }

    @Test
    fun `recordInteraction includes timestamp`() = runTest {
        // Given
        val userId = "user-123"
        val beforeTime = Instant.now().toEpochMilli()

        // When
        repository.recordInteraction(
            userId = userId,
            interactionType = "MOOD_LOG",
            targetId = null,
            targetType = null,
            metadata = null
        )

        val afterTime = Instant.now().toEpochMilli()

        // Then
        coVerify {
            userInteractionDao.insertInteraction(match {
                it.timestamp >= beforeTime && it.timestamp <= afterTime
            })
        }
    }

    @Test
    fun `recordInteraction handles null metadata gracefully`() = runTest {
        // Given
        val userId = "user-123"

        // When/Then - should not throw
        repository.recordInteraction(
            userId = userId,
            interactionType = "ARTICLE_VIEW",
            targetId = "article-789",
            targetType = "article",
            metadata = null
        )

        coVerify { userInteractionDao.insertInteraction(any()) }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // FEEDBACK PROCESSING TESTS
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun `recordFeedback stores positive feedback`() = runTest {
        // Given
        val userId = "user-123"
        val contentId = "observation-456"

        // When
        repository.recordFeedback(
            userId = userId,
            contentId = contentId,
            contentType = "OBSERVATION",
            wasHelpful = true,
            feedbackText = "This was very useful!"
        )

        // Then
        coVerify {
            userInteractionDao.insertFeedback(match {
                it.userId == userId &&
                it.contentId == contentId &&
                it.wasHelpful == true
            })
        }
    }

    @Test
    fun `recordFeedback stores negative feedback`() = runTest {
        // Given
        val userId = "user-123"
        val contentId = "recommendation-789"

        // When
        repository.recordFeedback(
            userId = userId,
            contentId = contentId,
            contentType = "RECOMMENDATION",
            wasHelpful = false,
            feedbackText = "Not relevant to me"
        )

        // Then
        coVerify {
            userInteractionDao.insertFeedback(match {
                it.wasHelpful == false &&
                it.feedbackText == "Not relevant to me"
            })
        }
    }

    @Test
    fun `getHelpfulContentHistory returns helpful content only`() = runTest {
        // Given
        val userId = "user-123"
        val helpfulContent = listOf(
            ContentFeedbackEntity(
                id = "fb-1",
                userId = userId,
                contentId = "c-1",
                contentType = "ARTICLE",
                contentTopic = "pregnancy",
                wasHelpful = true,
                feedbackText = null,
                timestamp = Instant.now().toEpochMilli()
            )
        )
        coEvery { userInteractionDao.getHelpfulContent(userId, any()) } returns helpfulContent

        // When
        val result = repository.getHelpfulContentHistory(userId)

        // Then
        assertEquals(1, result.size)
        assertTrue(result.all { it.wasHelpful })
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // OBSERVATION GENERATION TESTS
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun `getMoodObservation returns observation from Gemini`() = runTest {
        // Given
        val userId = "user-123"
        val mockObservation = PatternObservation(
            type = ObservationType.MOOD_PATTERN,
            observation = "I notice your mood has been improving over the past week.",
            dataPoints = 7,
            timeSpan = "7 days",
            suggestsFollowUp = false
        )

        coEvery { moodRepository.getMoodLogsForPeriod(userId, any()) } returns emptyList()
        coEvery { userRepository.getUserProfile(userId) } returns UserProfileForAI(
            age = 28,
            lifecycleStage = LifecycleStage.REPRODUCTIVE
        )
        coEvery { geminiService.analyzeMoodPatterns(any()) } returns Result.success(mockObservation)

        // When
        val result = repository.getMoodObservation(userId)

        // Then
        assertTrue(result.isSuccess)
        assertEquals("I notice your mood has been improving over the past week.", result.getOrNull()?.observation)
    }

    @Test
    fun `getMoodObservation stores observation in history`() = runTest {
        // Given
        val userId = "user-123"
        val mockObservation = PatternObservation(
            type = ObservationType.MOOD_PATTERN,
            observation = "Your sleep quality correlates with your mood.",
            dataPoints = 14,
            timeSpan = "14 days",
            suggestsFollowUp = true
        )

        coEvery { moodRepository.getMoodLogsForPeriod(userId, any()) } returns emptyList()
        coEvery { userRepository.getUserProfile(userId) } returns UserProfileForAI(
            age = 30,
            lifecycleStage = LifecycleStage.PREGNANCY
        )
        coEvery { geminiService.analyzeMoodPatterns(any()) } returns Result.success(mockObservation)

        // When
        repository.getMoodObservation(userId)

        // Then
        coVerify {
            userInteractionDao.insertObservation(match {
                it.userId == userId &&
                it.observationType == "MOOD_PATTERN" &&
                it.suggestsFollowUp == true
            })
        }
    }

    @Test
    fun `getMoodObservation handles API failure gracefully`() = runTest {
        // Given
        val userId = "user-123"
        coEvery { moodRepository.getMoodLogsForPeriod(userId, any()) } returns emptyList()
        coEvery { userRepository.getUserProfile(userId) } returns UserProfileForAI(
            age = 25,
            lifecycleStage = LifecycleStage.REPRODUCTIVE
        )
        coEvery { geminiService.analyzeMoodPatterns(any()) } returns Result.failure(Exception("API Error"))

        // When
        val result = repository.getMoodObservation(userId)

        // Then
        assertTrue(result.isFailure)
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // LEARNING & PERSONALIZATION TESTS
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun `getInteractionPatterns analyzes user behavior`() = runTest {
        // Given
        val userId = "user-123"
        val interactions = listOf(
            createInteraction(userId, "SYMPTOM_LOG"),
            createInteraction(userId, "SYMPTOM_LOG"),
            createInteraction(userId, "MOOD_LOG"),
            createInteraction(userId, "ARTICLE_VIEW"),
            createInteraction(userId, "SYMPTOM_LOG")
        )
        coEvery { userInteractionDao.getRecentInteractions(userId, any()) } returns interactions

        // When
        val patterns = repository.getInteractionPatterns(userId)

        // Then
        assertEquals("SYMPTOM_LOG", patterns.mostFrequentInteraction)
        assertEquals(3, patterns.interactionCounts["SYMPTOM_LOG"])
    }

    @Test
    fun `getContentPreferences learns from feedback`() = runTest {
        // Given
        val userId = "user-123"
        val feedback = listOf(
            createFeedback(userId, "ARTICLE", true),
            createFeedback(userId, "ARTICLE", true),
            createFeedback(userId, "OBSERVATION", false),
            createFeedback(userId, "ARTICLE", true)
        )
        coEvery { userInteractionDao.getRecentFeedback(userId, any()) } returns feedback

        // When
        val preferences = repository.getContentPreferences(userId)

        // Then
        assertTrue(preferences.preferredTypes.contains("ARTICLE"))
        assertTrue(preferences.helpfulRate["ARTICLE"]!! > 0.9f)
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // DANGER SIGN DETECTION TESTS
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun `detectsConcerningPatterns flags declining mood trend`() = runTest {
        // Given
        val userId = "user-123"
        coEvery { moodRepository.getMoodTrend(userId) } returns "DECLINING"
        coEvery { moodRepository.getAverageMoodScore(userId) } returns 2.0f

        // When
        val concerns = repository.detectConcerningPatterns(userId)

        // Then
        assertTrue(concerns.hasConcerningPatterns)
        assertTrue(concerns.patterns.any { it.contains("mood", ignoreCase = true) })
    }

    @Test
    fun `detectsConcerningPatterns suggests follow-up for low mood`() = runTest {
        // Given
        val userId = "user-123"
        coEvery { moodRepository.getMoodTrend(userId) } returns "STABLE"
        coEvery { moodRepository.getAverageMoodScore(userId) } returns 1.5f  // Very low

        // When
        val concerns = repository.detectConcerningPatterns(userId)

        // Then
        assertTrue(concerns.suggestsSeekingCare)
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // HELPER FUNCTIONS
    // ═══════════════════════════════════════════════════════════════════════════

    private fun createInteraction(
        userId: String,
        type: String
    ): UserInteractionEntity {
        return UserInteractionEntity(
            id = java.util.UUID.randomUUID().toString(),
            userId = userId,
            timestamp = Instant.now().toEpochMilli(),
            interactionType = type,
            targetId = null,
            targetType = null,
            metadata = null,
            sessionId = null,
            durationMs = null
        )
    }

    private fun createFeedback(
        userId: String,
        contentType: String,
        wasHelpful: Boolean
    ): ContentFeedbackEntity {
        return ContentFeedbackEntity(
            id = java.util.UUID.randomUUID().toString(),
            userId = userId,
            contentId = java.util.UUID.randomUUID().toString(),
            contentType = contentType,
            contentTopic = null,
            wasHelpful = wasHelpful,
            feedbackText = null,
            timestamp = Instant.now().toEpochMilli()
        )
    }
}

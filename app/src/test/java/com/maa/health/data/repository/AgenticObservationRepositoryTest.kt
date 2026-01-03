package com.maa.health.data.repository

import com.maa.health.data.local.dao.UserInteractionDao
import com.maa.health.data.local.entity.ContentFeedbackEntity
import com.maa.health.data.local.entity.ObservationHistoryEntity
import com.maa.health.data.local.entity.UserInteractionEntity
import com.maa.health.data.model.LifecycleStage
import com.maa.health.data.model.MoodLog
import com.maa.health.data.remote.ai.GeminiObservationService
import com.maa.health.data.remote.ai.MoodPatternData
import com.maa.health.data.remote.ai.ObservationType
import com.maa.health.data.remote.ai.PatternObservation
import com.maa.health.data.remote.ai.PersonalizedObservation
import com.maa.health.data.remote.medical.MedlinePlusService
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.time.Instant
import java.util.UUID

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
            type = interactionType,
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
        val beforeTime = System.currentTimeMillis()

        // When
        repository.recordInteraction(
            userId = userId,
            type = "MOOD_LOG",
            targetId = null,
            targetType = null,
            metadata = null
        )

        val afterTime = System.currentTimeMillis()

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
            type = "ARTICLE_VIEW",
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
            contentTopic = "mood",
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
            contentTopic = "nutrition",
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
    fun `recordFeedback also records as interaction`() = runTest {
        // Given
        val userId = "user-123"

        // When
        repository.recordFeedback(
            userId = userId,
            contentId = "content-1",
            contentType = "ARTICLE",
            contentTopic = "pregnancy",
            wasHelpful = true
        )

        // Then - should insert both feedback and interaction
        coVerify {
            userInteractionDao.insertFeedback(any())
            userInteractionDao.insertInteraction(match {
                it.interactionType == "FEEDBACK_GIVEN"
            })
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // MOOD PATTERN ANALYSIS TESTS
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun `analyzeMoodPatterns returns observation from Gemini`() = runTest {
        // Given
        val userId = "user-123"
        val mockObservation = PatternObservation(
            type = ObservationType.MOOD_PATTERN,
            observation = "I notice your mood has been improving over the past week.",
            dataPoints = 7,
            timeSpan = "7 days",
            suggestsFollowUp = false
        )

        coEvery { moodRepository.getMoodLogsForPeriod(userId, any()) } returns createMockMoodLogs(7)
        coEvery { userRepository.getUserProfile(userId) } returns UserProfileForAI(
            age = 28,
            lifecycleStage = LifecycleStage.REPRODUCTIVE,
            isPregnant = false,
            isPostpartum = false,
            hasChildren = false,
            preferredLanguage = "en"
        )
        coEvery { geminiService.analyzeMoodPatterns(any()) } returns Result.success(mockObservation)

        // When
        val result = repository.analyzeMoodPatterns(userId)

        // Then
        assertTrue(result.isSuccess)
        assertEquals("I notice your mood has been improving over the past week.", result.getOrNull()?.observation)
    }

    @Test
    fun `analyzeMoodPatterns stores observation in history`() = runTest {
        // Given
        val userId = "user-123"
        val mockObservation = PatternObservation(
            type = ObservationType.MOOD_PATTERN,
            observation = "Your sleep quality correlates with your mood.",
            dataPoints = 14,
            timeSpan = "14 days",
            suggestsFollowUp = true
        )

        coEvery { moodRepository.getMoodLogsForPeriod(userId, any()) } returns createMockMoodLogs(14)
        coEvery { userRepository.getUserProfile(userId) } returns UserProfileForAI(
            age = 30,
            lifecycleStage = LifecycleStage.PREGNANCY,
            isPregnant = true,
            isPostpartum = false,
            hasChildren = false,
            preferredLanguage = "en"
        )
        coEvery { geminiService.analyzeMoodPatterns(any()) } returns Result.success(mockObservation)

        // When
        repository.analyzeMoodPatterns(userId)

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
    fun `analyzeMoodPatterns handles insufficient data`() = runTest {
        // Given
        val userId = "user-123"
        coEvery { moodRepository.getMoodLogsForPeriod(userId, any()) } returns createMockMoodLogs(2) // Only 2 entries
        coEvery { userRepository.getUserProfile(userId) } returns UserProfileForAI(
            age = 25,
            lifecycleStage = LifecycleStage.REPRODUCTIVE,
            isPregnant = false,
            isPostpartum = false,
            hasChildren = false,
            preferredLanguage = "en"
        )

        // When
        val result = repository.analyzeMoodPatterns(userId)

        // Then
        assertTrue(result.isSuccess)
        assertTrue(result.getOrNull()?.observation?.contains("Keep tracking") == true)
    }

    @Test
    fun `analyzeMoodPatterns handles API failure gracefully`() = runTest {
        // Given
        val userId = "user-123"
        coEvery { moodRepository.getMoodLogsForPeriod(userId, any()) } returns createMockMoodLogs(10)
        coEvery { userRepository.getUserProfile(userId) } returns UserProfileForAI(
            age = 25,
            lifecycleStage = LifecycleStage.REPRODUCTIVE,
            isPregnant = false,
            isPostpartum = false,
            hasChildren = false,
            preferredLanguage = "en"
        )
        coEvery { geminiService.analyzeMoodPatterns(any()) } returns Result.failure(Exception("API Error"))

        // When
        val result = repository.analyzeMoodPatterns(userId)

        // Then
        assertTrue(result.isFailure)
    }

    @Test
    fun `analyzeMoodPatterns returns failure when user not found`() = runTest {
        // Given
        val userId = "nonexistent-user"
        coEvery { userRepository.getUserProfile(userId) } returns null

        // When
        val result = repository.analyzeMoodPatterns(userId)

        // Then
        assertTrue(result.isFailure)
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // PERSONALIZED CONTENT TESTS
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun `getPersonalizedContent returns content based on lifecycle stage`() = runTest {
        // Given
        val userId = "user-123"
        coEvery { userRepository.getUserProfile(userId) } returns UserProfileForAI(
            age = 28,
            lifecycleStage = LifecycleStage.PREGNANCY,
            isPregnant = true,
            isPostpartum = false,
            hasChildren = false,
            preferredLanguage = "en"
        )
        coEvery { userInteractionDao.getRecentInteractions(userId, any()) } returns emptyList()
        coEvery { userInteractionDao.getInteractionCounts(userId) } returns emptyList()
        coEvery { userInteractionDao.getContentTypePreferences(userId) } returns emptyList()
        coEvery { geminiService.getContentRecommendations(any()) } returns Result.success(emptyList())
        coEvery { medlinePlusService.getTopicsForCondition(any()) } returns Result.success(emptyList())

        // When
        val result = repository.getPersonalizedContent(userId)

        // Then
        assertTrue(result.isSuccess)
        coVerify { medlinePlusService.getTopicsForCondition(any()) }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // OBSERVATIONS NEEDING FOLLOW-UP TESTS
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun `getObservationsNeedingFollowUp returns observations with follow-up flag`() = runTest {
        // Given
        val userId = "user-123"
        val observations = listOf(
            ObservationHistoryEntity(
                id = "obs-1",
                userId = userId,
                timestamp = System.currentTimeMillis(),
                observationType = "MOOD_PATTERN",
                observation = "Your mood has been declining. Consider speaking with a healthcare provider.",
                dataPointsAnalyzed = 14,
                timeSpanDays = 14,
                suggestsFollowUp = true,
                wasActedUpon = null,
                userFeedback = null
            )
        )
        coEvery { userInteractionDao.getObservationsNeedingFollowUp(userId) } returns observations

        // When
        val result = repository.getObservationsNeedingFollowUp(userId)

        // Then
        assertEquals(1, result.size)
        assertTrue(result.all { it.suggestsFollowUp })
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // CONTENT VIEWING TESTS
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun `onContentViewed records short view as ARTICLE_VIEW`() = runTest {
        // Given
        val userId = "user-123"

        // When - short view (< 30 seconds)
        repository.onContentViewed(
            userId = userId,
            contentId = "article-1",
            contentType = "ARTICLE",
            contentTopic = "pregnancy",
            viewDurationMs = 15_000  // 15 seconds
        )

        // Then
        coVerify {
            userInteractionDao.insertInteraction(match {
                it.interactionType == "ARTICLE_VIEW"
            })
        }
    }

    @Test
    fun `onContentViewed records long view as ARTICLE_COMPLETE`() = runTest {
        // Given
        val userId = "user-123"

        // When - long view (> 30 seconds)
        repository.onContentViewed(
            userId = userId,
            contentId = "article-1",
            contentType = "ARTICLE",
            contentTopic = "nutrition",
            viewDurationMs = 60_000  // 60 seconds
        )

        // Then
        coVerify {
            userInteractionDao.insertInteraction(match {
                it.interactionType == "ARTICLE_COMPLETE"
            })
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // ENGAGED TOPICS TESTS
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun `getMostEngagedTopics returns top topics from completed articles`() = runTest {
        // Given
        val userId = "user-123"
        val interactions = listOf(
            createInteractionWithMetadata(userId, "ARTICLE_COMPLETE", """{"topic":"pregnancy"}"""),
            createInteractionWithMetadata(userId, "ARTICLE_COMPLETE", """{"topic":"pregnancy"}"""),
            createInteractionWithMetadata(userId, "ARTICLE_COMPLETE", """{"topic":"nutrition"}""")
        )
        coEvery { userInteractionDao.getInteractionsByType(userId, "ARTICLE_COMPLETE") } returns interactions

        // When
        val topics = repository.getMostEngagedTopics(userId)

        // Then
        assertEquals("pregnancy", topics.first())
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // HELPER FUNCTIONS
    // ═══════════════════════════════════════════════════════════════════════════

    private fun createMockMoodLogs(count: Int): List<MoodLog> {
        return (1..count).map { i ->
            MoodLog(
                id = UUID.randomUUID().toString(),
                userId = "user-123",
                timestamp = Instant.now().minusSeconds(i * 86400L),
                moodScore = (3..5).random(),
                sleepQuality = (2..5).random(),
                energyLevel = (2..5).random(),
                anxietyLevel = null,
                notes = null,
                voiceNoteUrl = null,
                detectedKeywords = emptyList()
            )
        }
    }

    private fun createInteractionWithMetadata(
        userId: String,
        type: String,
        metadata: String
    ): UserInteractionEntity {
        return UserInteractionEntity(
            id = java.util.UUID.randomUUID().toString(),
            userId = userId,
            timestamp = System.currentTimeMillis(),
            interactionType = type,
            targetId = null,
            targetType = null,
            metadata = metadata,
            sessionId = null,
            durationMs = null
        )
    }
}

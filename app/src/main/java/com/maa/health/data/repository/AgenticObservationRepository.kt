package com.maa.health.data.repository

import com.maa.health.data.local.dao.UserInteractionDao
import com.maa.health.data.local.entity.ContentFeedbackEntity
import com.maa.health.data.local.entity.ContentTypes
import com.maa.health.data.local.entity.InteractionTypes
import com.maa.health.data.local.entity.ObservationHistoryEntity
import com.maa.health.data.local.entity.UserInteractionEntity
import com.maa.health.data.remote.ai.GeminiObservationService
import com.maa.health.data.remote.ai.ContentRecommendation
import com.maa.health.data.remote.ai.CycleEntry
import com.maa.health.data.remote.ai.CyclePatternData
import com.maa.health.data.remote.ai.GrowthEntry
import com.maa.health.data.remote.ai.GrowthPatternData
import com.maa.health.data.remote.ai.MoodEntry
import com.maa.health.data.remote.ai.MoodPatternData
import com.maa.health.data.remote.ai.ObservationType
import com.maa.health.data.remote.ai.PatternObservation
import com.maa.health.data.remote.ai.PersonalizedObservation
import com.maa.health.data.remote.ai.ScreeningSummary
import com.maa.health.data.remote.ai.SymptomEntry
import com.maa.health.data.remote.ai.SymptomPatternData
import com.maa.health.data.remote.ai.UserFeedback
import com.maa.health.data.remote.ai.UserHealthSummary
import com.maa.health.data.remote.ai.UserObservationContext
import com.maa.health.data.remote.medical.HealthCondition
import com.maa.health.data.remote.medical.HealthTopic
import com.maa.health.data.remote.medical.MedlinePlusService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Agentic Observation Repository
 *
 * This is the brain of the personalization system. It:
 * 1. OBSERVES: Tracks user interactions and health data
 * 2. RECORDS: Stores interaction history for pattern learning
 * 3. ANALYZES: Uses Gemini to identify patterns (observations only, no diagnoses)
 * 4. LEARNS: Adapts recommendations based on user feedback
 * 5. PROVIDES: Personalized, relevant content from verified sources
 *
 * KEY PRINCIPLES:
 * - We observe patterns, we don't diagnose
 * - We recommend verified content (MedlinePlus), we don't generate medical advice
 * - We learn user preferences, not medical decisions
 * - We always suggest consulting healthcare providers for concerns
 */
@Singleton
class AgenticObservationRepository @Inject constructor(
    private val userInteractionDao: UserInteractionDao,
    private val geminiService: GeminiObservationService,
    private val medlinePlusService: MedlinePlusService,
    private val moodRepository: MoodRepository,
    private val symptomRepository: SymptomRepository,
    private val cycleRepository: CycleRepository,
    private val userRepository: UserRepository
) {
    /**
     * Record any user interaction
     * This is the primary input for the learning system
     */
    suspend fun recordInteraction(
        userId: String,
        type: String,
        targetId: String? = null,
        targetType: String? = null,
        metadata: Map<String, Any>? = null,
        durationMs: Long? = null
    ) {
        val interaction = UserInteractionEntity(
            id = UUID.randomUUID().toString(),
            userId = userId,
            timestamp = System.currentTimeMillis(),
            interactionType = type,
            targetId = targetId,
            targetType = targetType,
            metadata = metadata?.let { mapToJson(it) },
            sessionId = null,  // TODO: Implement session tracking
            durationMs = durationMs
        )
        userInteractionDao.insertInteraction(interaction)
    }

    /**
     * Record user feedback on content
     * This is how the system learns what's helpful
     */
    suspend fun recordFeedback(
        userId: String,
        contentId: String,
        contentType: String,
        contentTopic: String?,
        wasHelpful: Boolean,
        feedbackText: String? = null
    ) {
        val feedback = ContentFeedbackEntity(
            id = UUID.randomUUID().toString(),
            userId = userId,
            contentId = contentId,
            contentType = contentType,
            contentTopic = contentTopic,
            wasHelpful = wasHelpful,
            feedbackText = feedbackText,
            timestamp = System.currentTimeMillis()
        )
        userInteractionDao.insertFeedback(feedback)

        // Also record as interaction
        recordInteraction(
            userId = userId,
            type = InteractionTypes.FEEDBACK_GIVEN,
            targetId = contentId,
            targetType = contentType,
            metadata = mapOf("wasHelpful" to wasHelpful)
        )
    }

    /**
     * Get personalized observation based on user's health data and history
     * This is the main "agentic" output
     */
    suspend fun getPersonalizedObservation(
        userId: String
    ): Result<PersonalizedObservation> {
        // Gather user's health summary
        val healthSummary = buildHealthSummary(userId)

        // Get previous feedback to learn from
        val recentFeedback = userInteractionDao.getRecentFeedback(userId, 20)
        val feedbackList = recentFeedback.map {
            UserFeedback(
                topic = it.contentTopic ?: it.contentType,
                wasHelpful = it.wasHelpful,
                timestamp = it.timestamp
            )
        }

        // Get personalized observation from Gemini
        val result = geminiService.getPersonalizedObservation(healthSummary, feedbackList)

        // Store the observation for history
        result.getOrNull()?.let { observation ->
            storeObservation(
                userId = userId,
                type = ObservationType.GENERAL.name,
                observation = observation.observation,
                dataPoints = healthSummary.totalMoodEntries + healthSummary.totalSymptomEntries,
                suggestsFollowUp = observation.suggestedActions.any {
                    it.contains("healthcare provider", ignoreCase = true)
                }
            )
        }

        return result
    }

    /**
     * Analyze mood patterns and provide observations
     */
    suspend fun analyzeMoodPatterns(
        userId: String,
        daysPeriod: Int = 14
    ): Result<PatternObservation> {
        val user = userRepository.getUserProfile(userId) ?: return Result.failure(
            Exception("User not found")
        )

        val moodLogs = moodRepository.getMoodLogsForPeriod(userId, daysPeriod)

        if (moodLogs.size < 3) {
            return Result.success(
                PatternObservation(
                    type = ObservationType.MOOD_PATTERN,
                    observation = "Keep tracking your mood. With more entries, I'll be able to identify patterns and trends to share with you.",
                    dataPoints = moodLogs.size,
                    timeSpan = "$daysPeriod days",
                    suggestsFollowUp = false
                )
            )
        }

        val moodData = MoodPatternData(
            lifecycleStage = user.lifecycleStage.name,
            periodDays = daysPeriod,
            entries = moodLogs.map { log ->
                MoodEntry(
                    date = log.timestamp.atZone(ZoneId.systemDefault()).toLocalDate().toString(),
                    score = log.moodScore,
                    sleepQuality = log.sleepQuality ?: 3,
                    energyLevel = log.energyLevel ?: 3
                )
            }
        )

        val result = geminiService.analyzeMoodPatterns(moodData)

        // Store observation
        result.getOrNull()?.let { observation ->
            storeObservation(
                userId = userId,
                type = ObservationType.MOOD_PATTERN.name,
                observation = observation.observation,
                dataPoints = observation.dataPoints,
                suggestsFollowUp = observation.suggestsFollowUp
            )
        }

        // Record this analysis as an interaction
        recordInteraction(
            userId = userId,
            type = InteractionTypes.OBSERVATION_VIEW,
            targetType = ObservationType.MOOD_PATTERN.name,
            metadata = mapOf("dataPoints" to moodLogs.size)
        )

        return result
    }

    /**
     * Analyze symptom patterns
     */
    suspend fun analyzeSymptomPatterns(
        userId: String,
        daysPeriod: Int = 30
    ): Result<PatternObservation> {
        val user = userRepository.getUserProfile(userId) ?: return Result.failure(
            Exception("User not found")
        )

        val symptomLogs = symptomRepository.getSymptomLogsForPeriod(userId, daysPeriod)

        if (symptomLogs.size < 3) {
            return Result.success(
                PatternObservation(
                    type = ObservationType.SYMPTOM_PATTERN,
                    observation = "Continue logging symptoms when they occur. With more data, I can help identify patterns.",
                    dataPoints = symptomLogs.size,
                    timeSpan = "$daysPeriod days",
                    suggestsFollowUp = false
                )
            )
        }

        val symptomData = SymptomPatternData(
            lifecycleStage = user.lifecycleStage.name,
            isPregnant = user.isPregnant,
            periodDays = daysPeriod,
            symptoms = symptomLogs.map { log ->
                SymptomEntry(
                    date = log.timestamp.atZone(ZoneId.systemDefault()).toLocalDate().toString(),
                    type = log.symptomType.name,
                    severity = log.severity.name,
                    bodyRegion = log.bodyRegion.name
                )
            }
        )

        val result = geminiService.analyzeSymptomPatterns(symptomData)

        result.getOrNull()?.let { observation ->
            storeObservation(
                userId = userId,
                type = ObservationType.SYMPTOM_PATTERN.name,
                observation = observation.observation,
                dataPoints = observation.dataPoints,
                suggestsFollowUp = observation.suggestsFollowUp
            )
        }

        recordInteraction(
            userId = userId,
            type = InteractionTypes.OBSERVATION_VIEW,
            targetType = ObservationType.SYMPTOM_PATTERN.name
        )

        return result
    }

    /**
     * Get personalized content recommendations
     * Combines user context with verified medical content
     */
    suspend fun getPersonalizedContent(
        userId: String
    ): Result<List<PersonalizedContent>> {
        val user = userRepository.getUserProfile(userId) ?: return Result.failure(
            Exception("User not found")
        )

        // Get recent interactions to understand interests
        val recentInteractions = userInteractionDao.getRecentInteractions(userId, 50)
        val interactionCounts = userInteractionDao.getInteractionCounts(userId)

        // Get content preferences from feedback
        val contentPrefs = userInteractionDao.getContentTypePreferences(userId)

        // Build context for Gemini
        val context = UserObservationContext(
            lifecycleStage = user.lifecycleStage.name,
            age = user.age,
            recentConcerns = extractRecentConcerns(recentInteractions),
            trackedConditions = extractTrackedConditions(user),
            language = user.preferredLanguage
        )

        // Get AI recommendations for topics
        val aiRecommendations = geminiService.getContentRecommendations(context)
            .getOrNull() ?: emptyList()

        // Fetch actual content from MedlinePlus for each recommendation
        val personalizedContent = mutableListOf<PersonalizedContent>()

        for (recommendation in aiRecommendations.take(5)) {
            val topics = medlinePlusService.searchHealthTopics(
                query = recommendation.topic,
                language = if (user.preferredLanguage == "es") "es" else "en"
            ).getOrNull() ?: emptyList()

            topics.firstOrNull()?.let { topic ->
                personalizedContent.add(
                    PersonalizedContent(
                        id = UUID.randomUUID().toString(),
                        title = topic.title,
                        summary = topic.summary,
                        url = topic.url,
                        source = topic.source,
                        relevanceReason = recommendation.reason,
                        priority = recommendation.priority.name,
                        contentType = ContentTypes.ARTICLE
                    )
                )
            }
        }

        // Also add lifecycle-specific content
        val lifecycleCondition = when (user.lifecycleStage.name) {
            "PREGNANCY" -> HealthCondition.PREGNANCY
            "POSTPARTUM" -> HealthCondition.POSTPARTUM
            "REPRODUCTIVE" -> HealthCondition.MENSTRUAL_HEALTH
            else -> HealthCondition.NUTRITION
        }

        medlinePlusService.getTopicsForCondition(lifecycleCondition)
            .getOrNull()
            ?.take(3)
            ?.forEach { topic ->
                personalizedContent.add(
                    PersonalizedContent(
                        id = UUID.randomUUID().toString(),
                        title = topic.title,
                        summary = topic.summary,
                        url = topic.url,
                        source = topic.source,
                        relevanceReason = "Relevant to your ${user.lifecycleStage.name.lowercase()} stage",
                        priority = "MEDIUM",
                        contentType = ContentTypes.ARTICLE
                    )
                )
            }

        return Result.success(personalizedContent.distinctBy { it.title })
    }

    /**
     * Get observations that suggest follow-up with healthcare provider
     */
    suspend fun getObservationsNeedingFollowUp(
        userId: String
    ): List<ObservationHistoryEntity> {
        return userInteractionDao.getObservationsNeedingFollowUp(userId)
    }

    /**
     * Stream real-time observation for UI
     */
    fun streamMoodObservation(
        userId: String,
        daysPeriod: Int = 14
    ): Flow<String> = flow {
        val user = userRepository.getUserProfile(userId) ?: return@flow
        val moodLogs = moodRepository.getMoodLogsForPeriod(userId, daysPeriod)

        if (moodLogs.size < 3) {
            emit("Keep tracking your mood. With more entries, I'll be able to share observations.")
            return@flow
        }

        val moodData = MoodPatternData(
            lifecycleStage = user.lifecycleStage.name,
            periodDays = daysPeriod,
            entries = moodLogs.map { log ->
                MoodEntry(
                    date = log.timestamp.atZone(ZoneId.systemDefault()).toLocalDate().toString(),
                    score = log.moodScore,
                    sleepQuality = log.sleepQuality ?: 3,
                    energyLevel = log.energyLevel ?: 3
                )
            }
        )

        geminiService.streamObservation(moodData, ObservationType.MOOD_PATTERN)
            .collect { emit(it) }
    }

    /**
     * Learn from user's viewing behavior
     * Called when user views content
     */
    suspend fun onContentViewed(
        userId: String,
        contentId: String,
        contentType: String,
        contentTopic: String,
        viewDurationMs: Long
    ) {
        val interactionType = if (viewDurationMs > 30_000) {  // > 30 seconds
            InteractionTypes.ARTICLE_COMPLETE
        } else {
            InteractionTypes.ARTICLE_VIEW
        }

        recordInteraction(
            userId = userId,
            type = interactionType,
            targetId = contentId,
            targetType = contentType,
            metadata = mapOf(
                "topic" to contentTopic,
                "durationMs" to viewDurationMs
            ),
            durationMs = viewDurationMs
        )
    }

    /**
     * Get user's most engaged topics
     * Used for content personalization
     */
    suspend fun getMostEngagedTopics(userId: String): List<String> {
        val interactions = userInteractionDao.getInteractionsByType(userId, InteractionTypes.ARTICLE_COMPLETE)
        val topicCounts = mutableMapOf<String, Int>()

        interactions.forEach { interaction ->
            interaction.metadata?.let { json ->
                extractFromJson(json, "topic")?.let { topic ->
                    topicCounts[topic] = (topicCounts[topic] ?: 0) + 1
                }
            }
        }

        return topicCounts.entries
            .sortedByDescending { it.value }
            .take(5)
            .map { it.key }
    }

    // Private helpers

    private suspend fun buildHealthSummary(userId: String): UserHealthSummary {
        val user = userRepository.getUserProfile(userId)
        val moodLogs = moodRepository.getMoodLogsForPeriod(userId, 30)
        val symptomLogs = symptomRepository.getSymptomLogsForPeriod(userId, 30)
        val recentScreenings = moodRepository.getRecentScreenings(userId, 3)
        val lastCycle = cycleRepository.getLastCycle(userId)

        // Calculate mood trend
        val moodTrend = if (moodLogs.size >= 7) {
            val recentAvg = moodLogs.take(7).map { it.moodScore }.average()
            val olderAvg = moodLogs.drop(7).take(7).map { it.moodScore }.average()
            when {
                recentAvg > olderAvg + 0.5 -> "improving"
                recentAvg < olderAvg - 0.5 -> "declining"
                else -> "stable"
            }
        } else "insufficient_data"

        return UserHealthSummary(
            lifecycleStage = user?.lifecycleStage?.name ?: "UNKNOWN",
            moodTrend = moodTrend,
            activeSymptoms = symptomLogs.take(10).map { it.symptomType.name }.distinct(),
            daysSinceLastPeriod = lastCycle?.let {
                ChronoUnit.DAYS.between(it.startDate, LocalDate.now()).toInt()
            },
            recentScreenings = recentScreenings.map {
                ScreeningSummary(
                    type = it.screeningType.name,
                    severity = it.severity.name
                )
            },
            totalMoodEntries = moodLogs.size,
            totalSymptomEntries = symptomLogs.size,
            cyclesTracked = cycleRepository.getCycleCount(userId)
        )
    }

    private suspend fun storeObservation(
        userId: String,
        type: String,
        observation: String,
        dataPoints: Int,
        suggestsFollowUp: Boolean
    ) {
        val entity = ObservationHistoryEntity(
            id = UUID.randomUUID().toString(),
            userId = userId,
            timestamp = System.currentTimeMillis(),
            observationType = type,
            observation = observation,
            dataPointsAnalyzed = dataPoints,
            timeSpanDays = 14,  // Default
            suggestsFollowUp = suggestsFollowUp,
            wasActedUpon = null,
            userFeedback = null
        )
        userInteractionDao.insertObservation(entity)
    }

    private fun extractRecentConcerns(interactions: List<UserInteractionEntity>): List<String> {
        val concerns = mutableSetOf<String>()

        interactions.forEach { interaction ->
            when (interaction.interactionType) {
                InteractionTypes.SYMPTOM_LOG -> {
                    interaction.metadata?.let { json ->
                        extractFromJson(json, "symptomType")?.let { concerns.add(it) }
                    }
                }
                InteractionTypes.SEARCH -> {
                    interaction.metadata?.let { json ->
                        extractFromJson(json, "query")?.let { concerns.add(it) }
                    }
                }
                InteractionTypes.SCREENING_COMPLETE -> {
                    interaction.targetType?.let { concerns.add(it) }
                }
            }
        }

        return concerns.toList()
    }

    private fun extractTrackedConditions(user: UserProfile?): List<String> {
        val conditions = mutableListOf<String>()
        user?.let {
            if (it.isPregnant) conditions.add("pregnancy")
            if (it.isPostpartum) conditions.add("postpartum")
            if (it.hasChildren) conditions.add("childcare")
            conditions.add(it.lifecycleStage.name.lowercase())
        }
        return conditions
    }

    private fun mapToJson(map: Map<String, Any>): String {
        return map.entries.joinToString(",", "{", "}") { (k, v) ->
            "\"$k\":${if (v is String) "\"$v\"" else v}"
        }
    }

    private fun extractFromJson(json: String, key: String): String? {
        val regex = "\"$key\":\\s*\"([^\"]+)\"".toRegex()
        return regex.find(json)?.groupValues?.get(1)
    }
}

/**
 * Personalized content item
 */
data class PersonalizedContent(
    val id: String,
    val title: String,
    val summary: String,
    val url: String,
    val source: String,
    val relevanceReason: String,
    val priority: String,
    val contentType: String
)

/**
 * User profile placeholder - should match your actual UserProfile
 */
data class UserProfile(
    val id: String,
    val lifecycleStage: LifecycleStage,
    val age: Int,
    val isPregnant: Boolean,
    val isPostpartum: Boolean,
    val hasChildren: Boolean,
    val preferredLanguage: String
)

enum class LifecycleStage {
    ADOLESCENCE,
    REPRODUCTIVE,
    PREGNANCY,
    POSTPARTUM,
    CHILD_CARE,
    MIDLIFE,
    ELDER
}

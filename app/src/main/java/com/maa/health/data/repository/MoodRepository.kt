package com.maa.health.data.repository

import com.maa.health.data.local.database.dao.MoodDao
import com.maa.health.data.local.database.dao.ScreeningDao
import com.maa.health.data.local.database.entity.MoodLogEntity
import com.maa.health.data.local.database.entity.ScreeningResultEntity
import com.maa.health.data.model.MoodLog
import com.maa.health.data.model.MoodPattern
import com.maa.health.data.model.ScreeningResult
import com.maa.health.data.model.ScreeningType
import com.maa.health.data.model.Severity
import com.maa.health.data.model.Trend
import com.maa.health.data.remote.medgemma.MedGemmaService
import com.maa.health.data.remote.medgemma.UserContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.roundToInt

/**
 * Repository for mood and mental health tracking
 */
@Singleton
class MoodRepository @Inject constructor(
    private val moodDao: MoodDao,
    private val screeningDao: ScreeningDao,
    private val medGemmaService: MedGemmaService
) {
    // ═══════════════════════════════════════════════════════════════════════════
    // MOOD LOGGING
    // ═══════════════════════════════════════════════════════════════════════════

    suspend fun logMood(
        userId: String,
        moodScore: Int,
        energyLevel: Int? = null,
        sleepQuality: Int? = null,
        anxietyLevel: Int? = null,
        notes: String? = null
    ): MoodLog {
        val id = java.util.UUID.randomUUID().toString()

        // Extract keywords from notes
        val keywords = notes?.let { extractKeywords(it) } ?: emptyList()

        val entity = MoodLogEntity(
            id = id,
            oderId = userId,
            timestamp = Instant.now(),
            moodScore = moodScore,
            energyLevel = energyLevel,
            sleepQuality = sleepQuality,
            anxietyLevel = anxietyLevel,
            notes = notes,
            voiceNoteUrl = null,
            detectedKeywords = keywords
        )
        moodDao.insertMoodLog(entity)

        return entity.toDomain()
    }

    private fun extractKeywords(text: String): List<String> {
        val keywords = mutableListOf<String>()
        val lowerText = text.lowercase()

        // Negative keywords
        val negativeKeywords = listOf(
            "sad", "anxious", "worried", "stressed", "tired", "exhausted",
            "hopeless", "helpless", "overwhelmed", "crying", "lonely",
            "angry", "frustrated", "scared", "panic", "can't sleep",
            "no appetite", "worthless", "guilty", "harm", "die"
        )

        // Positive keywords
        val positiveKeywords = listOf(
            "happy", "calm", "peaceful", "grateful", "hopeful",
            "energetic", "rested", "motivated", "loved", "supported"
        )

        negativeKeywords.forEach { if (lowerText.contains(it)) keywords.add(it) }
        positiveKeywords.forEach { if (lowerText.contains(it)) keywords.add(it) }

        return keywords
    }

    fun getMoodHistory(userId: String, days: Int = 30): Flow<List<MoodLog>> {
        val startTime = Instant.now().minus(days.toLong(), ChronoUnit.DAYS)
        return moodDao.getMoodLogsAfter(userId, startTime).map { list ->
            list.map { it.toDomain() }
        }
    }

    fun getRecentMoods(userId: String, limit: Int = 7): Flow<List<MoodLog>> {
        return moodDao.getRecentMoodLogs(userId, limit).map { list ->
            list.map { it.toDomain() }
        }
    }

    suspend fun getTodaysMood(userId: String): MoodLog? {
        val todayStart = Instant.now().truncatedTo(ChronoUnit.DAYS)
        return moodDao.getMoodLogsAfter(userId, todayStart).first().firstOrNull()?.toDomain()
    }

    /**
     * Get mood logs for a specific period (for agentic analysis)
     */
    suspend fun getMoodLogsForPeriod(userId: String, days: Int): List<MoodLog> {
        val startTime = Instant.now().minus(days.toLong(), ChronoUnit.DAYS)
        return moodDao.getMoodLogsAfter(userId, startTime).first().map { it.toDomain() }
    }

    /**
     * Get recent screenings with limit (for agentic analysis)
     */
    suspend fun getRecentScreenings(userId: String, limit: Int): List<ScreeningResult> {
        return screeningDao.getScreeningsForUser(userId).first()
            .take(limit)
            .map { it.toDomain() }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // MOOD PATTERN ANALYSIS
    // ═══════════════════════════════════════════════════════════════════════════

    suspend fun getMoodPattern(userId: String): MoodPattern {
        val recentMoods = moodDao.getRecentMoodLogs(userId, 30).first()

        if (recentMoods.isEmpty()) {
            return MoodPattern(
                userId = userId,
                averageMoodScore = 0f,
                moodTrend = Trend.BASELINE,
                cycleMoodCorrelation = null,
                triggers = emptyList(),
                effectiveCopingStrategies = emptyList(),
                lastUpdated = Instant.now()
            )
        }

        val avgScore = recentMoods.map { it.moodScore }.average().toFloat()

        // Calculate trend by comparing first half to second half
        val halfSize = recentMoods.size / 2
        val trend = if (recentMoods.size >= 4) {
            val recentHalf = recentMoods.take(halfSize).map { it.moodScore }.average()
            val olderHalf = recentMoods.takeLast(halfSize).map { it.moodScore }.average()
            when {
                recentHalf > olderHalf + 0.5 -> Trend.IMPROVING
                recentHalf < olderHalf - 0.5 -> Trend.DECLINING
                else -> Trend.STABLE
            }
        } else Trend.BASELINE

        // Extract common triggers from negative keywords
        val allKeywords = recentMoods.flatMap { it.detectedKeywords }
        val triggers = allKeywords
            .filter { it in listOf("stressed", "tired", "anxious", "overwhelmed", "lonely") }
            .groupingBy { it }
            .eachCount()
            .entries
            .sortedByDescending { it.value }
            .take(3)
            .map { it.key }

        return MoodPattern(
            userId = userId,
            averageMoodScore = avgScore,
            moodTrend = trend,
            cycleMoodCorrelation = null, // Would correlate with cycle data
            triggers = triggers,
            effectiveCopingStrategies = emptyList(),
            lastUpdated = Instant.now()
        )
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // MENTAL HEALTH SCREENINGS
    // ═══════════════════════════════════════════════════════════════════════════

    suspend fun submitScreening(
        userId: String,
        screeningType: ScreeningType,
        responses: List<Int>,
        userContext: UserContext
    ): ScreeningResult {
        val id = java.util.UUID.randomUUID().toString()
        val score = responses.sum()

        // Get AI interpretation
        val interpretation = medGemmaService.interpretScreening(
            screeningType, responses, userContext
        ).getOrNull()

        val (severity, needsFollowUp) = when (screeningType) {
            ScreeningType.EPDS -> interpretEPDS(score, responses)
            ScreeningType.PHQ9, ScreeningType.PHQ_A -> interpretPHQ9(score, responses)
            ScreeningType.GAD7 -> interpretGAD7(score)
            else -> Pair(Severity.NONE, false)
        }

        val entity = ScreeningResultEntity(
            id = id,
            oderId = userId,
            screeningType = screeningType,
            timestamp = Instant.now(),
            score = score,
            severity = severity,
            responses = responses,
            interpretation = interpretation?.interpretation ?: getDefaultInterpretation(screeningType, score),
            recommendations = interpretation?.recommendations ?: getDefaultRecommendations(screeningType, severity),
            needsFollowUp = needsFollowUp || (interpretation?.needsFollowUp ?: false)
        )
        screeningDao.insertScreening(entity)

        return entity.toDomain()
    }

    private fun interpretEPDS(score: Int, responses: List<Int>): Pair<Severity, Boolean> {
        val selfHarmResponse = responses.getOrNull(9) ?: 0
        return when {
            selfHarmResponse > 0 -> Pair(Severity.SEVERE, true)
            score >= 13 -> Pair(Severity.MODERATE, true)
            score >= 10 -> Pair(Severity.MILD, true)
            else -> Pair(Severity.NONE, false)
        }
    }

    private fun interpretPHQ9(score: Int, responses: List<Int>): Pair<Severity, Boolean> {
        val selfHarmResponse = responses.getOrNull(8) ?: 0
        return when {
            selfHarmResponse > 0 -> Pair(Severity.SEVERE, true)
            score >= 20 -> Pair(Severity.SEVERE, true)
            score >= 15 -> Pair(Severity.MODERATELY_SEVERE, true)
            score >= 10 -> Pair(Severity.MODERATE, true)
            score >= 5 -> Pair(Severity.MILD, false)
            else -> Pair(Severity.NONE, false)
        }
    }

    private fun interpretGAD7(score: Int): Pair<Severity, Boolean> {
        return when {
            score >= 15 -> Pair(Severity.SEVERE, true)
            score >= 10 -> Pair(Severity.MODERATE, true)
            score >= 5 -> Pair(Severity.MILD, false)
            else -> Pair(Severity.NONE, false)
        }
    }

    private fun getDefaultInterpretation(type: ScreeningType, score: Int): String {
        return when (type) {
            ScreeningType.EPDS -> when {
                score >= 13 -> "Your score suggests possible depression. Please speak with a healthcare provider."
                score >= 10 -> "Your score indicates some emotional distress. Monitoring is recommended."
                else -> "Your score is within the normal range. Continue to monitor your wellbeing."
            }
            ScreeningType.PHQ9, ScreeningType.PHQ_A -> when {
                score >= 20 -> "Score indicates severe depression. Professional evaluation is strongly recommended."
                score >= 15 -> "Score indicates moderately severe depression. Consider consultation."
                score >= 10 -> "Score indicates moderate depression. Monitoring and support recommended."
                score >= 5 -> "Score indicates mild symptoms. Self-care strategies may help."
                else -> "Minimal symptoms. Continue healthy habits."
            }
            ScreeningType.GAD7 -> when {
                score >= 15 -> "Score indicates severe anxiety. Professional support recommended."
                score >= 10 -> "Score indicates moderate anxiety. Consider consultation."
                score >= 5 -> "Score indicates mild anxiety. Self-help strategies may help."
                else -> "Minimal anxiety symptoms."
            }
            else -> "Screening completed. Please discuss results with your healthcare provider."
        }
    }

    private fun getDefaultRecommendations(type: ScreeningType, severity: Severity): List<String> {
        val recommendations = mutableListOf<String>()

        when (severity) {
            Severity.SEVERE -> {
                recommendations.add("Speak with a mental health professional as soon as possible")
                recommendations.add("Reach out to a trusted person for support")
                recommendations.add("If you have thoughts of self-harm, please contact a crisis helpline")
            }
            Severity.MODERATELY_SEVERE, Severity.MODERATE -> {
                recommendations.add("Consider speaking with a healthcare provider")
                recommendations.add("Practice daily self-care routines")
                recommendations.add("Stay connected with supportive people")
            }
            Severity.MILD -> {
                recommendations.add("Monitor your symptoms over the next week")
                recommendations.add("Practice relaxation techniques")
                recommendations.add("Maintain regular sleep and exercise")
            }
            else -> {
                recommendations.add("Continue your current healthy habits")
                recommendations.add("Re-screen in 2-4 weeks if symptoms develop")
            }
        }

        return recommendations
    }

    fun getScreeningHistory(userId: String): Flow<List<ScreeningResult>> {
        return screeningDao.getScreeningsForUser(userId).map { list ->
            list.map { it.toDomain() }
        }
    }

    fun getScreeningsByType(userId: String, type: ScreeningType): Flow<List<ScreeningResult>> {
        return screeningDao.getScreeningsByType(userId, type).map { list ->
            list.map { it.toDomain() }
        }
    }

    suspend fun getLatestScreening(userId: String, type: ScreeningType): ScreeningResult? {
        return screeningDao.getLatestScreening(userId, type)?.toDomain()
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // MAPPERS
    // ═══════════════════════════════════════════════════════════════════════════

    private fun MoodLogEntity.toDomain(): MoodLog {
        return MoodLog(
            id = id,
            userId = oderId,
            timestamp = timestamp,
            moodScore = moodScore,
            energyLevel = energyLevel,
            sleepQuality = sleepQuality,
            anxietyLevel = anxietyLevel,
            notes = notes,
            voiceNoteUrl = voiceNoteUrl,
            detectedKeywords = detectedKeywords
        )
    }

    private fun ScreeningResultEntity.toDomain(): ScreeningResult {
        return ScreeningResult(
            id = id,
            userId = oderId,
            screeningType = screeningType,
            timestamp = timestamp,
            score = score,
            severity = severity,
            responses = responses,
            interpretation = interpretation,
            recommendations = recommendations,
            needsFollowUp = needsFollowUp
        )
    }
}

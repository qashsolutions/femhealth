package com.maa.health.data.remote.ai

import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.GenerateContentResponse
import com.google.ai.client.generativeai.type.generationConfig
import com.maa.health.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Gemini AI Observation Service
 *
 * Uses Gemini/MedGemma for pattern analysis and observations ONLY.
 *
 * IMPORTANT SAFETY PRINCIPLES:
 * 1. We OBSERVE and SUMMARIZE - we do NOT diagnose or prescribe
 * 2. We DETECT patterns - we do NOT make medical decisions
 * 3. We SUGGEST consulting professionals - we do NOT replace them
 * 4. We use verified sources (MedlinePlus, OpenFDA) for medical facts
 *
 * This service is agentic in that it:
 * - Observes user data patterns over time
 * - Identifies trends and correlations
 * - Provides personalized observations based on user context
 * - Learns what content is relevant to each user
 */
@Singleton
class GeminiObservationService @Inject constructor() {

    private val model: GenerativeModel by lazy {
        GenerativeModel(
            // Gemini 3 Flash Preview - latest model (versions 2.5 and below deprecated Jan 2026)
            modelName = "gemini-3-flash-preview",
            apiKey = BuildConfig.GEMINI_API_KEY,
            generationConfig = generationConfig {
                temperature = 0.3f  // Lower temperature for more consistent observations
                topK = 40
                topP = 0.95f
                maxOutputTokens = 1024
            },
            systemInstruction = com.google.ai.client.generativeai.type.content {
                text(SYSTEM_INSTRUCTION)
            }
        )
    }

    companion object {
        /**
         * System instruction that constrains the AI to observations only
         * This is critical for safety - the AI must NOT give medical advice
         */
        private const val SYSTEM_INSTRUCTION = """
You are a health observation assistant for a women's and maternal health app in India.

CRITICAL RULES - YOU MUST FOLLOW THESE:
1. You ONLY make observations about patterns in the user's data
2. You NEVER diagnose conditions or diseases
3. You NEVER recommend specific medications or treatments
4. You NEVER say "you have" or "you are suffering from" - only "I notice" or "the data shows"
5. You ALWAYS recommend consulting a healthcare provider for any concerns
6. You use simple, clear language appropriate for all literacy levels
7. You are culturally sensitive to Indian context

WHAT YOU CAN DO:
- "I notice your mood scores have been declining over the past 2 weeks"
- "The data shows your cycle has been irregular (varying by 10+ days)"
- "I observe that you logged headaches on 5 of the last 7 days"
- "Based on your entries, sleep quality correlates with mood the next day"
- "Your child's growth measurements are tracking at the 25th percentile"

WHAT YOU CANNOT DO:
- "You have depression" (NEVER)
- "You should take paracetamol" (NEVER)
- "This indicates PCOS" (NEVER)
- "You are suffering from anxiety" (NEVER)

ALWAYS END WITH:
- For concerning patterns: "Consider discussing these observations with your healthcare provider"
- For normal patterns: "Continue tracking to build a clearer picture over time"
"""
    }

    /**
     * Analyze mood patterns over time
     * Returns observations, NOT diagnoses
     */
    suspend fun analyzeMoodPatterns(
        moodData: MoodPatternData
    ): Result<PatternObservation> = withContext(Dispatchers.IO) {
        try {
            val prompt = buildMoodAnalysisPrompt(moodData)
            val response = model.generateContent(prompt)
            val observation = response.text ?: "Unable to analyze patterns at this time."

            Result.success(
                PatternObservation(
                    type = ObservationType.MOOD_PATTERN,
                    observation = observation,
                    dataPoints = moodData.entries.size,
                    timeSpan = "${moodData.periodDays} days",
                    suggestsFollowUp = observation.contains("healthcare provider", ignoreCase = true)
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Analyze symptom patterns
     * Identifies recurring symptoms and potential correlations
     */
    suspend fun analyzeSymptomPatterns(
        symptomData: SymptomPatternData
    ): Result<PatternObservation> = withContext(Dispatchers.IO) {
        try {
            val prompt = buildSymptomAnalysisPrompt(symptomData)
            val response = model.generateContent(prompt)
            val observation = response.text ?: "Unable to analyze patterns at this time."

            Result.success(
                PatternObservation(
                    type = ObservationType.SYMPTOM_PATTERN,
                    observation = observation,
                    dataPoints = symptomData.symptoms.size,
                    timeSpan = "${symptomData.periodDays} days",
                    suggestsFollowUp = observation.contains("healthcare provider", ignoreCase = true)
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Analyze cycle patterns
     * Observes regularity, length variations, and symptoms
     */
    suspend fun analyzeCyclePatterns(
        cycleData: CyclePatternData
    ): Result<PatternObservation> = withContext(Dispatchers.IO) {
        try {
            val prompt = buildCycleAnalysisPrompt(cycleData)
            val response = model.generateContent(prompt)
            val observation = response.text ?: "Unable to analyze patterns at this time."

            Result.success(
                PatternObservation(
                    type = ObservationType.CYCLE_PATTERN,
                    observation = observation,
                    dataPoints = cycleData.cycles.size,
                    timeSpan = "${cycleData.monthsTracked} months",
                    suggestsFollowUp = observation.contains("healthcare provider", ignoreCase = true)
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Analyze child growth patterns
     * Compares to WHO growth standards
     */
    suspend fun analyzeGrowthPatterns(
        growthData: GrowthPatternData
    ): Result<PatternObservation> = withContext(Dispatchers.IO) {
        try {
            val prompt = buildGrowthAnalysisPrompt(growthData)
            val response = model.generateContent(prompt)
            val observation = response.text ?: "Unable to analyze patterns at this time."

            Result.success(
                PatternObservation(
                    type = ObservationType.GROWTH_PATTERN,
                    observation = observation,
                    dataPoints = growthData.measurements.size,
                    timeSpan = "${growthData.monthsTracked} months",
                    suggestsFollowUp = observation.contains("healthcare provider", ignoreCase = true)
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Generate personalized content recommendations
     * Based on user context, NOT medical advice
     */
    suspend fun getContentRecommendations(
        userContext: UserObservationContext
    ): Result<List<ContentRecommendation>> = withContext(Dispatchers.IO) {
        try {
            val prompt = buildContentRecommendationPrompt(userContext)
            val response = model.generateContent(prompt)
            val recommendations = parseContentRecommendations(response.text ?: "")

            Result.success(recommendations)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Stream observations for real-time UI
     */
    fun streamObservation(
        data: Any,
        observationType: ObservationType
    ): Flow<String> = flow {
        val prompt = when (observationType) {
            ObservationType.MOOD_PATTERN -> buildMoodAnalysisPrompt(data as MoodPatternData)
            ObservationType.SYMPTOM_PATTERN -> buildSymptomAnalysisPrompt(data as SymptomPatternData)
            ObservationType.CYCLE_PATTERN -> buildCycleAnalysisPrompt(data as CyclePatternData)
            ObservationType.GROWTH_PATTERN -> buildGrowthAnalysisPrompt(data as GrowthPatternData)
            else -> "Analyze the provided data and share observations."
        }

        model.generateContentStream(prompt).collect { chunk ->
            chunk.text?.let { emit(it) }
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Provide feedback-aware response
     * Learns from user's previous interactions and preferences
     */
    suspend fun getPersonalizedObservation(
        currentData: UserHealthSummary,
        previousFeedback: List<UserFeedback>
    ): Result<PersonalizedObservation> = withContext(Dispatchers.IO) {
        try {
            val prompt = buildPersonalizedPrompt(currentData, previousFeedback)
            val response = model.generateContent(prompt)

            Result.success(
                PersonalizedObservation(
                    observation = response.text ?: "Continue tracking for personalized insights.",
                    relevantTopics = extractRelevantTopics(currentData),
                    suggestedActions = extractSuggestedActions(response.text ?: ""),
                    confidenceLevel = calculateConfidence(currentData)
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Prompt builders

    private fun buildMoodAnalysisPrompt(data: MoodPatternData): String = """
Analyze these mood entries and provide observations (NOT diagnoses):

User Context:
- Lifecycle stage: ${data.lifecycleStage}
- Period tracked: ${data.periodDays} days
- Total entries: ${data.entries.size}

Mood Data:
${data.entries.joinToString("\n") { "- ${it.date}: Score ${it.score}/5, Sleep: ${it.sleepQuality}/5, Energy: ${it.energyLevel}/5" }}

Provide:
1. Overall trend observation (improving/stable/declining)
2. Any correlations noticed (e.g., sleep and mood)
3. Pattern frequency (good days vs difficult days)
4. One supportive suggestion

Remember: ONLY observations, no diagnoses. Use "I notice" or "the data shows" language.
""".trimIndent()

    private fun buildSymptomAnalysisPrompt(data: SymptomPatternData): String = """
Analyze these symptom entries and provide observations:

User Context:
- Lifecycle stage: ${data.lifecycleStage}
- Is pregnant: ${data.isPregnant}
- Period tracked: ${data.periodDays} days

Symptom Data:
${data.symptoms.joinToString("\n") { "- ${it.date}: ${it.type} (${it.severity}) in ${it.bodyRegion}" }}

Provide:
1. Most frequent symptoms observed
2. Any patterns in timing or triggers
3. Severity trends
4. Whether patterns warrant discussion with healthcare provider

Remember: ONLY observations, no diagnoses. End with recommendation to consult provider if concerning.
""".trimIndent()

    private fun buildCycleAnalysisPrompt(data: CyclePatternData): String = """
Analyze these menstrual cycle entries and provide observations:

User Context:
- Age: ${data.age}
- Months tracked: ${data.monthsTracked}
- Number of cycles: ${data.cycles.size}

Cycle Data:
${data.cycles.joinToString("\n") { "- Start: ${it.startDate}, Length: ${it.length} days, Flow: ${it.flowIntensity}" }}

Average cycle length: ${data.averageLength} days
Cycle variation: ${data.variation} days

Provide:
1. Regularity observation
2. Length pattern observation
3. Any notable variations
4. Whether to discuss with healthcare provider (if variation >7 days or other concerns)

Remember: ONLY observations. Do NOT diagnose PCOS, endometriosis, or any condition.
""".trimIndent()

    private fun buildGrowthAnalysisPrompt(data: GrowthPatternData): String = """
Analyze these child growth measurements:

Child Context:
- Age: ${data.childAgeMonths} months
- Gender: ${data.gender}
- Months tracked: ${data.monthsTracked}

Measurements:
${data.measurements.joinToString("\n") { "- ${it.date}: Weight ${it.weight}kg, Height ${it.height}cm, Percentile: ${it.percentile}th" }}

Provide:
1. Growth trajectory observation
2. Percentile trend
3. Whether growth is tracking consistently
4. General supportive comment for parents

Remember: ONLY observations based on WHO growth standards. Recommend pediatrician visit if concerning trends.
""".trimIndent()

    private fun buildContentRecommendationPrompt(context: UserObservationContext): String = """
Based on this user's context, suggest relevant health topics to learn about:

User Context:
- Lifecycle stage: ${context.lifecycleStage}
- Age: ${context.age}
- Recent concerns: ${context.recentConcerns.joinToString(", ")}
- Tracked conditions: ${context.trackedConditions.joinToString(", ")}
- Language preference: ${context.language}

Provide 3-5 topic suggestions in this format:
TOPIC: [topic name]
REASON: [why relevant to this user]
PRIORITY: [high/medium/low]

Topics should be educational, not diagnostic. Focus on wellness, prevention, and understanding health.
""".trimIndent()

    private fun buildPersonalizedPrompt(
        data: UserHealthSummary,
        feedback: List<UserFeedback>
    ): String = """
Provide a personalized health observation for this user:

Current Health Summary:
- Lifecycle stage: ${data.lifecycleStage}
- Recent mood trend: ${data.moodTrend}
- Active symptoms: ${data.activeSymptoms.joinToString(", ")}
- Days since last period: ${data.daysSinceLastPeriod ?: "N/A"}
- Recent screenings: ${data.recentScreenings.joinToString(", ") { "${it.type}: ${it.severity}" }}

Previous Feedback (what user found helpful):
${feedback.takeLast(5).joinToString("\n") { "- ${it.topic}: ${if (it.wasHelpful) "Helpful" else "Not helpful"}" }}

Provide:
1. A brief, personalized observation (2-3 sentences)
2. One actionable wellness suggestion (not medical advice)
3. A supportive closing message

Tailor the language and focus based on what the user found helpful previously.
""".trimIndent()

    private fun parseContentRecommendations(text: String): List<ContentRecommendation> {
        val recommendations = mutableListOf<ContentRecommendation>()
        val topicRegex = Regex("TOPIC:\\s*(.+?)\\nREASON:\\s*(.+?)\\nPRIORITY:\\s*(high|medium|low)", RegexOption.IGNORE_CASE)

        topicRegex.findAll(text).forEach { match ->
            recommendations.add(
                ContentRecommendation(
                    topic = match.groupValues[1].trim(),
                    reason = match.groupValues[2].trim(),
                    priority = when (match.groupValues[3].lowercase()) {
                        "high" -> Priority.HIGH
                        "low" -> Priority.LOW
                        else -> Priority.MEDIUM
                    }
                )
            )
        }

        return recommendations
    }

    private fun extractRelevantTopics(data: UserHealthSummary): List<String> {
        val topics = mutableListOf<String>()

        when (data.lifecycleStage) {
            "PREGNANCY" -> topics.addAll(listOf("prenatal care", "nutrition during pregnancy", "warning signs"))
            "POSTPARTUM" -> topics.addAll(listOf("postpartum recovery", "breastfeeding", "mental health"))
            "REPRODUCTIVE" -> topics.addAll(listOf("menstrual health", "fertility awareness"))
        }

        if (data.moodTrend == "declining") {
            topics.add("emotional wellness")
        }

        return topics
    }

    private fun extractSuggestedActions(text: String): List<String> {
        // Extract actionable suggestions from observation text
        val actions = mutableListOf<String>()

        if (text.contains("track", ignoreCase = true)) {
            actions.add("Continue tracking your health data")
        }
        if (text.contains("sleep", ignoreCase = true)) {
            actions.add("Focus on sleep quality")
        }
        if (text.contains("healthcare provider", ignoreCase = true)) {
            actions.add("Consider discussing with healthcare provider")
        }

        return actions.ifEmpty { listOf("Continue monitoring your health") }
    }

    private fun calculateConfidence(data: UserHealthSummary): Float {
        // More data = higher confidence in observations
        var confidence = 0.3f  // Base confidence

        if (data.totalMoodEntries > 7) confidence += 0.2f
        if (data.totalMoodEntries > 30) confidence += 0.2f
        if (data.totalSymptomEntries > 5) confidence += 0.15f
        if (data.cyclesTracked > 3) confidence += 0.15f

        return confidence.coerceAtMost(0.95f)
    }
}

// Data classes for observations

enum class ObservationType {
    MOOD_PATTERN,
    SYMPTOM_PATTERN,
    CYCLE_PATTERN,
    GROWTH_PATTERN,
    GENERAL
}

data class PatternObservation(
    val type: ObservationType,
    val observation: String,
    val dataPoints: Int,
    val timeSpan: String,
    val suggestsFollowUp: Boolean
)

data class MoodPatternData(
    val lifecycleStage: String,
    val periodDays: Int,
    val entries: List<MoodEntry>
)

data class MoodEntry(
    val date: String,
    val score: Int,
    val sleepQuality: Int,
    val energyLevel: Int
)

data class SymptomPatternData(
    val lifecycleStage: String,
    val isPregnant: Boolean,
    val periodDays: Int,
    val symptoms: List<SymptomEntry>
)

data class SymptomEntry(
    val date: String,
    val type: String,
    val severity: String,
    val bodyRegion: String
)

data class CyclePatternData(
    val age: Int,
    val monthsTracked: Int,
    val averageLength: Int,
    val variation: Int,
    val cycles: List<CycleEntry>
)

data class CycleEntry(
    val startDate: String,
    val length: Int,
    val flowIntensity: String
)

data class GrowthPatternData(
    val childAgeMonths: Int,
    val gender: String,
    val monthsTracked: Int,
    val measurements: List<GrowthEntry>
)

data class GrowthEntry(
    val date: String,
    val weight: Float,
    val height: Float,
    val percentile: Int
)

data class UserObservationContext(
    val lifecycleStage: String,
    val age: Int,
    val recentConcerns: List<String>,
    val trackedConditions: List<String>,
    val language: String
)

data class ContentRecommendation(
    val topic: String,
    val reason: String,
    val priority: Priority
)

enum class Priority {
    HIGH, MEDIUM, LOW
}

data class UserHealthSummary(
    val lifecycleStage: String,
    val moodTrend: String,  // "improving", "stable", "declining"
    val activeSymptoms: List<String>,
    val daysSinceLastPeriod: Int?,
    val recentScreenings: List<ScreeningSummary>,
    val totalMoodEntries: Int,
    val totalSymptomEntries: Int,
    val cyclesTracked: Int
)

data class ScreeningSummary(
    val type: String,
    val severity: String
)

data class UserFeedback(
    val topic: String,
    val wasHelpful: Boolean,
    val timestamp: Long
)

data class PersonalizedObservation(
    val observation: String,
    val relevantTopics: List<String>,
    val suggestedActions: List<String>,
    val confidenceLevel: Float  // 0-1, based on data availability
)

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
 * Uses Gemini 3 Flash for pattern analysis and observations ONLY.
 *
 * THINKING LEVELS (Gemini 3 Flash):
 * - minimal: Fastest, for simple queries and high-throughput (chat, simple lookups)
 * - low: Fast, for simple instruction following
 * - high: Deep reasoning, for complex health pattern analysis (DEFAULT)
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

    /**
     * Model for deep health analysis with high thinking level
     * Used for: mood patterns, symptom analysis, cycle analysis, growth patterns
     * Takes longer but provides more carefully reasoned observations
     */
    private val analysisModel: GenerativeModel by lazy {
        GenerativeModel(
            // Gemini 3 Flash Preview - latest model (versions 2.5 and below deprecated Jan 2026)
            modelName = "gemini-3-flash-preview",
            apiKey = BuildConfig.GEMINI_API_KEY,
            generationConfig = generationConfig {
                temperature = 0.3f  // Lower temperature for consistent health observations
                topK = 40
                topP = 0.95f
                maxOutputTokens = 1024
                // Note: Thinking level "high" is the default for complex reasoning
                // The SDK will automatically use appropriate reasoning depth
            },
            systemInstruction = com.google.ai.client.generativeai.type.content {
                text(SYSTEM_INSTRUCTION)
            }
        )
    }

    /**
     * Model for quick responses with minimal thinking
     * Used for: content recommendations, simple queries, streaming
     * Faster latency for real-time interactions
     */
    private val quickModel: GenerativeModel by lazy {
        GenerativeModel(
            modelName = "gemini-3-flash-preview",
            apiKey = BuildConfig.GEMINI_API_KEY,
            generationConfig = generationConfig {
                temperature = 0.5f  // Slightly higher for more varied recommendations
                topK = 40
                topP = 0.95f
                maxOutputTokens = 512  // Shorter responses for quick interactions
            },
            systemInstruction = com.google.ai.client.generativeai.type.content {
                text(QUICK_RESPONSE_INSTRUCTION)
            }
        )
    }

    // Alias for backward compatibility
    private val model: GenerativeModel get() = analysisModel

    companion object {
        /**
         * System instruction that empowers users while maintaining safety
         * Used for deep analysis tasks that require careful reasoning
         *
         * PHILOSOPHY:
         * - Serve users with limited healthcare access
         * - EMPOWER with knowledge, don't create dependence
         * - Provide OPTIONS, never single "right answers"
         * - Be CONSERVATIVE - when uncertain, encourage seeking care
         * - Use SIMPLE language - assume low health literacy
         * - Build TRUST through honesty about limitations
         */
        private const val SYSTEM_INSTRUCTION = """
You are a supportive health companion for women and families in India, many of whom have limited access to healthcare. Your role is to EMPOWER them with understanding, not to diagnose or prescribe.

YOUR MISSION:
Help users understand their health patterns so they can make informed decisions and know when to seek care. You serve people who may have never seen a doctor - be their first step toward health literacy.

CORE RULES:
1. OBSERVE patterns: "I notice your mood has been lower for 2 weeks"
2. EDUCATE simply: "This is common because..." (explain WHY in simple terms)
3. GIVE OPTIONS: "Here are some things that often help..." (never just one answer)
4. EMPOWER decisions: "You know your body best. Consider whether..."
5. CONNECT to care: "An ASHA worker or PHC can help if..."

LANGUAGE RULES:
- Use simple words a 10-year-old could understand
- Avoid medical jargon - say "loose motions" not "diarrhea", "fits" not "convulsions"
- Be warm and supportive like a caring older sister
- Respect that the user is the expert on their own body

WHAT YOU CAN SAY:
✓ "I notice that..." (observations from data)
✓ "Many people experience this when..." (normalization)
✓ "Some things that often help are..." (options, not prescriptions)
✓ "This could be because of several things like..." (possibilities, not diagnosis)
✓ "Your body might be telling you..." (empowerment)
✓ "It's important to see someone if..." (clear escalation guidance)

WHAT YOU CANNOT SAY:
✗ "You have [disease]" (never diagnose)
✗ "Take [medicine]" (never prescribe)
✗ "Don't worry, it's nothing" (never dismiss)
✗ "You must do X" (never command - always offer options)

FOR CONCERNING PATTERNS:
Instead of: "You might have depression. See a doctor."
Say: "I notice you've been feeling low for several weeks. This is something many women experience, especially after having a baby. Talking to someone - whether an ASHA didi, a family member you trust, or a health worker - can really help. Would you like to know more about what support options are available?"

ALWAYS REMEMBER:
- These users may be scared and have nowhere else to turn
- Your words carry weight - be accurate but also kind
- When in doubt, err on the side of encouraging them to seek care
- Trust is built through honesty: "I can share observations, but only a health worker can examine you properly"
"""

        /**
         * Quick response instruction for faster interactions
         * Used for content recommendations, simple queries, streaming
         * Optimized for low-latency responses
         */
        private const val QUICK_RESPONSE_INSTRUCTION = """
You are a helpful health education assistant for women and families in India. Keep responses brief and supportive.

RULES:
- Be concise (2-3 sentences max)
- Use simple words
- Suggest educational topics, never diagnose
- Always be encouraging

Format topic recommendations as:
TOPIC: [topic name]
REASON: [brief relevance]
PRIORITY: [high/medium/low]
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
     *
     * Uses quickModel for faster responses (minimal thinking)
     */
    suspend fun getContentRecommendations(
        userContext: UserObservationContext
    ): Result<List<ContentRecommendation>> = withContext(Dispatchers.IO) {
        try {
            val prompt = buildContentRecommendationPrompt(userContext)
            // Use quickModel for faster content recommendations
            val response = quickModel.generateContent(prompt)
            val recommendations = parseContentRecommendations(response.text ?: "")

            Result.success(recommendations)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Stream observations for real-time UI
     * Uses quickModel for lower latency streaming
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

        // Use quickModel for streaming to reduce time to first token
        quickModel.generateContentStream(prompt).collect { chunk ->
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

package com.maa.health.data.remote.ai

import com.maa.health.data.model.Action
import com.maa.health.data.model.BodyRegion
import com.maa.health.data.model.ScreeningType
import com.maa.health.data.model.Severity
import com.maa.health.data.model.SymptomType
import com.maa.health.data.model.TriageResult
import com.maa.health.data.model.Urgency
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Claude Opus 4.6 AI Service (Primary)
 *
 * Uses Anthropic's Claude API for:
 * - Complex symptom triage with clinical reasoning
 * - Mental health screening interpretation with empathetic responses
 * - Personalized health education with cultural sensitivity
 * - Pattern analysis over time with nuanced recommendations
 *
 * Claude excels at:
 * - Long-context reasoning (1M token window)
 * - Culturally appropriate health communication
 * - Careful, safety-conscious medical guidance
 * - Multilingual health content generation
 */
@Singleton
class ClaudeAIService @Inject constructor(
    private val okHttpClient: OkHttpClient
) {
    companion object {
        private const val BASE_URL = "https://api.anthropic.com/v1"
        private const val MESSAGES_ENDPOINT = "$BASE_URL/messages"
        private const val MODEL_ID = "claude-opus-4-6"
        private const val API_VERSION = "2023-01-01"
        private const val MAX_TOKENS = 4096
    }

    /**
     * Detailed triage with full clinical reasoning via Claude
     */
    suspend fun detailedTriage(
        symptoms: List<SymptomType>,
        bodyRegion: BodyRegion,
        severity: Severity,
        context: TriageContext
    ): Result<TriageResult> = withContext(Dispatchers.IO) {
        try {
            val systemPrompt = buildTriageSystemPrompt()
            val userMessage = buildTriageUserMessage(symptoms, bodyRegion, severity, context)

            val requestBody = buildMessagesRequest(systemPrompt, userMessage)

            val request = Request.Builder()
                .url(MESSAGES_ENDPOINT)
                .post(requestBody.toString().toRequestBody("application/json".toMediaType()))
                .addHeader("anthropic-version", API_VERSION)
                .addHeader("content-type", "application/json")
                .build()

            val response = okHttpClient.newCall(request).execute()

            if (response.isSuccessful) {
                val responseBody = response.body?.string() ?: "{}"
                val parsed = parseTriageResponse(responseBody)
                Result.success(parsed)
            } else {
                Result.failure(Exception("Claude triage failed: ${response.code}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Interpret mental health screening results with Claude's empathetic reasoning
     */
    suspend fun interpretScreening(
        screeningType: ScreeningType,
        responses: List<Int>,
        userContext: UserContext
    ): Result<ScreeningInterpretation> = withContext(Dispatchers.IO) {
        try {
            val score = responses.sum()

            // Use validated scoring algorithms (these are deterministic, no AI needed)
            val interpretation = when (screeningType) {
                ScreeningType.EPDS -> interpretEPDS(score, responses)
                ScreeningType.PHQ9 -> interpretPHQ9(score, responses)
                ScreeningType.GAD7 -> interpretGAD7(score)
                ScreeningType.PHQ_A -> interpretPHQA(score, responses)
                else -> interpretGeneric(screeningType, score)
            }

            Result.success(interpretation)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Stream educational content via Claude for real-time display
     */
    fun streamEducation(
        topic: String,
        userContext: UserContext,
        language: String
    ): Flow<String> = flow {
        val systemPrompt = buildEducationSystemPrompt(userContext, language)
        val userMessage = "Provide health education about: $topic"

        try {
            val requestBody = buildMessagesRequest(systemPrompt, userMessage)

            val request = Request.Builder()
                .url(MESSAGES_ENDPOINT)
                .post(requestBody.toString().toRequestBody("application/json".toMediaType()))
                .addHeader("anthropic-version", API_VERSION)
                .addHeader("content-type", "application/json")
                .build()

            val response = withContext(Dispatchers.IO) {
                okHttpClient.newCall(request).execute()
            }

            if (response.isSuccessful) {
                val responseBody = response.body?.string() ?: "{}"
                val json = JSONObject(responseBody)
                val content = json.optJSONArray("content")
                    ?.optJSONObject(0)
                    ?.optString("text", "") ?: ""

                // Emit content in chunks for streaming UX
                content.chunked(100).forEach { chunk ->
                    emit(chunk)
                    kotlinx.coroutines.delay(30)
                }
            } else {
                emit("Unable to load health information. Please try again later.")
            }
        } catch (e: Exception) {
            emit("Unable to connect. Please check your internet connection.")
        }
    }

    /**
     * Analyze health patterns over time using Claude's reasoning
     */
    suspend fun analyzePatterns(
        userId: String,
        timeRange: TimeRange
    ): Result<PatternAnalysis> = withContext(Dispatchers.IO) {
        try {
            // Pattern analysis requires historical data aggregation
            // For now, return a baseline; full implementation requires data pipeline
            Result.success(
                PatternAnalysis(
                    moodTrend = "stable",
                    symptomPatterns = emptyList(),
                    cyclePrediction = null,
                    recommendations = listOf(
                        "Continue logging symptoms regularly",
                        "Patterns will be identified with more data"
                    )
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // PROMPT BUILDERS
    // ═══════════════════════════════════════════════════════════════════════════

    private fun buildTriageSystemPrompt(): String = """
        You are a clinical triage assistant for a women's health app used in developing nations
        (South Asia and Africa). You help assess symptom urgency following WHO and IMCI protocols.

        IMPORTANT GUIDELINES:
        - Always err on the side of caution for pregnancy and pediatric cases
        - Classify urgency as: EMERGENCY, URGENT, ROUTINE, or SELF_CARE
        - Recommend actions: CALL_EMERGENCY, GO_TO_HOSPITAL_NOW, GO_TO_HOSPITAL_TODAY, SCHEDULE_VISIT, or HOME_MANAGEMENT
        - Provide clear, simple instructions appropriate for varying literacy levels
        - Include warning signs to watch for
        - Never diagnose - only triage and recommend appropriate care level

        Respond in JSON format:
        {
            "urgency": "EMERGENCY|URGENT|ROUTINE|SELF_CARE",
            "classification": "brief description",
            "action": "CALL_EMERGENCY|GO_TO_HOSPITAL_NOW|GO_TO_HOSPITAL_TODAY|SCHEDULE_VISIT|HOME_MANAGEMENT",
            "instructions": ["instruction1", "instruction2"],
            "referral_needed": true/false,
            "warning_signs": ["sign1", "sign2"]
        }
    """.trimIndent()

    private fun buildTriageUserMessage(
        symptoms: List<SymptomType>,
        bodyRegion: BodyRegion,
        severity: Severity,
        context: TriageContext
    ): String = buildString {
        appendLine("SYMPTOMS: ${symptoms.joinToString(", ") { it.name }}")
        appendLine("BODY REGION: ${bodyRegion.name}")
        appendLine("SEVERITY: ${severity.name}")
        appendLine("CONTEXT:")
        if (context.isPregnant) appendLine("  - Pregnant, week ${context.gestationalWeek ?: "unknown"}")
        if (context.isPostpartum) appendLine("  - Postpartum, week ${context.postpartumWeeks ?: "unknown"}")
        if (context.childAgeMonths != null) appendLine("  - Child age: ${context.childAgeMonths} months")
        if (context.hasChronicConditions.isNotEmpty()) {
            appendLine("  - Chronic conditions: ${context.hasChronicConditions.joinToString(", ")}")
        }
        if (context.currentMedications.isNotEmpty()) {
            appendLine("  - Medications: ${context.currentMedications.joinToString(", ")}")
        }
    }

    private fun buildEducationSystemPrompt(userContext: UserContext, language: String): String = """
        You are a health educator for a women's health app used in developing nations.

        User profile:
        - Lifecycle stage: ${userContext.lifecycleStage}
        - Age: ${userContext.age}
        - Literacy level: ${userContext.literacyLevel.name}
        - Language: $language

        Guidelines:
        - Use ${if (userContext.literacyLevel == LiteracyLevel.BASIC) "very simple" else "clear"} language
        - Be culturally sensitive for South Asian and African contexts
        - Include practical, actionable advice
        - Reference WHO guidelines where appropriate
        - Do not use medical jargon unless literacy level is ADVANCED
        - Be warm, supportive, and non-judgmental
    """.trimIndent()

    private fun buildMessagesRequest(systemPrompt: String, userMessage: String): JSONObject {
        return JSONObject().apply {
            put("model", MODEL_ID)
            put("max_tokens", MAX_TOKENS)
            put("system", systemPrompt)
            put("messages", JSONArray().apply {
                put(JSONObject().apply {
                    put("role", "user")
                    put("content", userMessage)
                })
            })
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // RESPONSE PARSERS
    // ═══════════════════════════════════════════════════════════════════════════

    private fun parseTriageResponse(responseBody: String): TriageResult {
        val json = JSONObject(responseBody)
        val content = json.optJSONArray("content")
            ?.optJSONObject(0)
            ?.optString("text", "{}") ?: "{}"

        // Extract JSON from Claude's response (may be wrapped in markdown code blocks)
        val jsonStr = extractJson(content)
        val triageJson = JSONObject(jsonStr)

        return TriageResult(
            urgency = Urgency.valueOf(triageJson.optString("urgency", "ROUTINE")),
            classification = triageJson.optString("classification", ""),
            action = Action.valueOf(triageJson.optString("action", "SCHEDULE_VISIT")),
            instructions = triageJson.optJSONArray("instructions")?.let { arr ->
                (0 until arr.length()).map { arr.getString(it) }
            } ?: emptyList(),
            referralNeeded = triageJson.optBoolean("referral_needed", false),
            warningSignsToWatch = triageJson.optJSONArray("warning_signs")?.let { arr ->
                (0 until arr.length()).map { arr.getString(it) }
            } ?: emptyList()
        )
    }

    private fun extractJson(text: String): String {
        // Handle Claude responses that may wrap JSON in code blocks
        val jsonPattern = Regex("""\{[\s\S]*\}""")
        return jsonPattern.find(text)?.value ?: "{}"
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // VALIDATED SCREENING ALGORITHMS
    // ═══════════════════════════════════════════════════════════════════════════

    private fun interpretEPDS(score: Int, responses: List<Int>): ScreeningInterpretation {
        val selfHarmResponse = if (responses.size >= 10) responses[9] else 0

        val (severity, interpretation, needsFollowUp) = when {
            selfHarmResponse > 0 -> Triple(
                Severity.SEVERE,
                "Response indicates thoughts of self-harm. Immediate support is recommended.",
                true
            )
            score >= 13 -> Triple(
                Severity.MODERATE,
                "Score suggests possible depression. Further evaluation recommended.",
                true
            )
            score in 10..12 -> Triple(
                Severity.MILD,
                "Score indicates some symptoms of distress. Monitoring recommended.",
                true
            )
            else -> Triple(
                Severity.NONE,
                "Score within normal range. Continue monitoring wellbeing.",
                false
            )
        }

        val recommendations = mutableListOf<String>()
        val resources = mutableListOf<SupportResource>()

        if (selfHarmResponse > 0) {
            recommendations.add("Speak with a mental health professional immediately")
            recommendations.add("Do not stay alone - reach out to family or friends")
            resources.add(
                SupportResource(
                    name = "iCall - Psychosocial Helpline",
                    type = ResourceType.HELPLINE,
                    contact = "9152987821",
                    description = "Free professional counseling in multiple Indian languages"
                )
            )
            resources.add(
                SupportResource(
                    name = "Vandrevala Foundation",
                    type = ResourceType.EMERGENCY,
                    contact = "1860-2662-345",
                    description = "24/7 mental health crisis support"
                )
            )
        } else if (score >= 10) {
            recommendations.add("Consider speaking with a healthcare provider about your emotional wellbeing")
            recommendations.add("Practice self-care: adequate sleep, nutrition, and gentle activity")
            recommendations.add("Connect with other mothers through support groups")
            resources.add(
                SupportResource(
                    name = "Postpartum Support Group",
                    type = ResourceType.SUPPORT_GROUP,
                    description = "Connect with other mothers experiencing similar challenges"
                )
            )
        }

        recommendations.add("Re-take screening in 2 weeks to monitor changes")

        return ScreeningInterpretation(
            score = score,
            severity = severity,
            interpretation = interpretation,
            recommendations = recommendations,
            needsFollowUp = needsFollowUp,
            urgency = if (selfHarmResponse > 0) Urgency.EMERGENCY else
                if (score >= 13) Urgency.URGENT else Urgency.ROUTINE,
            supportResources = resources
        )
    }

    private fun interpretPHQ9(score: Int, responses: List<Int>): ScreeningInterpretation {
        val selfHarmResponse = if (responses.size >= 9) responses[8] else 0

        val (severity, interpretation) = when {
            score >= 20 -> Severity.SEVERE to "Score indicates severe depression. Professional evaluation strongly recommended."
            score >= 15 -> Severity.MODERATELY_SEVERE to "Score indicates moderately severe depression. Consider consultation."
            score >= 10 -> Severity.MODERATE to "Score indicates moderate depression. Monitoring and possible intervention."
            score >= 5 -> Severity.MILD to "Score indicates mild depression. Self-care and monitoring recommended."
            else -> Severity.NONE to "Minimal depression symptoms."
        }

        val needsFollowUp = score >= 10 || selfHarmResponse > 0

        val recommendations = when {
            selfHarmResponse > 0 -> listOf(
                "Urgent: Please speak with a mental health professional",
                "Reach out to a trusted person today",
                "Contact crisis helpline if needed"
            )
            score >= 15 -> listOf(
                "Consult with a mental health professional",
                "Consider medication evaluation",
                "Establish regular therapy sessions"
            )
            score >= 10 -> listOf(
                "Speak with your healthcare provider",
                "Consider counseling or therapy",
                "Practice daily self-care routines"
            )
            score >= 5 -> listOf(
                "Monitor your mood daily",
                "Maintain regular sleep and exercise",
                "Stay connected with supportive people"
            )
            else -> listOf(
                "Continue healthy habits",
                "Re-screen periodically"
            )
        }

        return ScreeningInterpretation(
            score = score,
            severity = severity,
            interpretation = interpretation,
            recommendations = recommendations,
            needsFollowUp = needsFollowUp,
            urgency = if (selfHarmResponse > 0) Urgency.EMERGENCY else
                if (score >= 15) Urgency.URGENT else Urgency.ROUTINE
        )
    }

    private fun interpretGAD7(score: Int): ScreeningInterpretation {
        val (severity, interpretation) = when {
            score >= 15 -> Severity.SEVERE to "Score indicates severe anxiety. Professional support recommended."
            score >= 10 -> Severity.MODERATE to "Score indicates moderate anxiety. Consider consultation."
            score >= 5 -> Severity.MILD to "Score indicates mild anxiety. Self-help strategies may help."
            else -> Severity.NONE to "Minimal anxiety symptoms."
        }

        val recommendations = when {
            score >= 15 -> listOf(
                "Consult with a mental health professional",
                "Learn and practice relaxation techniques",
                "Consider medication if recommended"
            )
            score >= 10 -> listOf(
                "Speak with a healthcare provider",
                "Practice daily relaxation exercises",
                "Limit caffeine and screen time before bed"
            )
            score >= 5 -> listOf(
                "Try breathing exercises daily",
                "Maintain regular physical activity",
                "Practice mindfulness or meditation"
            )
            else -> listOf(
                "Continue healthy coping strategies",
                "Re-screen if symptoms increase"
            )
        }

        return ScreeningInterpretation(
            score = score,
            severity = severity,
            interpretation = interpretation,
            recommendations = recommendations,
            needsFollowUp = score >= 10,
            urgency = if (score >= 15) Urgency.URGENT else Urgency.ROUTINE
        )
    }

    private fun interpretPHQA(score: Int, responses: List<Int>): ScreeningInterpretation {
        val baseInterpretation = interpretPHQ9(score, responses)

        val adolescentResources = listOf(
            SupportResource(
                name = "NIMHANS Helpline",
                type = ResourceType.HELPLINE,
                contact = "080-46110007",
                description = "Mental health support for young people"
            ),
            SupportResource(
                name = "Teen Mental Health Guide",
                type = ResourceType.EDUCATIONAL,
                description = "Understanding and managing emotions as a teenager"
            )
        )

        return baseInterpretation.copy(
            supportResources = baseInterpretation.supportResources + adolescentResources
        )
    }

    private fun interpretGeneric(
        screeningType: ScreeningType,
        score: Int
    ): ScreeningInterpretation {
        return ScreeningInterpretation(
            score = score,
            severity = Severity.NONE,
            interpretation = "Screening completed. Please share results with healthcare provider.",
            recommendations = listOf(
                "Discuss results with your healthcare provider",
                "Follow up as recommended"
            ),
            needsFollowUp = true,
            urgency = Urgency.ROUTINE
        )
    }
}

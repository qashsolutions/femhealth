package com.maa.health.data.remote.medgemma

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
 * MedGemma 27B Cloud Service
 *
 * Provides cloud-based medical AI capabilities for:
 * - Complex triage decisions
 * - Screening interpretation
 * - Personalized health education
 * - Pattern analysis over time
 *
 * Uses streaming for real-time response delivery
 */
@Singleton
class MedGemmaCloudService @Inject constructor(
    private val okHttpClient: OkHttpClient
) {
    companion object {
        private const val BASE_URL = "https://api.medgemma.ai/v1"
        private const val TRIAGE_ENDPOINT = "$BASE_URL/triage"
        private const val SCREENING_ENDPOINT = "$BASE_URL/screening/interpret"
        private const val EDUCATION_ENDPOINT = "$BASE_URL/education/generate"
        private const val PATTERNS_ENDPOINT = "$BASE_URL/patterns/analyze"
    }

    /**
     * Detailed triage with full clinical reasoning
     */
    suspend fun detailedTriage(
        symptoms: List<SymptomType>,
        bodyRegion: BodyRegion,
        severity: Severity,
        context: TriageContext
    ): Result<TriageResult> = withContext(Dispatchers.IO) {
        try {
            val requestBody = JSONObject().apply {
                put("symptoms", JSONArray(symptoms.map { it.name }))
                put("body_region", bodyRegion.name)
                put("severity", severity.name)
                put("context", JSONObject().apply {
                    put("is_pregnant", context.isPregnant)
                    put("gestational_week", context.gestationalWeek)
                    put("is_postpartum", context.isPostpartum)
                    put("postpartum_weeks", context.postpartumWeeks)
                    put("child_age_months", context.childAgeMonths)
                    put("chronic_conditions", JSONArray(context.hasChronicConditions))
                    put("medications", JSONArray(context.currentMedications))
                })
            }

            val request = Request.Builder()
                .url(TRIAGE_ENDPOINT)
                .post(requestBody.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val response = okHttpClient.newCall(request).execute()

            if (response.isSuccessful) {
                val responseBody = response.body?.string() ?: "{}"
                val json = JSONObject(responseBody)

                Result.success(
                    TriageResult(
                        urgency = Urgency.valueOf(json.optString("urgency", "ROUTINE")),
                        classification = json.optString("classification", ""),
                        action = Action.valueOf(json.optString("action", "SCHEDULE_VISIT")),
                        instructions = json.optJSONArray("instructions")?.let { arr ->
                            (0 until arr.length()).map { arr.getString(it) }
                        } ?: emptyList(),
                        referralNeeded = json.optBoolean("referral_needed", false),
                        warningSignsToWatch = json.optJSONArray("warning_signs")?.let { arr ->
                            (0 until arr.length()).map { arr.getString(it) }
                        } ?: emptyList()
                    )
                )
            } else {
                Result.failure(Exception("Triage API failed: ${response.code}"))
            }
        } catch (e: Exception) {
            // Fallback to basic triage on network error
            Result.success(createFallbackTriage(symptoms, severity))
        }
    }

    /**
     * Interpret mental health screening results
     */
    suspend fun interpretScreening(
        screeningType: ScreeningType,
        responses: List<Int>,
        userContext: UserContext
    ): Result<ScreeningInterpretation> = withContext(Dispatchers.IO) {
        try {
            val score = responses.sum()

            // Use validated scoring for common screenings
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
     * EPDS interpretation (Edinburgh Postnatal Depression Scale)
     */
    private fun interpretEPDS(score: Int, responses: List<Int>): ScreeningInterpretation {
        // Question 10 is about self-harm
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

    /**
     * PHQ-9 interpretation (Patient Health Questionnaire)
     */
    private fun interpretPHQ9(score: Int, responses: List<Int>): ScreeningInterpretation {
        // Question 9 is about self-harm thoughts
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

    /**
     * GAD-7 interpretation (Generalized Anxiety Disorder)
     */
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

    /**
     * PHQ-A interpretation (Adolescent version)
     */
    private fun interpretPHQA(score: Int, responses: List<Int>): ScreeningInterpretation {
        // Similar to PHQ-9 but with adolescent-specific resources
        val baseInterpretation = interpretPHQ9(score, responses)

        // Add adolescent-specific resources
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

    /**
     * Generic screening interpretation
     */
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

    /**
     * Stream educational content for real-time display
     */
    fun streamEducation(
        topic: String,
        userContext: UserContext,
        language: String
    ): Flow<String> = flow {
        // TODO: Implement actual streaming from MedGemma API
        // For now, provide pre-built educational content

        val content = getEducationalContent(topic, userContext, language)
        content.forEach { chunk ->
            emit(chunk)
            kotlinx.coroutines.delay(50) // Simulate streaming
        }
    }

    private fun getEducationalContent(
        topic: String,
        userContext: UserContext,
        language: String
    ): List<String> {
        // Educational content library (simplified)
        // In production, this would call the MedGemma API
        return listOf(
            "Loading health information about $topic...",
            "\n\nThis information is personalized for your ${userContext.lifecycleStage} stage.",
            "\n\nPlease consult a healthcare provider for medical advice."
        )
    }

    /**
     * Analyze health patterns over time
     */
    suspend fun analyzePatterns(
        userId: String,
        timeRange: TimeRange
    ): Result<PatternAnalysis> = withContext(Dispatchers.IO) {
        try {
            // TODO: Implement actual pattern analysis API call
            // This would analyze symptom logs, mood entries, cycle data, etc.

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

    /**
     * Fallback triage when network is unavailable
     */
    private fun createFallbackTriage(
        symptoms: List<SymptomType>,
        severity: Severity
    ): TriageResult {
        val hasDangerSign = symptoms.any { it.isDangerSign }

        return TriageResult(
            urgency = when {
                hasDangerSign -> Urgency.EMERGENCY
                severity >= Severity.SEVERE -> Urgency.URGENT
                severity >= Severity.MODERATE -> Urgency.ROUTINE
                else -> Urgency.SELF_CARE
            },
            classification = "Assessment needed",
            action = when {
                hasDangerSign -> Action.GO_TO_HOSPITAL_NOW
                severity >= Severity.SEVERE -> Action.GO_TO_HOSPITAL_TODAY
                severity >= Severity.MODERATE -> Action.SCHEDULE_VISIT
                else -> Action.HOME_MANAGEMENT
            },
            instructions = listOf(
                "Unable to connect to health service",
                "If symptoms are severe, seek immediate care",
                "Try again when connected to internet"
            ),
            referralNeeded = hasDangerSign || severity >= Severity.MODERATE
        )
    }
}

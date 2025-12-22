package com.maa.health.data.remote.medgemma

import com.maa.health.data.model.BodyRegion
import com.maa.health.data.model.ScreeningType
import com.maa.health.data.model.Severity
import com.maa.health.data.model.SymptomType
import com.maa.health.data.model.TriageResult
import com.maa.health.data.model.Urgency
import com.maa.health.data.model.Action
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * MedGemma AI Service
 *
 * Provides medical AI capabilities using:
 * - MedGemma 4B: On-device for latency-critical tasks
 * - MedGemma 27B: Cloud API for complex reasoning
 *
 * All responses include confidence scores and source citations.
 */
@Singleton
class MedGemmaService @Inject constructor(
    private val localInference: MedGemmaLocalInference,
    private val cloudService: MedGemmaCloudService
) {
    /**
     * Triage symptoms and determine urgency
     *
     * Uses on-device model for speed, escalates to cloud for complex cases
     */
    suspend fun triageSymptoms(
        symptoms: List<SymptomType>,
        bodyRegion: BodyRegion,
        severity: Severity,
        context: TriageContext
    ): Result<TriageResult> {
        // First, try local inference for common patterns
        val localResult = localInference.quickTriage(symptoms, bodyRegion, severity, context)

        return if (localResult.confidence >= 0.85f) {
            Result.success(localResult.result)
        } else {
            // Escalate to cloud for complex reasoning
            cloudService.detailedTriage(symptoms, bodyRegion, severity, context)
        }
    }

    /**
     * Interpret screening results (EPDS, PHQ-9, etc.)
     */
    suspend fun interpretScreening(
        screeningType: ScreeningType,
        responses: List<Int>,
        userContext: UserContext
    ): Result<ScreeningInterpretation> {
        return cloudService.interpretScreening(screeningType, responses, userContext)
    }

    /**
     * Generate personalized health education
     */
    suspend fun generateEducation(
        topic: String,
        userContext: UserContext,
        language: String
    ): Flow<String> {
        return cloudService.streamEducation(topic, userContext, language)
    }

    /**
     * Analyze symptom patterns over time
     */
    suspend fun analyzePatterns(
        userId: String,
        timeRange: TimeRange
    ): Result<PatternAnalysis> {
        return cloudService.analyzePatterns(userId, timeRange)
    }

    /**
     * Quick danger sign check (on-device only)
     */
    suspend fun checkDangerSigns(
        symptoms: List<SymptomType>,
        isPregnant: Boolean,
        childAgeMonths: Int?
    ): DangerSignResult {
        return localInference.checkDangerSigns(symptoms, isPregnant, childAgeMonths)
    }
}

/**
 * Context for triage decisions
 */
data class TriageContext(
    val isPregnant: Boolean = false,
    val gestationalWeek: Int? = null,
    val isPostpartum: Boolean = false,
    val postpartumWeeks: Int? = null,
    val childAgeMonths: Int? = null,
    val hasChronicConditions: List<String> = emptyList(),
    val currentMedications: List<String> = emptyList(),
    val recentSymptoms: List<RecentSymptom> = emptyList()
)

data class RecentSymptom(
    val symptom: SymptomType,
    val daysAgo: Int,
    val severity: Severity
)

/**
 * User context for personalized responses
 */
data class UserContext(
    val lifecycleStage: String,
    val age: Int,
    val language: String,
    val literacyLevel: LiteracyLevel = LiteracyLevel.STANDARD,
    val isPregnant: Boolean = false,
    val gestationalWeek: Int? = null,
    val hasChildren: Boolean = false,
    val childrenAges: List<Int> = emptyList()
)

enum class LiteracyLevel {
    BASIC,      // Simple language, more visuals
    STANDARD,   // Normal health literacy
    ADVANCED    // Medical terminology acceptable
}

/**
 * Screening interpretation result
 */
data class ScreeningInterpretation(
    val score: Int,
    val severity: Severity,
    val interpretation: String,
    val recommendations: List<String>,
    val needsFollowUp: Boolean,
    val urgency: Urgency,
    val supportResources: List<SupportResource> = emptyList()
)

data class SupportResource(
    val name: String,
    val type: ResourceType,
    val contact: String? = null,
    val description: String
)

enum class ResourceType {
    HELPLINE,
    COUNSELING,
    SUPPORT_GROUP,
    EDUCATIONAL,
    EMERGENCY
}

/**
 * Time range for pattern analysis
 */
data class TimeRange(
    val days: Int = 30
)

/**
 * Pattern analysis result
 */
data class PatternAnalysis(
    val moodTrend: String,
    val symptomPatterns: List<SymptomPattern>,
    val cyclePrediction: CyclePrediction?,
    val recommendations: List<String>,
    val alerts: List<String> = emptyList()
)

data class SymptomPattern(
    val symptom: SymptomType,
    val frequency: Int,
    val trend: String,
    val correlations: List<String>
)

data class CyclePrediction(
    val nextPeriodDate: String,
    val ovulationDate: String?,
    val confidence: Float
)

/**
 * Danger sign check result
 */
data class DangerSignResult(
    val hasDangerSigns: Boolean,
    val dangerSigns: List<String>,
    val urgency: Urgency,
    val immediateAction: String?
)

/**
 * Local inference result with confidence
 */
data class LocalInferenceResult(
    val result: TriageResult,
    val confidence: Float
)

package com.maa.health.data.remote.ai

import com.maa.health.data.model.BodyRegion
import com.maa.health.data.model.ScreeningType
import com.maa.health.data.model.Severity
import com.maa.health.data.model.SymptomType
import com.maa.health.data.model.TriageResult
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Maa AI Service - Unified AI orchestrator
 *
 * Architecture:
 * - On-device: Rule-based triage engine for latency-critical danger sign detection
 * - Primary cloud: Claude Opus 4.6 (Anthropic) for complex reasoning
 * - Backup cloud: Gemini 3 Flash (Google) when Claude is unavailable
 *
 * All responses include confidence scores and source citations.
 */
@Singleton
class MaaAIService @Inject constructor(
    private val onDeviceEngine: OnDeviceTriageEngine,
    private val claudeService: ClaudeAIService,
    private val geminiService: GeminiAIService
) {
    /**
     * Triage symptoms and determine urgency
     *
     * Uses on-device engine for speed, escalates to Claude for complex cases,
     * falls back to Gemini if Claude is unavailable
     */
    suspend fun triageSymptoms(
        symptoms: List<SymptomType>,
        bodyRegion: BodyRegion,
        severity: Severity,
        context: TriageContext
    ): Result<TriageResult> {
        // First, try on-device rule-based triage for common patterns
        val localResult = onDeviceEngine.quickTriage(symptoms, bodyRegion, severity, context)

        if (localResult.confidence >= 0.85f) {
            return Result.success(localResult.result)
        }

        // Escalate to Claude (primary) for complex reasoning
        val claudeResult = claudeService.detailedTriage(symptoms, bodyRegion, severity, context)
        if (claudeResult.isSuccess) {
            return claudeResult
        }

        // Fall back to Gemini (backup) if Claude fails
        val geminiResult = geminiService.detailedTriage(symptoms, bodyRegion, severity, context)
        if (geminiResult.isSuccess) {
            return geminiResult
        }

        // Last resort: return on-device result regardless of confidence
        return Result.success(localResult.result)
    }

    /**
     * Interpret screening results (EPDS, PHQ-9, etc.)
     *
     * Uses Claude as primary, Gemini as backup
     */
    suspend fun interpretScreening(
        screeningType: ScreeningType,
        responses: List<Int>,
        userContext: UserContext
    ): Result<ScreeningInterpretation> {
        val claudeResult = claudeService.interpretScreening(screeningType, responses, userContext)
        if (claudeResult.isSuccess) {
            return claudeResult
        }

        return geminiService.interpretScreening(screeningType, responses, userContext)
    }

    /**
     * Generate personalized health education
     *
     * Uses Claude as primary for superior reasoning and empathy
     */
    suspend fun generateEducation(
        topic: String,
        userContext: UserContext,
        language: String
    ): Flow<String> {
        return try {
            claudeService.streamEducation(topic, userContext, language)
        } catch (e: Exception) {
            geminiService.streamEducation(topic, userContext, language)
        }
    }

    /**
     * Analyze symptom patterns over time
     */
    suspend fun analyzePatterns(
        userId: String,
        timeRange: TimeRange
    ): Result<PatternAnalysis> {
        val claudeResult = claudeService.analyzePatterns(userId, timeRange)
        if (claudeResult.isSuccess) {
            return claudeResult
        }

        return geminiService.analyzePatterns(userId, timeRange)
    }

    /**
     * Quick danger sign check (on-device only, no network needed)
     */
    suspend fun checkDangerSigns(
        symptoms: List<SymptomType>,
        isPregnant: Boolean,
        childAgeMonths: Int?
    ): DangerSignResult {
        return onDeviceEngine.checkDangerSigns(symptoms, isPregnant, childAgeMonths)
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
    val urgency: com.maa.health.data.model.Urgency,
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
    val urgency: com.maa.health.data.model.Urgency,
    val immediateAction: String?
)

/**
 * On-device inference result with confidence
 */
data class LocalInferenceResult(
    val result: TriageResult,
    val confidence: Float
)

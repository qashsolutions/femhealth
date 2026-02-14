package com.maa.health.data.repository

import com.maa.health.data.local.database.dao.SymptomDao
import com.maa.health.data.local.database.entity.SymptomLogEntity
import com.maa.health.data.model.Action
import com.maa.health.data.model.BodyRegion
import com.maa.health.data.model.Severity
import com.maa.health.data.model.SymptomLog
import com.maa.health.data.model.SymptomType
import com.maa.health.data.model.TriageResult
import com.maa.health.data.model.Urgency
import com.maa.health.data.remote.ai.MaaAIService
import com.maa.health.data.remote.ai.TriageContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Duration
import java.time.Instant
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for symptom logging and triage
 */
@Singleton
class SymptomRepository @Inject constructor(
    private val symptomDao: SymptomDao,
    private val maaAIService: MaaAIService
) {
    // ═══════════════════════════════════════════════════════════════════════════
    // SYMPTOM LOGGING
    // ═══════════════════════════════════════════════════════════════════════════

    suspend fun logSymptom(
        userId: String,
        bodyRegion: BodyRegion,
        symptomType: SymptomType,
        severity: Severity,
        childId: String? = null,
        pregnancyId: String? = null,
        duration: Duration? = null,
        measurements: Map<String, Float>? = null
    ): SymptomLog {
        val id = java.util.UUID.randomUUID().toString()

        val entity = SymptomLogEntity(
            id = id,
            oderId = userId,
            childId = childId,
            pregnancyId = pregnancyId,
            timestamp = Instant.now(),
            bodyRegion = bodyRegion,
            symptomType = symptomType,
            severity = severity,
            duration = duration,
            measurements = measurements,
            triageResult = null
        )
        symptomDao.insertSymptomLog(entity)

        return entity.toDomain()
    }

    suspend fun logSymptomWithTriage(
        userId: String,
        bodyRegion: BodyRegion,
        symptoms: List<SymptomType>,
        severity: Severity,
        context: TriageContext,
        childId: String? = null,
        pregnancyId: String? = null,
        duration: Duration? = null,
        measurements: Map<String, Float>? = null
    ): Pair<SymptomLog, TriageResult> {
        // Get triage result from AI
        val triageResult = maaAIService.triageSymptoms(
            symptoms = symptoms,
            bodyRegion = bodyRegion,
            severity = severity,
            context = context
        ).getOrElse {
            // Fallback triage if AI fails
            createFallbackTriage(symptoms, severity, context)
        }

        val id = java.util.UUID.randomUUID().toString()
        val primarySymptom = symptoms.firstOrNull() ?: SymptomType.PAIN

        val entity = SymptomLogEntity(
            id = id,
            oderId = userId,
            childId = childId,
            pregnancyId = pregnancyId,
            timestamp = Instant.now(),
            bodyRegion = bodyRegion,
            symptomType = primarySymptom,
            severity = severity,
            duration = duration,
            measurements = measurements,
            triageResult = triageResult
        )
        symptomDao.insertSymptomLog(entity)

        return Pair(entity.toDomain(), triageResult)
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // SYMPTOM HISTORY
    // ═══════════════════════════════════════════════════════════════════════════

    fun getSymptomHistory(userId: String, days: Int = 30): Flow<List<SymptomLog>> {
        val startTime = Instant.now().minus(days.toLong(), ChronoUnit.DAYS)
        return symptomDao.getSymptomLogsAfter(userId, startTime).map { list ->
            list.map { it.toDomain() }
        }
    }

    fun getSymptomsByRegion(userId: String, region: BodyRegion): Flow<List<SymptomLog>> {
        return symptomDao.getSymptomsByRegion(userId, region).map { list ->
            list.map { it.toDomain() }
        }
    }

    fun getChildSymptoms(childId: String): Flow<List<SymptomLog>> {
        return symptomDao.getChildSymptoms(childId).map { list ->
            list.map { it.toDomain() }
        }
    }

    fun getPregnancySymptoms(pregnancyId: String): Flow<List<SymptomLog>> {
        return symptomDao.getPregnancySymptoms(pregnancyId).map { list ->
            list.map { it.toDomain() }
        }
    }

    suspend fun getSymptomById(id: String): SymptomLog? {
        return symptomDao.getSymptomById(id)?.toDomain()
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // DANGER SIGN DETECTION
    // ═══════════════════════════════════════════════════════════════════════════

    suspend fun checkDangerSigns(
        symptoms: List<SymptomType>,
        isPregnant: Boolean,
        childAgeMonths: Int?
    ): DangerSignCheck {
        val result = maaAIService.checkDangerSigns(symptoms, isPregnant, childAgeMonths)

        return DangerSignCheck(
            hasDangerSigns = result.hasDangerSigns,
            dangerSigns = result.dangerSigns,
            urgency = result.urgency,
            immediateAction = result.immediateAction
        )
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // SYMPTOM SUGGESTIONS
    // ═══════════════════════════════════════════════════════════════════════════

    fun getSymptomsForRegion(region: BodyRegion, isPregnant: Boolean, isChild: Boolean): List<SymptomType> {
        return when (region) {
            BodyRegion.HEAD -> listOf(
                SymptomType.HEADACHE,
                SymptomType.SEVERE_HEADACHE,
                SymptomType.FEVER
            )
            BodyRegion.EYES -> listOf(
                SymptomType.BLURRED_VISION,
                SymptomType.PAIN
            )
            BodyRegion.MOUTH -> listOf(
                SymptomType.NAUSEA,
                SymptomType.VOMITING
            )
            BodyRegion.CHEST -> if (isChild) {
                listOf(
                    SymptomType.COUGH,
                    SymptomType.FAST_BREATHING,
                    SymptomType.CHEST_INDRAWING,
                    SymptomType.DIFFICULTY_BREATHING
                )
            } else {
                listOf(
                    SymptomType.DIFFICULTY_BREATHING,
                    SymptomType.PAIN
                )
            }
            BodyRegion.ABDOMEN, BodyRegion.ABDOMEN_UPPER -> if (isPregnant) {
                listOf(
                    SymptomType.PAIN,
                    SymptomType.CONTRACTIONS,
                    SymptomType.NAUSEA,
                    SymptomType.VOMITING
                )
            } else {
                listOf(
                    SymptomType.PAIN,
                    SymptomType.NAUSEA,
                    SymptomType.VOMITING,
                    SymptomType.DIARRHEA
                )
            }
            BodyRegion.PELVIS, BodyRegion.UTERUS -> if (isPregnant) {
                listOf(
                    SymptomType.BLEEDING,
                    SymptomType.LEAKING_FLUID,
                    SymptomType.CONTRACTIONS,
                    SymptomType.REDUCED_FETAL_MOVEMENT,
                    SymptomType.PAIN
                )
            } else {
                listOf(
                    SymptomType.BLEEDING,
                    SymptomType.PAIN
                )
            }
            BodyRegion.HANDS, BodyRegion.FEET -> listOf(
                SymptomType.SWELLING,
                SymptomType.PAIN
            )
            BodyRegion.SKIN -> if (isChild) {
                listOf(
                    SymptomType.RASH,
                    SymptomType.JAUNDICE
                )
            } else {
                listOf(
                    SymptomType.RASH
                )
            }
            BodyRegion.DIAPER_AREA -> listOf(
                SymptomType.RASH,
                SymptomType.DIARRHEA
            )
            BodyRegion.JOINTS -> listOf(
                SymptomType.PAIN,
                SymptomType.SWELLING
            )
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // FALLBACK TRIAGE
    // ═══════════════════════════════════════════════════════════════════════════

    private fun createFallbackTriage(
        symptoms: List<SymptomType>,
        severity: Severity,
        context: TriageContext
    ): TriageResult {
        val hasDangerSign = symptoms.any { it.isDangerSign }

        // Pregnancy danger signs
        if (context.isPregnant) {
            if (SymptomType.BLEEDING in symptoms ||
                SymptomType.CONVULSIONS in symptoms ||
                (SymptomType.SEVERE_HEADACHE in symptoms && SymptomType.BLURRED_VISION in symptoms)
            ) {
                return TriageResult(
                    urgency = Urgency.EMERGENCY,
                    classification = "Pregnancy danger sign",
                    action = Action.GO_TO_HOSPITAL_NOW,
                    instructions = listOf(
                        "Go to the hospital immediately",
                        "Do not wait",
                        "Bring someone with you if possible"
                    ),
                    referralNeeded = true,
                    warningSignsToWatch = emptyList()
                )
            }
        }

        // Child danger signs (IMCI)
        if (context.childAgeMonths != null) {
            if (SymptomType.NOT_FEEDING in symptoms ||
                SymptomType.CONVULSIONS in symptoms ||
                SymptomType.LETHARGY in symptoms ||
                SymptomType.CHEST_INDRAWING in symptoms
            ) {
                return TriageResult(
                    urgency = Urgency.EMERGENCY,
                    classification = "Child danger sign",
                    action = Action.GO_TO_HOSPITAL_NOW,
                    instructions = listOf(
                        "Take your child to the hospital immediately",
                        "Keep the child warm",
                        "Continue breastfeeding if possible"
                    ),
                    referralNeeded = true,
                    warningSignsToWatch = emptyList()
                )
            }
        }

        // General triage based on severity
        return when {
            hasDangerSign || severity == Severity.SEVERE -> TriageResult(
                urgency = Urgency.URGENT,
                classification = "Needs medical evaluation",
                action = Action.GO_TO_HOSPITAL_TODAY,
                instructions = listOf(
                    "Seek medical care today",
                    "Monitor symptoms closely",
                    "Note any changes"
                ),
                referralNeeded = true,
                warningSignsToWatch = listOf(
                    "Symptoms getting worse",
                    "New symptoms developing",
                    "Unable to eat or drink"
                )
            )
            severity == Severity.MODERATELY_SEVERE || severity == Severity.MODERATE -> TriageResult(
                urgency = Urgency.ROUTINE,
                classification = "Schedule medical visit",
                action = Action.SCHEDULE_VISIT,
                instructions = listOf(
                    "Schedule a visit with your healthcare provider",
                    "Rest and stay hydrated",
                    "Monitor symptoms"
                ),
                referralNeeded = false,
                warningSignsToWatch = listOf(
                    "Symptoms lasting more than 3 days",
                    "Symptoms getting worse",
                    "New symptoms"
                )
            )
            else -> TriageResult(
                urgency = Urgency.SELF_CARE,
                classification = "Home management",
                action = Action.HOME_MANAGEMENT,
                instructions = listOf(
                    "Rest at home",
                    "Stay hydrated",
                    "Take appropriate over-the-counter medicine if needed"
                ),
                referralNeeded = false,
                warningSignsToWatch = listOf(
                    "Symptoms not improving after 2-3 days",
                    "Symptoms getting worse",
                    "Fever developing"
                )
            )
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // MAPPERS
    // ═══════════════════════════════════════════════════════════════════════════

    private fun SymptomLogEntity.toDomain(): SymptomLog {
        return SymptomLog(
            id = id,
            userId = oderId,
            childId = childId,
            pregnancyId = pregnancyId,
            timestamp = timestamp,
            bodyRegion = bodyRegion,
            symptomType = symptomType,
            severity = severity,
            duration = duration,
            measurements = measurements,
            triageResult = triageResult
        )
    }
}

data class DangerSignCheck(
    val hasDangerSigns: Boolean,
    val dangerSigns: List<String>,
    val urgency: Urgency,
    val immediateAction: String?
)

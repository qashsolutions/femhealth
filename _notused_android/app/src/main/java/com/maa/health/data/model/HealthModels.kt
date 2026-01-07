package com.maa.health.data.model

import java.time.Duration
import java.time.Instant
import java.time.LocalDate

// ═══════════════════════════════════════════════════════════════════════════════
// PREGNANCY MODELS
// ═══════════════════════════════════════════════════════════════════════════════

/**
 * Pregnancy profile
 */
data class Pregnancy(
    val id: String,
    val userId: String,
    val lmpDate: LocalDate? = null,           // Last menstrual period
    val eddDate: LocalDate? = null,           // Estimated due date
    val gestationalWeek: Int = 0,             // Calculated
    val isHighRisk: Boolean = false,
    val riskFactors: List<RiskFactor> = emptyList(),
    val deliveryDate: LocalDate? = null,      // If delivered
    val outcome: PregnancyOutcome? = null
)

enum class RiskFactor {
    PREVIOUS_CESAREAN,
    PREVIOUS_PRETERM,
    MULTIPLE_PREGNANCY,
    MATERNAL_AGE_OVER_35,
    MATERNAL_AGE_UNDER_18,
    HYPERTENSION,
    DIABETES,
    ANEMIA,
    PREVIOUS_MISCARRIAGE,
    PREVIOUS_STILLBIRTH,
    OBESITY,
    UNDERWEIGHT
}

enum class PregnancyOutcome {
    LIVE_BIRTH,
    CESAREAN,
    MISCARRIAGE,
    STILLBIRTH,
    ECTOPIC,
    ONGOING
}

// ═══════════════════════════════════════════════════════════════════════════════
// CHILD MODELS
// ═══════════════════════════════════════════════════════════════════════════════

/**
 * Child profile
 */
data class Child(
    val id: String,
    val userId: String,
    val name: String? = null,
    val dateOfBirth: LocalDate,
    val gender: Gender,
    val birthWeight: Float? = null,           // kg
    val birthHeight: Float? = null,           // cm
    val currentWeight: Float? = null,
    val currentHeight: Float? = null
) {
    val ageMonths: Int
        get() {
            val today = LocalDate.now()
            return ((today.year - dateOfBirth.year) * 12) +
                   (today.monthValue - dateOfBirth.monthValue)
        }

    val ageDays: Int
        get() = (LocalDate.now().toEpochDay() - dateOfBirth.toEpochDay()).toInt()
}

// ═══════════════════════════════════════════════════════════════════════════════
// CYCLE MODELS
// ═══════════════════════════════════════════════════════════════════════════════

/**
 * Menstrual cycle log
 */
data class CycleLog(
    val id: String,
    val userId: String,
    val startDate: LocalDate,
    val endDate: LocalDate? = null,
    val cycleLength: Int? = null,             // Calculated
    val flowDays: List<FlowDay> = emptyList(),
    val symptoms: List<CycleSymptom> = emptyList()
)

data class FlowDay(
    val date: LocalDate,
    val intensity: FlowIntensity,
    val symptoms: List<Symptom> = emptyList()
)

enum class FlowIntensity {
    SPOTTING,
    LIGHT,
    MEDIUM,
    HEAVY
}

enum class CycleSymptom {
    CRAMPS,
    HEADACHE,
    BLOATING,
    BREAST_TENDERNESS,
    MOOD_SWINGS,
    FATIGUE,
    ACNE,
    BACK_PAIN,
    NAUSEA
}

/**
 * Learned cycle pattern for predictions
 */
data class CyclePattern(
    val userId: String,
    val averageCycleLength: Int = 28,
    val cycleVariability: Int = 0,            // Standard deviation
    val averageOvulationDay: Int = 14,
    val predictedNextPeriod: LocalDate? = null,
    val predictedOvulation: LocalDate? = null,
    val confidence: Float = 0f,               // 0-1
    val cyclesAnalyzed: Int = 0,
    val irregularityFlag: Boolean = false,
    val pcosRiskFlag: Boolean = false
)

// ═══════════════════════════════════════════════════════════════════════════════
// MOOD/MENTAL HEALTH MODELS
// ═══════════════════════════════════════════════════════════════════════════════

/**
 * Mood log entry
 */
data class MoodLog(
    val id: String,
    val userId: String,
    val timestamp: Instant,
    val moodScore: Int,                       // 1-5
    val energyLevel: Int? = null,             // 1-5
    val sleepQuality: Int? = null,            // 1-5
    val anxietyLevel: Int? = null,            // 1-5
    val notes: String? = null,
    val voiceNoteUrl: String? = null,
    val detectedKeywords: List<String> = emptyList()
)

/**
 * Screening result (EPDS, PHQ-9, etc.)
 */
data class ScreeningResult(
    val id: String,
    val userId: String,
    val screeningType: ScreeningType,
    val timestamp: Instant,
    val score: Int,
    val severity: Severity,
    val responses: List<Int>,
    val interpretation: String,
    val recommendations: List<String> = emptyList(),
    val needsFollowUp: Boolean = false
)

enum class ScreeningType {
    EPDS,       // Edinburgh Postnatal Depression Scale
    PHQ9,       // Patient Health Questionnaire-9
    PHQ_A,      // Adolescent version
    GAD7,       // Generalized Anxiety Disorder
    PSS,        // Perceived Stress Scale
    M_CHAT,     // Modified Checklist for Autism in Toddlers
    ASQ3        // Ages and Stages Questionnaire
}

enum class Severity {
    NONE,
    MILD,
    MODERATE,
    MODERATELY_SEVERE,
    SEVERE
}

/**
 * Learned mood pattern
 */
data class MoodPattern(
    val userId: String,
    val averageMoodScore: Float = 0f,
    val moodTrend: Trend = Trend.BASELINE,
    val cycleMoodCorrelation: Float? = null,  // PMDD indicator
    val triggers: List<String> = emptyList(),
    val effectiveCopingStrategies: List<String> = emptyList(),
    val lastUpdated: Instant = Instant.now()
)

enum class Trend {
    IMPROVING,
    STABLE,
    DECLINING,
    BASELINE
}

// ═══════════════════════════════════════════════════════════════════════════════
// SYMPTOM/TRIAGE MODELS
// ═══════════════════════════════════════════════════════════════════════════════

/**
 * Symptom log entry
 */
data class SymptomLog(
    val id: String,
    val userId: String,
    val childId: String? = null,              // If child-related
    val pregnancyId: String? = null,          // If pregnancy-related
    val timestamp: Instant,
    val bodyRegion: BodyRegion,
    val symptomType: SymptomType,
    val severity: Severity,
    val duration: Duration? = null,
    val measurements: Map<String, Float>? = null,  // e.g., breathing rate, temp
    val triageResult: TriageResult? = null
)

enum class BodyRegion {
    HEAD,
    EYES,
    MOUTH,
    CHEST,
    ABDOMEN,
    ABDOMEN_UPPER,
    PELVIS,
    UTERUS,
    HANDS,
    FEET,
    SKIN,
    JOINTS,
    DIAPER_AREA
}

enum class SymptomType {
    // General
    FEVER,
    HEADACHE,
    FATIGUE,
    PAIN,
    SWELLING,
    NAUSEA,
    VOMITING,
    DIARRHEA,

    // Pregnancy danger signs
    BLEEDING,
    BLURRED_VISION,
    SEVERE_HEADACHE,
    REDUCED_FETAL_MOVEMENT,
    LEAKING_FLUID,
    CONTRACTIONS,
    CONVULSIONS,
    DIFFICULTY_BREATHING,

    // Child symptoms
    COUGH,
    FAST_BREATHING,
    CHEST_INDRAWING,
    NOT_FEEDING,
    LETHARGY,
    RASH,
    JAUNDICE;

    val isDangerSign: Boolean
        get() = this in listOf(
            BLEEDING, BLURRED_VISION, SEVERE_HEADACHE,
            REDUCED_FETAL_MOVEMENT, LEAKING_FLUID, CONVULSIONS,
            DIFFICULTY_BREATHING, NOT_FEEDING, CHEST_INDRAWING
        )
}

/**
 * Triage result
 */
data class TriageResult(
    val urgency: Urgency,
    val classification: String,               // e.g., "Pneumonia", "Severe dehydration"
    val action: Action,
    val instructions: List<String>,
    val referralNeeded: Boolean = false,
    val warningSignsToWatch: List<String> = emptyList()
)

enum class Urgency {
    EMERGENCY,    // Call ambulance / Go now
    URGENT,       // Go to hospital today
    ROUTINE,      // Schedule visit
    SELF_CARE     // Home management
}

enum class Action {
    CALL_EMERGENCY,
    GO_TO_HOSPITAL_NOW,
    GO_TO_HOSPITAL_TODAY,
    SCHEDULE_VISIT,
    HOME_MANAGEMENT
}

// ═══════════════════════════════════════════════════════════════════════════════
// CHILD HEALTH MODELS
// ═══════════════════════════════════════════════════════════════════════════════

/**
 * Vaccination record
 */
data class VaccinationRecord(
    val id: String,
    val childId: String,
    val vaccineType: VaccineType,
    val dueDate: LocalDate,
    val givenDate: LocalDate? = null,
    val batch: String? = null,
    val facility: String? = null,
    val sideEffects: List<String>? = null
)

enum class VaccineType {
    BCG,
    OPV_0,
    OPV_1,
    OPV_2,
    OPV_3,
    OPV_BOOSTER_1,
    OPV_BOOSTER_2,
    HEPATITIS_B_BIRTH,
    HEPATITIS_B_1,
    HEPATITIS_B_2,
    HEPATITIS_B_3,
    PENTAVALENT_1,
    PENTAVALENT_2,
    PENTAVALENT_3,
    DPT_BOOSTER_1,
    DPT_BOOSTER_2,
    ROTAVIRUS_1,
    ROTAVIRUS_2,
    ROTAVIRUS_3,
    PCV_1,
    PCV_2,
    PCV_BOOSTER,
    MEASLES_1,
    MEASLES_2,
    MR_1,
    MR_2,
    JE_1,
    JE_2,
    VITAMIN_A_1,
    VITAMIN_A_2,
    VITAMIN_A_3,
    VITAMIN_A_4,
    VITAMIN_A_5,
    VITAMIN_A_6,
    VITAMIN_A_7,
    VITAMIN_A_8,
    VITAMIN_A_9
}

/**
 * Growth measurement
 */
data class GrowthMeasurement(
    val id: String,
    val childId: String,
    val date: LocalDate,
    val weight: Float,                        // kg
    val height: Float,                        // cm
    val headCircumference: Float? = null,     // cm
    val weightForAgePercentile: Int = 0,
    val heightForAgePercentile: Int = 0,
    val weightForHeightPercentile: Int = 0
)

/**
 * Developmental milestone
 */
data class Milestone(
    val id: String,
    val childId: String,
    val milestoneType: MilestoneType,
    val expectedAgeMonths: Int,
    val achievedDate: LocalDate? = null,
    val notes: String? = null
)

enum class MilestoneType {
    // Gross motor
    HOLDS_HEAD_UP,
    ROLLS_OVER,
    SITS_WITHOUT_SUPPORT,
    CRAWLS,
    PULLS_TO_STAND,
    WALKS_INDEPENDENTLY,
    RUNS,
    CLIMBS_STAIRS,

    // Fine motor
    GRASPS_OBJECTS,
    TRANSFERS_OBJECTS,
    PINCER_GRASP,
    SCRIBBLES,
    BUILDS_TOWER,

    // Language
    COOS,
    BABBLES,
    FIRST_WORD,
    TWO_WORD_PHRASES,
    SENTENCES,

    // Social
    SOCIAL_SMILE,
    STRANGER_ANXIETY,
    PLAYS_PEEK_A_BOO,
    PARALLEL_PLAY,
    COOPERATIVE_PLAY
}

// ═══════════════════════════════════════════════════════════════════════════════
// MEDICATION MODELS
// ═══════════════════════════════════════════════════════════════════════════════

/**
 * Medication entry
 */
data class Medication(
    val id: String,
    val userId: String,
    val name: String,
    val dosage: String,
    val frequency: String,
    val startDate: LocalDate,
    val endDate: LocalDate? = null,
    val reminderTimes: List<String> = emptyList(),
    val isActive: Boolean = true
)

/**
 * Medication adherence log
 */
data class MedicationLog(
    val id: String,
    val medicationId: String,
    val scheduledTime: Instant,
    val takenTime: Instant? = null,
    val skipped: Boolean = false,
    val skipReason: String? = null
)

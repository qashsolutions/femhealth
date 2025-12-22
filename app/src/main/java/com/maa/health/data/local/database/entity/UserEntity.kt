package com.maa.health.data.local.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.maa.health.data.model.LifecycleStage
import com.maa.health.data.model.SubscriptionStatus
import com.maa.health.data.model.SupportedLanguage

/**
 * User entity for Room database
 */
@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey
    val id: String,
    val phoneNumber: String,
    val languageCode: String,
    val lifecycleStage: String,
    val dateOfBirth: Long? = null,            // Epoch day
    val biometricEnabled: Boolean = false,
    val subscriptionStatus: String = SubscriptionStatus.FREE.name,
    val createdAt: Long,                       // Epoch millis
    val lastActiveAt: Long                     // Epoch millis
)

/**
 * Pregnancy entity
 */
@Entity(tableName = "pregnancies")
data class PregnancyEntity(
    @PrimaryKey
    val id: String,
    val userId: String,
    val lmpDate: Long? = null,                // Epoch day
    val eddDate: Long? = null,                // Epoch day
    val isHighRisk: Boolean = false,
    val riskFactors: String = "",             // JSON array
    val deliveryDate: Long? = null,           // Epoch day
    val outcome: String? = null
)

/**
 * Child entity
 */
@Entity(tableName = "children")
data class ChildEntity(
    @PrimaryKey
    val id: String,
    val userId: String,
    val name: String? = null,
    val dateOfBirth: Long,                    // Epoch day
    val gender: String,
    val birthWeight: Float? = null,
    val birthHeight: Float? = null,
    val currentWeight: Float? = null,
    val currentHeight: Float? = null
)

/**
 * Cycle log entity
 */
@Entity(tableName = "cycle_logs")
data class CycleLogEntity(
    @PrimaryKey
    val id: String,
    val userId: String,
    val startDate: Long,                      // Epoch day
    val endDate: Long? = null,
    val cycleLength: Int? = null,
    val flowDays: String = "",                // JSON array
    val symptoms: String = ""                 // JSON array
)

/**
 * Mood log entity
 */
@Entity(tableName = "mood_logs")
data class MoodLogEntity(
    @PrimaryKey
    val id: String,
    val userId: String,
    val timestamp: Long,                      // Epoch millis
    val moodScore: Int,
    val energyLevel: Int? = null,
    val sleepQuality: Int? = null,
    val anxietyLevel: Int? = null,
    val notes: String? = null,
    val voiceNoteUrl: String? = null,
    val detectedKeywords: String = ""         // JSON array
)

/**
 * Symptom log entity
 */
@Entity(tableName = "symptom_logs")
data class SymptomLogEntity(
    @PrimaryKey
    val id: String,
    val userId: String,
    val childId: String? = null,
    val pregnancyId: String? = null,
    val timestamp: Long,                      // Epoch millis
    val bodyRegion: String,
    val symptomType: String,
    val severity: String,
    val durationMinutes: Long? = null,
    val measurements: String? = null,         // JSON object
    val triageResult: String? = null          // JSON object
)

/**
 * Screening result entity
 */
@Entity(tableName = "screening_results")
data class ScreeningResultEntity(
    @PrimaryKey
    val id: String,
    val userId: String,
    val screeningType: String,
    val timestamp: Long,                      // Epoch millis
    val score: Int,
    val severity: String,
    val responses: String,                    // JSON array
    val interpretation: String,
    val recommendations: String = "",         // JSON array
    val needsFollowUp: Boolean = false
)

/**
 * Vaccination record entity
 */
@Entity(tableName = "vaccinations")
data class VaccinationEntity(
    @PrimaryKey
    val id: String,
    val childId: String,
    val vaccineType: String,
    val dueDate: Long,                        // Epoch day
    val givenDate: Long? = null,
    val batch: String? = null,
    val facility: String? = null,
    val sideEffects: String? = null           // JSON array
)

/**
 * Growth measurement entity
 */
@Entity(tableName = "growth_measurements")
data class GrowthMeasurementEntity(
    @PrimaryKey
    val id: String,
    val childId: String,
    val date: Long,                           // Epoch day
    val weight: Float,
    val height: Float,
    val headCircumference: Float? = null,
    val weightForAgePercentile: Int = 0,
    val heightForAgePercentile: Int = 0,
    val weightForHeightPercentile: Int = 0
)

/**
 * Medication entity
 */
@Entity(tableName = "medications")
data class MedicationEntity(
    @PrimaryKey
    val id: String,
    val userId: String,
    val name: String,
    val dosage: String,
    val frequency: String,
    val startDate: Long,                      // Epoch day
    val endDate: Long? = null,
    val reminderTimes: String = "",           // JSON array
    val isActive: Boolean = true
)

/**
 * Cycle pattern entity (learned)
 */
@Entity(tableName = "cycle_patterns")
data class CyclePatternEntity(
    @PrimaryKey
    val userId: String,
    val averageCycleLength: Int = 28,
    val cycleVariability: Int = 0,
    val averageOvulationDay: Int = 14,
    val predictedNextPeriod: Long? = null,    // Epoch day
    val predictedOvulation: Long? = null,
    val confidence: Float = 0f,
    val cyclesAnalyzed: Int = 0,
    val irregularityFlag: Boolean = false,
    val pcosRiskFlag: Boolean = false
)

/**
 * Mood pattern entity (learned)
 */
@Entity(tableName = "mood_patterns")
data class MoodPatternEntity(
    @PrimaryKey
    val userId: String,
    val averageMoodScore: Float = 0f,
    val moodTrend: String = "BASELINE",
    val cycleMoodCorrelation: Float? = null,
    val triggers: String = "",                // JSON array
    val effectiveCopingStrategies: String = "",// JSON array
    val lastUpdated: Long                     // Epoch millis
)

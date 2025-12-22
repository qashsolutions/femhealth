package com.maa.health.data.local.database.dao

import androidx.room.*
import com.maa.health.data.local.database.entity.*
import kotlinx.coroutines.flow.Flow

// ═══════════════════════════════════════════════════════════════════════════════
// CYCLE DAO
// ═══════════════════════════════════════════════════════════════════════════════

@Dao
interface CycleDao {
    @Query("SELECT * FROM cycle_logs WHERE userId = :userId ORDER BY startDate DESC")
    fun getCycleLogs(userId: String): Flow<List<CycleLogEntity>>

    @Query("SELECT * FROM cycle_logs WHERE userId = :userId ORDER BY startDate DESC LIMIT :limit")
    suspend fun getRecentCycles(userId: String, limit: Int): List<CycleLogEntity>

    @Query("SELECT * FROM cycle_logs WHERE id = :id")
    suspend fun getCycleById(id: String): CycleLogEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCycleLog(cycleLog: CycleLogEntity)

    @Update
    suspend fun updateCycleLog(cycleLog: CycleLogEntity)

    @Delete
    suspend fun deleteCycleLog(cycleLog: CycleLogEntity)

    @Query("SELECT * FROM cycle_patterns WHERE userId = :userId")
    fun getCyclePattern(userId: String): Flow<CyclePatternEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCyclePattern(pattern: CyclePatternEntity)
}

// ═══════════════════════════════════════════════════════════════════════════════
// MOOD DAO
// ═══════════════════════════════════════════════════════════════════════════════

@Dao
interface MoodDao {
    @Query("SELECT * FROM mood_logs WHERE userId = :userId ORDER BY timestamp DESC")
    fun getMoodLogs(userId: String): Flow<List<MoodLogEntity>>

    @Query("SELECT * FROM mood_logs WHERE userId = :userId AND timestamp >= :since ORDER BY timestamp DESC")
    suspend fun getMoodLogsSince(userId: String, since: Long): List<MoodLogEntity>

    @Query("SELECT * FROM mood_logs WHERE userId = :userId ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecentMoodLogs(userId: String, limit: Int): List<MoodLogEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMoodLog(moodLog: MoodLogEntity)

    @Update
    suspend fun updateMoodLog(moodLog: MoodLogEntity)

    @Delete
    suspend fun deleteMoodLog(moodLog: MoodLogEntity)

    @Query("SELECT * FROM mood_patterns WHERE userId = :userId")
    fun getMoodPattern(userId: String): Flow<MoodPatternEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMoodPattern(pattern: MoodPatternEntity)
}

// ═══════════════════════════════════════════════════════════════════════════════
// SYMPTOM DAO
// ═══════════════════════════════════════════════════════════════════════════════

@Dao
interface SymptomDao {
    @Query("SELECT * FROM symptom_logs WHERE userId = :userId ORDER BY timestamp DESC")
    fun getSymptomLogs(userId: String): Flow<List<SymptomLogEntity>>

    @Query("SELECT * FROM symptom_logs WHERE userId = :userId AND timestamp >= :since ORDER BY timestamp DESC")
    suspend fun getSymptomLogsSince(userId: String, since: Long): List<SymptomLogEntity>

    @Query("SELECT * FROM symptom_logs WHERE childId = :childId ORDER BY timestamp DESC")
    fun getChildSymptomLogs(childId: String): Flow<List<SymptomLogEntity>>

    @Query("SELECT * FROM symptom_logs WHERE pregnancyId = :pregnancyId ORDER BY timestamp DESC")
    fun getPregnancySymptomLogs(pregnancyId: String): Flow<List<SymptomLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSymptomLog(symptomLog: SymptomLogEntity)

    @Delete
    suspend fun deleteSymptomLog(symptomLog: SymptomLogEntity)
}

// ═══════════════════════════════════════════════════════════════════════════════
// PREGNANCY DAO
// ═══════════════════════════════════════════════════════════════════════════════

@Dao
interface PregnancyDao {
    @Query("SELECT * FROM pregnancies WHERE userId = :userId AND outcome IS NULL")
    fun getCurrentPregnancy(userId: String): Flow<PregnancyEntity?>

    @Query("SELECT * FROM pregnancies WHERE userId = :userId ORDER BY lmpDate DESC")
    fun getAllPregnancies(userId: String): Flow<List<PregnancyEntity>>

    @Query("SELECT * FROM pregnancies WHERE id = :id")
    suspend fun getPregnancyById(id: String): PregnancyEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPregnancy(pregnancy: PregnancyEntity)

    @Update
    suspend fun updatePregnancy(pregnancy: PregnancyEntity)

    @Delete
    suspend fun deletePregnancy(pregnancy: PregnancyEntity)
}

// ═══════════════════════════════════════════════════════════════════════════════
// CHILD DAO
// ═══════════════════════════════════════════════════════════════════════════════

@Dao
interface ChildDao {
    @Query("SELECT * FROM children WHERE userId = :userId ORDER BY dateOfBirth DESC")
    fun getChildren(userId: String): Flow<List<ChildEntity>>

    @Query("SELECT * FROM children WHERE id = :id")
    suspend fun getChildById(id: String): ChildEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChild(child: ChildEntity)

    @Update
    suspend fun updateChild(child: ChildEntity)

    @Delete
    suspend fun deleteChild(child: ChildEntity)
}

// ═══════════════════════════════════════════════════════════════════════════════
// SCREENING DAO
// ═══════════════════════════════════════════════════════════════════════════════

@Dao
interface ScreeningDao {
    @Query("SELECT * FROM screening_results WHERE userId = :userId ORDER BY timestamp DESC")
    fun getScreeningResults(userId: String): Flow<List<ScreeningResultEntity>>

    @Query("SELECT * FROM screening_results WHERE userId = :userId AND screeningType = :type ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecentScreenings(userId: String, type: String, limit: Int): List<ScreeningResultEntity>

    @Query("SELECT * FROM screening_results WHERE id = :id")
    suspend fun getScreeningById(id: String): ScreeningResultEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScreeningResult(result: ScreeningResultEntity)

    @Delete
    suspend fun deleteScreeningResult(result: ScreeningResultEntity)
}

// ═══════════════════════════════════════════════════════════════════════════════
// VACCINATION DAO
// ═══════════════════════════════════════════════════════════════════════════════

@Dao
interface VaccinationDao {
    @Query("SELECT * FROM vaccinations WHERE childId = :childId ORDER BY dueDate")
    fun getVaccinations(childId: String): Flow<List<VaccinationEntity>>

    @Query("SELECT * FROM vaccinations WHERE childId = :childId AND givenDate IS NULL ORDER BY dueDate")
    fun getPendingVaccinations(childId: String): Flow<List<VaccinationEntity>>

    @Query("SELECT * FROM vaccinations WHERE childId = :childId AND dueDate <= :date AND givenDate IS NULL")
    suspend fun getOverdueVaccinations(childId: String, date: Long): List<VaccinationEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVaccination(vaccination: VaccinationEntity)

    @Update
    suspend fun updateVaccination(vaccination: VaccinationEntity)

    @Query("DELETE FROM vaccinations WHERE childId = :childId")
    suspend fun deleteVaccinationsForChild(childId: String)
}

// ═══════════════════════════════════════════════════════════════════════════════
// GROWTH DAO
// ═══════════════════════════════════════════════════════════════════════════════

@Dao
interface GrowthDao {
    @Query("SELECT * FROM growth_measurements WHERE childId = :childId ORDER BY date DESC")
    fun getGrowthMeasurements(childId: String): Flow<List<GrowthMeasurementEntity>>

    @Query("SELECT * FROM growth_measurements WHERE childId = :childId ORDER BY date DESC LIMIT 1")
    suspend fun getLatestMeasurement(childId: String): GrowthMeasurementEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMeasurement(measurement: GrowthMeasurementEntity)

    @Delete
    suspend fun deleteMeasurement(measurement: GrowthMeasurementEntity)
}

// ═══════════════════════════════════════════════════════════════════════════════
// MEDICATION DAO
// ═══════════════════════════════════════════════════════════════════════════════

@Dao
interface MedicationDao {
    @Query("SELECT * FROM medications WHERE userId = :userId AND isActive = 1")
    fun getActiveMedications(userId: String): Flow<List<MedicationEntity>>

    @Query("SELECT * FROM medications WHERE userId = :userId")
    fun getAllMedications(userId: String): Flow<List<MedicationEntity>>

    @Query("SELECT * FROM medications WHERE id = :id")
    suspend fun getMedicationById(id: String): MedicationEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedication(medication: MedicationEntity)

    @Update
    suspend fun updateMedication(medication: MedicationEntity)

    @Query("UPDATE medications SET isActive = 0 WHERE id = :id")
    suspend fun deactivateMedication(id: String)

    @Delete
    suspend fun deleteMedication(medication: MedicationEntity)
}

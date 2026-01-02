package com.maa.health.service

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.maa.health.data.local.database.MaaDatabase
import com.maa.health.data.local.database.entity.ChildEntity
import com.maa.health.data.local.database.entity.CycleLogEntity
import com.maa.health.data.local.database.entity.CyclePatternEntity
import com.maa.health.data.local.database.entity.GrowthMeasurementEntity
import com.maa.health.data.local.database.entity.MedicationEntity
import com.maa.health.data.local.database.entity.MoodLogEntity
import com.maa.health.data.local.database.entity.MoodPatternEntity
import com.maa.health.data.local.database.entity.PregnancyEntity
import com.maa.health.data.local.database.entity.ScreeningResultEntity
import com.maa.health.data.local.database.entity.SymptomLogEntity
import com.maa.health.data.local.database.entity.UserEntity
import com.maa.health.data.local.database.entity.VaccinationEntity
import com.maa.health.data.local.datastore.UserPreferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Cloud Sync Service for Firebase
 *
 * Provides:
 * - Backup to Firestore (premium feature)
 * - Restore from cloud
 * - Sync status tracking
 * - Conflict resolution
 *
 * Collections:
 * - users/{userId}/profile
 * - users/{userId}/symptoms
 * - users/{userId}/cycles
 * - users/{userId}/moods
 * - users/{userId}/pregnancies
 * - users/{userId}/children
 * - users/{userId}/vaccinations
 * - users/{userId}/screenings
 * - users/{userId}/medications
 */
@Singleton
class CloudSyncService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val database: MaaDatabase,
    private val userPreferencesDataStore: UserPreferencesDataStore
) {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    companion object {
        private const val COLLECTION_USERS = "users"
        private const val SUBCOLLECTION_PROFILE = "profile"
        private const val SUBCOLLECTION_SYMPTOMS = "symptoms"
        private const val SUBCOLLECTION_CYCLES = "cycles"
        private const val SUBCOLLECTION_MOODS = "moods"
        private const val SUBCOLLECTION_PREGNANCIES = "pregnancies"
        private const val SUBCOLLECTION_CHILDREN = "children"
        private const val SUBCOLLECTION_VACCINATIONS = "vaccinations"
        private const val SUBCOLLECTION_SCREENINGS = "screenings"
        private const val SUBCOLLECTION_MEDICATIONS = "medications"
        private const val SUBCOLLECTION_PATTERNS = "patterns"
    }

    /**
     * Check if user has premium subscription
     * All features are now free for all users
     */
    suspend fun isPremiumUser(): Boolean = true

    /**
     * Get current authenticated user ID
     */
    fun getCurrentUserId(): String? = auth.currentUser?.uid

    /**
     * Sync all data to cloud (now free for all users)
     */
    suspend fun syncToCloud(): SyncResult = withContext(Dispatchers.IO) {
        val userId = getCurrentUserId()
            ?: return@withContext SyncResult.Error("User not authenticated")

        try {
            val userRef = firestore.collection(COLLECTION_USERS).document(userId)

            // Sync user profile
            syncUserProfile(userId, userRef)

            // Sync symptoms
            syncSymptoms(userId, userRef)

            // Sync cycles
            syncCycles(userId, userRef)

            // Sync moods
            syncMoods(userId, userRef)

            // Sync pregnancies
            syncPregnancies(userId, userRef)

            // Sync children and vaccinations
            syncChildren(userId, userRef)

            // Sync medications
            syncMedications(userId, userRef)

            // Sync screenings
            syncScreenings(userId, userRef)

            // Update last sync time
            userRef.set(
                mapOf("lastSyncedAt" to Instant.now().toEpochMilli()),
                SetOptions.merge()
            ).await()

            userPreferencesDataStore.updateLastSyncTime(Instant.now())

            SyncResult.Success(Instant.now())
        } catch (e: Exception) {
            SyncResult.Error(e.message ?: "Sync failed")
        }
    }

    /**
     * Restore data from cloud (now free for all users)
     */
    suspend fun restoreFromCloud(): SyncResult = withContext(Dispatchers.IO) {
        val userId = getCurrentUserId()
            ?: return@withContext SyncResult.Error("User not authenticated")

        try {
            val userRef = firestore.collection(COLLECTION_USERS).document(userId)

            // Restore user profile
            restoreUserProfile(userId, userRef)

            // Restore symptoms
            restoreSymptoms(userId, userRef)

            // Restore cycles
            restoreCycles(userId, userRef)

            // Restore moods
            restoreMoods(userId, userRef)

            // Restore pregnancies
            restorePregnancies(userId, userRef)

            // Restore children
            restoreChildren(userId, userRef)

            // Restore medications
            restoreMedications(userId, userRef)

            // Restore screenings
            restoreScreenings(userId, userRef)

            userPreferencesDataStore.updateLastSyncTime(Instant.now())

            SyncResult.Success(Instant.now())
        } catch (e: Exception) {
            SyncResult.Error(e.message ?: "Restore failed")
        }
    }

    /**
     * Get last sync time
     */
    suspend fun getLastSyncTime(): Instant? {
        return userPreferencesDataStore.getUserPreferences().first().lastSyncedAt
    }

    // Private sync methods

    private suspend fun syncUserProfile(userId: String, userRef: com.google.firebase.firestore.DocumentReference) {
        val user = database.userDao().getUser(userId)
        user?.let {
            userRef.collection(SUBCOLLECTION_PROFILE).document("data")
                .set(it.toMap())
                .await()
        }
    }

    private suspend fun syncSymptoms(userId: String, userRef: com.google.firebase.firestore.DocumentReference) {
        val symptoms = database.symptomDao().getAllByUser(userId)
        val batch = firestore.batch()
        val collection = userRef.collection(SUBCOLLECTION_SYMPTOMS)

        symptoms.forEach { symptom ->
            batch.set(collection.document(symptom.id), symptom.toMap())
        }
        batch.commit().await()
    }

    private suspend fun syncCycles(userId: String, userRef: com.google.firebase.firestore.DocumentReference) {
        val cycles = database.cycleDao().getAllByUser(userId)
        val batch = firestore.batch()
        val collection = userRef.collection(SUBCOLLECTION_CYCLES)

        cycles.forEach { cycle ->
            batch.set(collection.document(cycle.id), cycle.toMap())
        }
        batch.commit().await()

        // Sync cycle pattern
        database.cycleDao().getPattern(userId)?.let { pattern ->
            userRef.collection(SUBCOLLECTION_PATTERNS).document("cycle")
                .set(pattern.toMap())
                .await()
        }
    }

    private suspend fun syncMoods(userId: String, userRef: com.google.firebase.firestore.DocumentReference) {
        val moods = database.moodDao().getAllByUser(userId)
        val batch = firestore.batch()
        val collection = userRef.collection(SUBCOLLECTION_MOODS)

        moods.forEach { mood ->
            batch.set(collection.document(mood.id), mood.toMap())
        }
        batch.commit().await()

        // Sync mood pattern
        database.moodDao().getPattern(userId)?.let { pattern ->
            userRef.collection(SUBCOLLECTION_PATTERNS).document("mood")
                .set(pattern.toMap())
                .await()
        }
    }

    private suspend fun syncPregnancies(userId: String, userRef: com.google.firebase.firestore.DocumentReference) {
        val pregnancies = database.pregnancyDao().getAllByUser(userId)
        val batch = firestore.batch()
        val collection = userRef.collection(SUBCOLLECTION_PREGNANCIES)

        pregnancies.forEach { pregnancy ->
            batch.set(collection.document(pregnancy.id), pregnancy.toMap())
        }
        batch.commit().await()
    }

    private suspend fun syncChildren(userId: String, userRef: com.google.firebase.firestore.DocumentReference) {
        val children = database.childDao().getAllByUser(userId)
        val batch = firestore.batch()
        val collection = userRef.collection(SUBCOLLECTION_CHILDREN)

        children.forEach { child ->
            batch.set(collection.document(child.id), child.toMap())

            // Sync vaccinations for each child
            val vaccinations = database.vaccinationDao().getAllByChild(child.id)
            vaccinations.forEach { vaccination ->
                batch.set(
                    collection.document(child.id)
                        .collection(SUBCOLLECTION_VACCINATIONS)
                        .document(vaccination.id),
                    vaccination.toMap()
                )
            }
        }
        batch.commit().await()
    }

    private suspend fun syncMedications(userId: String, userRef: com.google.firebase.firestore.DocumentReference) {
        val medications = database.medicationDao().getAllByUser(userId)
        val batch = firestore.batch()
        val collection = userRef.collection(SUBCOLLECTION_MEDICATIONS)

        medications.forEach { medication ->
            batch.set(collection.document(medication.id), medication.toMap())
        }
        batch.commit().await()
    }

    private suspend fun syncScreenings(userId: String, userRef: com.google.firebase.firestore.DocumentReference) {
        val screenings = database.screeningDao().getAllByUser(userId)
        val batch = firestore.batch()
        val collection = userRef.collection(SUBCOLLECTION_SCREENINGS)

        screenings.forEach { screening ->
            batch.set(collection.document(screening.id), screening.toMap())
        }
        batch.commit().await()
    }

    // Restore methods

    private suspend fun restoreUserProfile(userId: String, userRef: com.google.firebase.firestore.DocumentReference) {
        val snapshot = userRef.collection(SUBCOLLECTION_PROFILE).document("data").get().await()
        snapshot.toObject(UserEntity::class.java)?.let { user ->
            database.userDao().insert(user)
        }
    }

    private suspend fun restoreSymptoms(userId: String, userRef: com.google.firebase.firestore.DocumentReference) {
        val snapshot = userRef.collection(SUBCOLLECTION_SYMPTOMS).get().await()
        val symptoms = snapshot.documents.mapNotNull { it.toObject(SymptomLogEntity::class.java) }
        database.symptomDao().insertAll(symptoms)
    }

    private suspend fun restoreCycles(userId: String, userRef: com.google.firebase.firestore.DocumentReference) {
        val snapshot = userRef.collection(SUBCOLLECTION_CYCLES).get().await()
        val cycles = snapshot.documents.mapNotNull { it.toObject(CycleLogEntity::class.java) }
        database.cycleDao().insertAll(cycles)

        // Restore cycle pattern
        val patternSnapshot = userRef.collection(SUBCOLLECTION_PATTERNS).document("cycle").get().await()
        patternSnapshot.toObject(CyclePatternEntity::class.java)?.let { pattern ->
            database.cycleDao().insertPattern(pattern)
        }
    }

    private suspend fun restoreMoods(userId: String, userRef: com.google.firebase.firestore.DocumentReference) {
        val snapshot = userRef.collection(SUBCOLLECTION_MOODS).get().await()
        val moods = snapshot.documents.mapNotNull { it.toObject(MoodLogEntity::class.java) }
        database.moodDao().insertAll(moods)

        // Restore mood pattern
        val patternSnapshot = userRef.collection(SUBCOLLECTION_PATTERNS).document("mood").get().await()
        patternSnapshot.toObject(MoodPatternEntity::class.java)?.let { pattern ->
            database.moodDao().insertPattern(pattern)
        }
    }

    private suspend fun restorePregnancies(userId: String, userRef: com.google.firebase.firestore.DocumentReference) {
        val snapshot = userRef.collection(SUBCOLLECTION_PREGNANCIES).get().await()
        val pregnancies = snapshot.documents.mapNotNull { it.toObject(PregnancyEntity::class.java) }
        database.pregnancyDao().insertAll(pregnancies)
    }

    private suspend fun restoreChildren(userId: String, userRef: com.google.firebase.firestore.DocumentReference) {
        val snapshot = userRef.collection(SUBCOLLECTION_CHILDREN).get().await()
        snapshot.documents.forEach { childDoc ->
            childDoc.toObject(ChildEntity::class.java)?.let { child ->
                database.childDao().insert(child)

                // Restore vaccinations
                val vaccSnapshot = childDoc.reference.collection(SUBCOLLECTION_VACCINATIONS).get().await()
                val vaccinations = vaccSnapshot.documents.mapNotNull { it.toObject(VaccinationEntity::class.java) }
                database.vaccinationDao().insertAll(vaccinations)
            }
        }
    }

    private suspend fun restoreMedications(userId: String, userRef: com.google.firebase.firestore.DocumentReference) {
        val snapshot = userRef.collection(SUBCOLLECTION_MEDICATIONS).get().await()
        val medications = snapshot.documents.mapNotNull { it.toObject(MedicationEntity::class.java) }
        database.medicationDao().insertAll(medications)
    }

    private suspend fun restoreScreenings(userId: String, userRef: com.google.firebase.firestore.DocumentReference) {
        val snapshot = userRef.collection(SUBCOLLECTION_SCREENINGS).get().await()
        val screenings = snapshot.documents.mapNotNull { it.toObject(ScreeningResultEntity::class.java) }
        database.screeningDao().insertAll(screenings)
    }

    // Extension functions to convert entities to maps

    private fun UserEntity.toMap() = mapOf(
        "id" to id,
        "phoneNumber" to phoneNumber,
        "languageCode" to languageCode,
        "lifecycleStage" to lifecycleStage,
        "dateOfBirth" to dateOfBirth,
        "biometricEnabled" to biometricEnabled,
        "subscriptionStatus" to subscriptionStatus,
        "createdAt" to createdAt,
        "lastActiveAt" to lastActiveAt
    )

    private fun SymptomLogEntity.toMap() = mapOf(
        "id" to id,
        "userId" to userId,
        "childId" to childId,
        "pregnancyId" to pregnancyId,
        "timestamp" to timestamp,
        "bodyRegion" to bodyRegion,
        "symptomType" to symptomType,
        "severity" to severity,
        "durationMinutes" to durationMinutes,
        "measurements" to measurements,
        "triageResult" to triageResult
    )

    private fun CycleLogEntity.toMap() = mapOf(
        "id" to id,
        "userId" to userId,
        "startDate" to startDate,
        "endDate" to endDate,
        "cycleLength" to cycleLength,
        "flowDays" to flowDays,
        "symptoms" to symptoms
    )

    private fun CyclePatternEntity.toMap() = mapOf(
        "userId" to userId,
        "averageCycleLength" to averageCycleLength,
        "cycleVariability" to cycleVariability,
        "averageOvulationDay" to averageOvulationDay,
        "predictedNextPeriod" to predictedNextPeriod,
        "predictedOvulation" to predictedOvulation,
        "confidence" to confidence,
        "cyclesAnalyzed" to cyclesAnalyzed,
        "irregularityFlag" to irregularityFlag,
        "pcosRiskFlag" to pcosRiskFlag
    )

    private fun MoodLogEntity.toMap() = mapOf(
        "id" to id,
        "userId" to userId,
        "timestamp" to timestamp,
        "moodScore" to moodScore,
        "energyLevel" to energyLevel,
        "sleepQuality" to sleepQuality,
        "anxietyLevel" to anxietyLevel,
        "notes" to notes,
        "voiceNoteUrl" to voiceNoteUrl,
        "detectedKeywords" to detectedKeywords
    )

    private fun MoodPatternEntity.toMap() = mapOf(
        "userId" to userId,
        "averageMoodScore" to averageMoodScore,
        "moodTrend" to moodTrend,
        "cycleMoodCorrelation" to cycleMoodCorrelation,
        "triggers" to triggers,
        "effectiveCopingStrategies" to effectiveCopingStrategies,
        "lastUpdated" to lastUpdated
    )

    private fun PregnancyEntity.toMap() = mapOf(
        "id" to id,
        "userId" to userId,
        "lmpDate" to lmpDate,
        "eddDate" to eddDate,
        "isHighRisk" to isHighRisk,
        "riskFactors" to riskFactors,
        "deliveryDate" to deliveryDate,
        "outcome" to outcome
    )

    private fun ChildEntity.toMap() = mapOf(
        "id" to id,
        "userId" to userId,
        "name" to name,
        "dateOfBirth" to dateOfBirth,
        "gender" to gender,
        "birthWeight" to birthWeight,
        "birthHeight" to birthHeight,
        "currentWeight" to currentWeight,
        "currentHeight" to currentHeight
    )

    private fun VaccinationEntity.toMap() = mapOf(
        "id" to id,
        "childId" to childId,
        "vaccineType" to vaccineType,
        "dueDate" to dueDate,
        "givenDate" to givenDate,
        "batch" to batch,
        "facility" to facility,
        "sideEffects" to sideEffects
    )

    private fun MedicationEntity.toMap() = mapOf(
        "id" to id,
        "userId" to userId,
        "name" to name,
        "dosage" to dosage,
        "frequency" to frequency,
        "startDate" to startDate,
        "endDate" to endDate,
        "reminderTimes" to reminderTimes,
        "isActive" to isActive
    )

    private fun ScreeningResultEntity.toMap() = mapOf(
        "id" to id,
        "userId" to userId,
        "screeningType" to screeningType,
        "timestamp" to timestamp,
        "score" to score,
        "severity" to severity,
        "responses" to responses,
        "interpretation" to interpretation,
        "recommendations" to recommendations,
        "needsFollowUp" to needsFollowUp
    )
}

/**
 * Result of a sync operation
 */
sealed class SyncResult {
    data class Success(val syncedAt: Instant) : SyncResult()
    data class Error(val message: String) : SyncResult()
}

package com.maa.health.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.maa.health.data.local.database.dao.*
import com.maa.health.data.local.database.entity.*
import com.maa.health.data.local.dao.UserInteractionDao
import com.maa.health.data.local.entity.UserInteractionEntity
import com.maa.health.data.local.entity.ContentFeedbackEntity
import com.maa.health.data.local.entity.ObservationHistoryEntity
import com.maa.health.data.local.entity.LearnedPreferenceEntity
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Main Room database for Maa app
 *
 * Contains all entities for offline-first functionality
 */
@Database(
    entities = [
        UserEntity::class,
        PregnancyEntity::class,
        ChildEntity::class,
        CycleLogEntity::class,
        MoodLogEntity::class,
        SymptomLogEntity::class,
        ScreeningResultEntity::class,
        VaccinationEntity::class,
        GrowthMeasurementEntity::class,
        MedicationEntity::class,
        CyclePatternEntity::class,
        MoodPatternEntity::class,
        // Agentic Learning System entities
        UserInteractionEntity::class,
        ContentFeedbackEntity::class,
        ObservationHistoryEntity::class,
        LearnedPreferenceEntity::class
    ],
    version = 2,  // Incremented for new entities
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class MaaDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun cycleDao(): CycleDao
    abstract fun moodDao(): MoodDao
    abstract fun symptomDao(): SymptomDao
    abstract fun pregnancyDao(): PregnancyDao
    abstract fun childDao(): ChildDao
    abstract fun screeningDao(): ScreeningDao
    abstract fun vaccinationDao(): VaccinationDao
    abstract fun growthDao(): GrowthDao
    abstract fun medicationDao(): MedicationDao

    // Agentic Learning System DAO
    abstract fun userInteractionDao(): UserInteractionDao
}

/**
 * Type converters for Room database
 */
class Converters {
    private val json = Json { ignoreUnknownKeys = true }

    @TypeConverter
    fun fromStringList(value: String): List<String> {
        return if (value.isEmpty()) emptyList()
        else try {
            json.decodeFromString<List<String>>(value)
        } catch (e: Exception) {
            value.split(",").filter { it.isNotEmpty() }
        }
    }

    @TypeConverter
    fun toStringList(list: List<String>): String {
        return json.encodeToString(list)
    }

    @TypeConverter
    fun fromIntList(value: String): List<Int> {
        return if (value.isEmpty()) emptyList()
        else try {
            json.decodeFromString<List<Int>>(value)
        } catch (e: Exception) {
            emptyList()
        }
    }

    @TypeConverter
    fun toIntList(list: List<Int>): String {
        return json.encodeToString(list)
    }

    @TypeConverter
    fun fromFloatMap(value: String?): Map<String, Float> {
        return if (value.isNullOrEmpty()) emptyMap()
        else try {
            json.decodeFromString<Map<String, Float>>(value)
        } catch (e: Exception) {
            emptyMap()
        }
    }

    @TypeConverter
    fun toFloatMap(map: Map<String, Float>?): String {
        return if (map == null) "" else json.encodeToString(map)
    }
}

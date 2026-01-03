package com.maa.health.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.maa.health.data.local.database.MaaDatabase
import com.maa.health.data.local.database.dao.*
import com.maa.health.data.local.dao.UserInteractionDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Database dependency injection module
 *
 * Provides Room database and all DAOs
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    /**
     * Migration from version 1 to 2
     * Adds the agentic learning system tables for tracking user interactions,
     * content feedback, observation history, and learned preferences.
     */
    private val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // Create user_interactions table
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS user_interactions (
                    id TEXT NOT NULL PRIMARY KEY,
                    userId TEXT NOT NULL,
                    timestamp INTEGER NOT NULL,
                    interactionType TEXT NOT NULL,
                    targetId TEXT,
                    targetType TEXT,
                    metadata TEXT,
                    sessionId TEXT,
                    durationMs INTEGER
                )
            """.trimIndent())
            db.execSQL("CREATE INDEX IF NOT EXISTS index_user_interactions_userId_timestamp ON user_interactions (userId, timestamp)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_user_interactions_userId_interactionType ON user_interactions (userId, interactionType)")

            // Create content_feedback table
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS content_feedback (
                    id TEXT NOT NULL PRIMARY KEY,
                    userId TEXT NOT NULL,
                    contentId TEXT NOT NULL,
                    contentType TEXT NOT NULL,
                    contentTopic TEXT,
                    wasHelpful INTEGER NOT NULL,
                    feedbackText TEXT,
                    timestamp INTEGER NOT NULL
                )
            """.trimIndent())
            db.execSQL("CREATE INDEX IF NOT EXISTS index_content_feedback_userId_contentId ON content_feedback (userId, contentId)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_content_feedback_userId_wasHelpful ON content_feedback (userId, wasHelpful)")

            // Create observation_history table
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS observation_history (
                    id TEXT NOT NULL PRIMARY KEY,
                    userId TEXT NOT NULL,
                    timestamp INTEGER NOT NULL,
                    observationType TEXT NOT NULL,
                    observation TEXT NOT NULL,
                    dataPointsAnalyzed INTEGER NOT NULL,
                    timeSpanDays INTEGER NOT NULL,
                    suggestsFollowUp INTEGER NOT NULL,
                    wasActedUpon INTEGER,
                    userFeedback TEXT
                )
            """.trimIndent())
            db.execSQL("CREATE INDEX IF NOT EXISTS index_observation_history_userId_timestamp ON observation_history (userId, timestamp)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_observation_history_userId_observationType ON observation_history (userId, observationType)")

            // Create learned_preferences table
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS learned_preferences (
                    id TEXT NOT NULL PRIMARY KEY,
                    userId TEXT NOT NULL,
                    preferenceType TEXT NOT NULL,
                    preferenceKey TEXT NOT NULL,
                    preferenceValue TEXT NOT NULL,
                    confidence REAL NOT NULL,
                    lastUpdated INTEGER NOT NULL,
                    dataPoints INTEGER NOT NULL
                )
            """.trimIndent())
            db.execSQL("CREATE INDEX IF NOT EXISTS index_learned_preferences_userId ON learned_preferences (userId)")
        }
    }

    @Provides
    @Singleton
    fun provideMaaDatabase(
        @ApplicationContext context: Context
    ): MaaDatabase {
        return Room.databaseBuilder(
            context,
            MaaDatabase::class.java,
            "maa_database"
        )
            .addMigrations(MIGRATION_1_2)
            // Fallback to destructive only for development - remove in production
            .fallbackToDestructiveMigrationOnDowngrade()
            .build()
    }

    @Provides
    @Singleton
    fun provideUserDao(database: MaaDatabase): UserDao = database.userDao()

    @Provides
    @Singleton
    fun provideCycleDao(database: MaaDatabase): CycleDao = database.cycleDao()

    @Provides
    @Singleton
    fun provideMoodDao(database: MaaDatabase): MoodDao = database.moodDao()

    @Provides
    @Singleton
    fun provideSymptomDao(database: MaaDatabase): SymptomDao = database.symptomDao()

    @Provides
    @Singleton
    fun providePregnancyDao(database: MaaDatabase): PregnancyDao = database.pregnancyDao()

    @Provides
    @Singleton
    fun provideChildDao(database: MaaDatabase): ChildDao = database.childDao()

    @Provides
    @Singleton
    fun provideScreeningDao(database: MaaDatabase): ScreeningDao = database.screeningDao()

    @Provides
    @Singleton
    fun provideVaccinationDao(database: MaaDatabase): VaccinationDao = database.vaccinationDao()

    @Provides
    @Singleton
    fun provideGrowthDao(database: MaaDatabase): GrowthDao = database.growthDao()

    @Provides
    @Singleton
    fun provideMedicationDao(database: MaaDatabase): MedicationDao = database.medicationDao()

    // Agentic Learning System DAO
    @Provides
    @Singleton
    fun provideUserInteractionDao(database: MaaDatabase): UserInteractionDao = database.userInteractionDao()
}

package com.maa.health.di

import android.content.Context
import androidx.room.Room
import com.maa.health.data.local.database.MaaDatabase
import com.maa.health.data.local.database.dao.*
import com.maa.health.security.EncryptedStorageManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Database dependency injection module
 *
 * Provides encrypted Room database and all DAOs.
 * Uses SQLCipher for database encryption at rest.
 *
 * Security: All health data is encrypted using AES-256 before
 * being written to disk. The encryption key is stored in
 * Android Keystore (hardware-backed when available).
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideMaaDatabase(
        @ApplicationContext context: Context,
        encryptedStorageManager: EncryptedStorageManager
    ): MaaDatabase {
        // Get SQLCipher factory for encrypted database
        val factory = encryptedStorageManager.getDatabaseEncryptionFactory()

        return Room.databaseBuilder(
            context,
            MaaDatabase::class.java,
            "maa_database"
        )
            .openHelperFactory(factory)  // Use SQLCipher encryption
            .fallbackToDestructiveMigration()
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
}

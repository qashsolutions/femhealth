package com.maa.health.di

import com.maa.health.data.local.dao.UserInteractionDao
import com.maa.health.data.remote.ai.GeminiObservationService
import com.maa.health.data.remote.medical.MedlinePlusService
import com.maa.health.data.repository.AgenticObservationRepository
import com.maa.health.data.repository.CycleRepository
import com.maa.health.data.repository.MoodRepository
import com.maa.health.data.repository.SymptomRepository
import com.maa.health.data.repository.UserRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Repository dependency injection module
 *
 * Provides the AgenticObservationRepository which orchestrates
 * the agentic learning and personalization system.
 */
@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideAgenticObservationRepository(
        userInteractionDao: UserInteractionDao,
        geminiService: GeminiObservationService,
        medlinePlusService: MedlinePlusService,
        moodRepository: MoodRepository,
        symptomRepository: SymptomRepository,
        cycleRepository: CycleRepository,
        userRepository: UserRepository
    ): AgenticObservationRepository {
        return AgenticObservationRepository(
            userInteractionDao = userInteractionDao,
            geminiService = geminiService,
            medlinePlusService = medlinePlusService,
            moodRepository = moodRepository,
            symptomRepository = symptomRepository,
            cycleRepository = cycleRepository,
            userRepository = userRepository
        )
    }
}

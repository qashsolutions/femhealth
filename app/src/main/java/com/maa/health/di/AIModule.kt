package com.maa.health.di

import android.content.Context
import com.maa.health.data.remote.medgemma.MedGemmaService
import com.maa.health.data.remote.medgemma.MedGemmaLocalInference
import com.maa.health.data.remote.medgemma.MedGemmaCloudService
import com.maa.health.data.remote.ai.GeminiObservationService
import com.maa.health.data.remote.medical.MedlinePlusService
import com.maa.health.data.remote.medical.OpenFDAService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import javax.inject.Named
import javax.inject.Singleton

/**
 * AI & Medical Services Dependency Injection Module
 *
 * Provides all AI and medical data services:
 *
 * 1. MedGemma Services (legacy) - On-device and cloud medical AI
 * 2. Gemini Observation Service - Pattern analysis (observations only, no diagnoses)
 * 3. MedlinePlus Service - NIH-verified health content (FREE)
 * 4. OpenFDA Service - FDA-verified drug information (FREE)
 *
 * ARCHITECTURE:
 * ┌─────────────────────────────────────────────────────────────────┐
 * │                    VERIFIED CONTENT LAYER                        │
 * │  MedlinePlus (NIH)  │  OpenFDA (FDA)  │  WHO Guidelines          │
 * │  - Health topics    │  - Drug safety  │  - Danger signs          │
 * │  - Patient ed       │  - Interactions │  - Clinical rules        │
 * ├─────────────────────────────────────────────────────────────────┤
 * │                    AI OBSERVATION LAYER                          │
 * │  GeminiObservationService                                       │
 * │  - Pattern detection (NOT diagnosis)                            │
 * │  - Trend analysis                                               │
 * │  - Content personalization                                       │
 * ├─────────────────────────────────────────────────────────────────┤
 * │                    SAFETY LAYER                                  │
 * │  MedGemmaLocalInference                                         │
 * │  - Rule-based danger sign detection                             │
 * │  - WHO/IMCI guidelines (hardcoded)                              │
 * │  - Offline capable                                              │
 * └─────────────────────────────────────────────────────────────────┘
 */
@Module
@InstallIn(SingletonComponent::class)
object AIModule {

    // ===== MedGemma Services (Legacy - Rule-based safety) =====

    @Provides
    @Singleton
    fun provideMedGemmaLocalInference(
        @ApplicationContext context: Context
    ): MedGemmaLocalInference {
        return MedGemmaLocalInference(context)
    }

    @Provides
    @Singleton
    fun provideMedGemmaCloudService(
        @Named("default") okHttpClient: OkHttpClient
    ): MedGemmaCloudService {
        return MedGemmaCloudService(okHttpClient)
    }

    @Provides
    @Singleton
    fun provideMedGemmaService(
        localInference: MedGemmaLocalInference,
        cloudService: MedGemmaCloudService
    ): MedGemmaService {
        return MedGemmaService(localInference, cloudService)
    }

    // ===== Gemini AI Observation Service =====

    @Provides
    @Singleton
    fun provideGeminiObservationService(): GeminiObservationService {
        return GeminiObservationService()
    }

    // ===== Medical Content Services (Verified Sources) =====

    @Provides
    @Singleton
    fun provideMedlinePlusService(
        @Named("default") okHttpClient: OkHttpClient
    ): MedlinePlusService {
        return MedlinePlusService(okHttpClient)
    }

    @Provides
    @Singleton
    fun provideOpenFDAService(
        @Named("default") okHttpClient: OkHttpClient
    ): OpenFDAService {
        return OpenFDAService(okHttpClient)
    }
}

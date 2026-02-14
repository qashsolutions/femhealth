package com.maa.health.di

import com.maa.health.data.remote.ai.ClaudeAIService
import com.maa.health.data.remote.ai.GeminiAIService
import com.maa.health.data.remote.ai.MaaAIService
import com.maa.health.data.remote.ai.OnDeviceTriageEngine
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import javax.inject.Named
import javax.inject.Singleton

/**
 * AI dependency injection module
 *
 * Provides AI services:
 * - OnDeviceTriageEngine: Rule-based on-device triage (WHO/IMCI protocols)
 * - ClaudeAIService: Primary cloud AI (Anthropic Claude Opus 4.6)
 * - GeminiAIService: Backup cloud AI (Google Gemini 3 Flash)
 * - MaaAIService: Unified orchestrator with automatic failover
 */
@Module
@InstallIn(SingletonComponent::class)
object AIModule {

    @Provides
    @Singleton
    fun provideOnDeviceTriageEngine(): OnDeviceTriageEngine {
        return OnDeviceTriageEngine()
    }

    @Provides
    @Singleton
    fun provideClaudeAIService(
        @Named("claude") okHttpClient: OkHttpClient
    ): ClaudeAIService {
        return ClaudeAIService(okHttpClient)
    }

    @Provides
    @Singleton
    fun provideGeminiAIService(
        @Named("gemini") okHttpClient: OkHttpClient
    ): GeminiAIService {
        return GeminiAIService(okHttpClient)
    }

    @Provides
    @Singleton
    fun provideMaaAIService(
        onDeviceEngine: OnDeviceTriageEngine,
        claudeService: ClaudeAIService,
        geminiService: GeminiAIService
    ): MaaAIService {
        return MaaAIService(onDeviceEngine, claudeService, geminiService)
    }
}

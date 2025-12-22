package com.maa.health.di

import android.content.Context
import com.maa.health.data.remote.medgemma.MedGemmaService
import com.maa.health.data.remote.medgemma.MedGemmaLocalInference
import com.maa.health.data.remote.medgemma.MedGemmaCloudService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import javax.inject.Named
import javax.inject.Singleton

/**
 * AI dependency injection module
 *
 * Provides MedGemma services for clinical reasoning
 */
@Module
@InstallIn(SingletonComponent::class)
object AIModule {

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
}

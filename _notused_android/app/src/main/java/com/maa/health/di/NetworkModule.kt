package com.maa.health.di

import com.maa.health.BuildConfig
import com.maa.health.data.remote.google.GoogleTranslateService
import com.maa.health.data.remote.sarvam.SarvamApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

/**
 * Network dependency injection module
 *
 * Provides OkHttp clients and API services for:
 * - Claude AI (Anthropic) - Primary AI for triage and reasoning
 * - Gemini AI (Google) - Backup AI for failover
 * - Sarvam AI (Indian languages - STT/TTS)
 * - Google Translate (International languages for Africa & other South Asia)
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            // Security: Use BASIC level even in debug to avoid logging sensitive data
            // BODY level would expose API keys and personal health information
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BASIC
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
            // Redact sensitive headers
            redactHeader("Authorization")
            redactHeader("Cookie")
            redactHeader("Set-Cookie")
            redactHeader("X-Goog-Api-Key")
            redactHeader("x-api-key")
            redactHeader("anthropic-api-key")
        }
    }

    @Provides
    @Singleton
    @Named("default")
    fun provideOkHttpClient(
        loggingInterceptor: HttpLoggingInterceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    @Named("claude")
    fun provideClaudeOkHttpClient(
        loggingInterceptor: HttpLoggingInterceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .addHeader("x-api-key", BuildConfig.CLAUDE_API_KEY)
                    .addHeader("anthropic-version", "2023-01-01")
                    .addHeader("Content-Type", "application/json")
                    .build()
                chain.proceed(request)
            }
            .addInterceptor(loggingInterceptor)
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)  // Claude can take longer for complex reasoning
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    @Named("gemini")
    fun provideGeminiOkHttpClient(
        loggingInterceptor: HttpLoggingInterceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor { chain ->
                val originalUrl = chain.request().url
                val newUrl = originalUrl.newBuilder()
                    .addQueryParameter("key", BuildConfig.GEMINI_API_KEY)
                    .build()
                val request = chain.request().newBuilder()
                    .url(newUrl)
                    .addHeader("Content-Type", "application/json")
                    .build()
                chain.proceed(request)
            }
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    @Named("sarvam")
    fun provideSarvamOkHttpClient(
        loggingInterceptor: HttpLoggingInterceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .addHeader("Authorization", "Bearer ${BuildConfig.SARVAM_API_KEY}")
                    .addHeader("Content-Type", "application/json")
                    .build()
                chain.proceed(request)
            }
            .addInterceptor(loggingInterceptor)
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    @Named("google")
    fun provideGoogleOkHttpClient(
        loggingInterceptor: HttpLoggingInterceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor { chain ->
                val originalUrl = chain.request().url
                val newUrl = originalUrl.newBuilder()
                    .addQueryParameter("key", BuildConfig.GOOGLE_TRANSLATE_API_KEY)
                    .build()
                val request = chain.request().newBuilder()
                    .url(newUrl)
                    .addHeader("Content-Type", "application/json")
                    .build()
                chain.proceed(request)
            }
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideSarvamApiService(
        @Named("sarvam") okHttpClient: OkHttpClient
    ): SarvamApiService {
        return SarvamApiService(okHttpClient)
    }

    @Provides
    @Singleton
    fun provideGoogleTranslateService(
        @Named("google") okHttpClient: OkHttpClient
    ): GoogleTranslateService {
        return GoogleTranslateService(okHttpClient)
    }
}

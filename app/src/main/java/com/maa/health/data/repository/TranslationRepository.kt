package com.maa.health.data.repository

import com.maa.health.data.model.SupportedCountry
import com.maa.health.data.model.SupportedLanguage
import com.maa.health.data.remote.google.GoogleTranslateService
import com.maa.health.data.remote.sarvam.SarvamApiService
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Unified Translation Repository
 *
 * Routes translation requests to the appropriate service:
 * - Sarvam AI: For Indian users (optimized for Indian languages, accents, and code-mixing)
 * - Google Translate: For users in other countries (Africa, other South Asia)
 *
 * This provides the best experience for each region while keeping costs optimal.
 */
@Singleton
class TranslationRepository @Inject constructor(
    private val sarvamApiService: SarvamApiService,
    private val googleTranslateService: GoogleTranslateService,
    private val userRepository: UserRepository
) {
    /**
     * Translate text to the user's preferred language
     *
     * @param text The text to translate (usually in English)
     * @param country The user's country (determines which service to use)
     * @param targetLanguage Override target language (optional, uses country default if not specified)
     * @return Translated text
     */
    suspend fun translateText(
        text: String,
        country: SupportedCountry,
        targetLanguage: String? = null
    ): Result<String> {
        val target = targetLanguage ?: country.defaultLanguage

        return if (SupportedCountry.usesSarvamAI(country)) {
            // Use Sarvam AI for Indian users
            translateWithSarvam(text, target)
        } else {
            // Use Google Translate for other countries
            googleTranslateService.translate(text, target, sourceLanguage = "en")
        }
    }

    /**
     * Translate using Sarvam AI (for Indian languages)
     *
     * Note: Sarvam's primary strength is STT/TTS, but they also offer translation.
     * For text-only translation, we may still use their API or fall back to internal handling.
     */
    private suspend fun translateWithSarvam(
        text: String,
        targetLanguage: String
    ): Result<String> {
        // For now, return the original text as Sarvam focuses on STT/TTS
        // The app already has localized strings for Indian languages
        // TODO: Integrate Sarvam translation API when available
        return Result.success(text)
    }

    /**
     * Speech to Text - Converts audio to text
     *
     * @param audioFile The audio file to transcribe
     * @param country The user's country
     * @param languageCode The language code for transcription
     * @return Transcribed text
     */
    suspend fun speechToText(
        audioFile: File,
        country: SupportedCountry,
        languageCode: String
    ): Result<String> {
        return if (SupportedCountry.usesSarvamAI(country)) {
            // Sarvam provides superior STT for Indian languages
            val sarvamCode = getSarvamLanguageCode(languageCode)
            sarvamApiService.speechToText(audioFile, sarvamCode)
        } else {
            // For non-Indian countries, return error as we don't have Google STT integrated
            // TODO: Integrate Google Speech-to-Text API for African languages
            Result.failure(Exception("Speech-to-text not yet available for this region"))
        }
    }

    /**
     * Text to Speech - Converts text to audio
     *
     * @param text The text to convert
     * @param country The user's country
     * @param languageCode The language code for speech
     * @return Audio bytes
     */
    suspend fun textToSpeech(
        text: String,
        country: SupportedCountry,
        languageCode: String
    ): Result<ByteArray> {
        return if (SupportedCountry.usesSarvamAI(country)) {
            // Sarvam provides natural-sounding TTS for Indian languages
            val sarvamCode = getSarvamLanguageCode(languageCode)
            sarvamApiService.textToSpeech(text, sarvamCode)
        } else {
            // For non-Indian countries, return error as we don't have Google TTS integrated
            // TODO: Integrate Google Text-to-Speech API for African languages
            Result.failure(Exception("Text-to-speech not yet available for this region"))
        }
    }

    /**
     * Get the appropriate default language for a country
     */
    fun getDefaultLanguageForCountry(country: SupportedCountry): String {
        return country.defaultLanguage
    }

    /**
     * Check if voice features (STT/TTS) are available for a country
     */
    fun isVoiceAvailable(country: SupportedCountry): Boolean {
        return if (SupportedCountry.usesSarvamAI(country)) {
            true  // Sarvam supports voice for Indian languages
        } else {
            false  // Voice not yet available for other regions
            // TODO: Enable when Google STT/TTS is integrated
        }
    }

    /**
     * Check if a specific Indian language has voice support
     */
    fun hasIndianLanguageVoiceSupport(language: SupportedLanguage): Boolean {
        return language.hasVoice
    }

    /**
     * Convert standard language code to Sarvam language code
     */
    private fun getSarvamLanguageCode(languageCode: String): String {
        // Sarvam uses format like "hi-IN" for Hindi
        return when (languageCode.lowercase()) {
            "hi" -> "hi-IN"
            "bn" -> "bn-IN"
            "kn" -> "kn-IN"
            "ml" -> "ml-IN"
            "mr" -> "mr-IN"
            "or" -> "or-IN"
            "pa" -> "pa-IN"
            "ta" -> "ta-IN"
            "te" -> "te-IN"
            "gu" -> "gu-IN"
            "en" -> "en-IN"
            else -> "$languageCode-IN"
        }
    }
}

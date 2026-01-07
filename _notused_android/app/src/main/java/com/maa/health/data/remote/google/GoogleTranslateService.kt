package com.maa.health.data.remote.google

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Google Cloud Translation API Service
 *
 * Provides translation capabilities for non-Indian languages
 * used in African and other South Asian countries.
 *
 * Uses Google Cloud Translation API v2
 */
@Singleton
class GoogleTranslateService @Inject constructor(
    private val okHttpClient: OkHttpClient
) {
    companion object {
        private const val BASE_URL = "https://translation.googleapis.com/language/translate/v2"
    }

    /**
     * Translate text from one language to another
     *
     * @param text The text to translate
     * @param targetLanguage The target language code (ISO 639-1, e.g., "en", "sw", "ar")
     * @param sourceLanguage Optional source language (auto-detect if not provided)
     * @return Translated text
     */
    suspend fun translate(
        text: String,
        targetLanguage: String,
        sourceLanguage: String? = null
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val jsonBody = JSONObject().apply {
                put("q", text)
                put("target", targetLanguage)
                sourceLanguage?.let { put("source", it) }
                put("format", "text")
            }

            val requestBody = jsonBody.toString()
                .toRequestBody("application/json".toMediaType())

            val request = Request.Builder()
                .url(BASE_URL)
                .post(requestBody)
                .build()

            val response = okHttpClient.newCall(request).execute()

            if (response.isSuccessful) {
                val responseBody = response.body?.string() ?: ""
                val json = JSONObject(responseBody)
                val translations = json.getJSONObject("data")
                    .getJSONArray("translations")
                    .getJSONObject(0)
                val translatedText = translations.getString("translatedText")
                Result.success(translatedText)
            } else {
                Result.failure(Exception("Translation failed: ${response.code} - ${response.message}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Translate multiple texts at once (batch translation)
     *
     * @param texts List of texts to translate
     * @param targetLanguage The target language code
     * @param sourceLanguage Optional source language
     * @return List of translated texts
     */
    suspend fun translateBatch(
        texts: List<String>,
        targetLanguage: String,
        sourceLanguage: String? = null
    ): Result<List<String>> = withContext(Dispatchers.IO) {
        try {
            val jsonBody = JSONObject().apply {
                put("q", JSONArray(texts))
                put("target", targetLanguage)
                sourceLanguage?.let { put("source", it) }
                put("format", "text")
            }

            val requestBody = jsonBody.toString()
                .toRequestBody("application/json".toMediaType())

            val request = Request.Builder()
                .url(BASE_URL)
                .post(requestBody)
                .build()

            val response = okHttpClient.newCall(request).execute()

            if (response.isSuccessful) {
                val responseBody = response.body?.string() ?: ""
                val json = JSONObject(responseBody)
                val translations = json.getJSONObject("data").getJSONArray("translations")
                val results = mutableListOf<String>()
                for (i in 0 until translations.length()) {
                    results.add(translations.getJSONObject(i).getString("translatedText"))
                }
                Result.success(results)
            } else {
                Result.failure(Exception("Batch translation failed: ${response.code}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Detect the language of a text
     *
     * @param text The text to analyze
     * @return Detected language code
     */
    suspend fun detectLanguage(text: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val jsonBody = JSONObject().apply {
                put("q", text)
            }

            val requestBody = jsonBody.toString()
                .toRequestBody("application/json".toMediaType())

            val request = Request.Builder()
                .url("$BASE_URL/detect")
                .post(requestBody)
                .build()

            val response = okHttpClient.newCall(request).execute()

            if (response.isSuccessful) {
                val responseBody = response.body?.string() ?: ""
                val json = JSONObject(responseBody)
                val detections = json.getJSONObject("data")
                    .getJSONArray("detections")
                    .getJSONArray(0)
                    .getJSONObject(0)
                val language = detections.getString("language")
                Result.success(language)
            } else {
                Result.failure(Exception("Language detection failed: ${response.code}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

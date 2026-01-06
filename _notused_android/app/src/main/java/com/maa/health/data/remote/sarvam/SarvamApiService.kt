package com.maa.health.data.remote.sarvam

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Sarvam AI API Service
 *
 * Provides Speech-to-Text and Text-to-Speech capabilities
 * for 10 Indian languages.
 *
 * API Cost: ₹1/minute
 */
@Singleton
class SarvamApiService @Inject constructor(
    private val okHttpClient: OkHttpClient
) {
    companion object {
        private const val BASE_URL = "https://api.sarvam.ai/v1"
        private const val STT_ENDPOINT = "$BASE_URL/speech-to-text"
        private const val TTS_ENDPOINT = "$BASE_URL/text-to-speech"
    }

    /**
     * Speech-to-Text
     * Converts audio to text in the specified language
     *
     * @param audioFile The audio file to transcribe
     * @param languageCode The Sarvam language code (e.g., "hi-IN")
     * @return Transcribed text
     */
    suspend fun speechToText(
        audioFile: File,
        languageCode: String
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart(
                    "file",
                    "audio.wav",
                    audioFile.asRequestBody("audio/wav".toMediaType())
                )
                .addFormDataPart("language", languageCode)
                .addFormDataPart("model", "sarvam-1")
                .build()

            val request = Request.Builder()
                .url(STT_ENDPOINT)
                .post(requestBody)
                .build()

            val response = okHttpClient.newCall(request).execute()

            if (response.isSuccessful) {
                val responseBody = response.body?.string() ?: ""
                val json = JSONObject(responseBody)
                Result.success(json.getString("transcript"))
            } else {
                Result.failure(Exception("STT failed: ${response.code} - ${response.message}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Text-to-Speech
     * Converts text to audio in the specified language
     *
     * @param text The text to convert
     * @param languageCode The Sarvam language code (e.g., "hi-IN")
     * @param voice The voice to use (default: "meera" - female voice)
     * @return Audio bytes
     */
    suspend fun textToSpeech(
        text: String,
        languageCode: String,
        voice: String = "meera"
    ): Result<ByteArray> = withContext(Dispatchers.IO) {
        try {
            val jsonBody = JSONObject().apply {
                put("text", text)
                put("language", languageCode)
                put("voice", voice)
                put("model", "sarvam-1")
            }

            val requestBody = jsonBody.toString()
                .toRequestBody("application/json".toMediaType())

            val request = Request.Builder()
                .url(TTS_ENDPOINT)
                .post(requestBody)
                .build()

            val response = okHttpClient.newCall(request).execute()

            if (response.isSuccessful) {
                val audioBytes = response.body?.bytes() ?: ByteArray(0)
                Result.success(audioBytes)
            } else {
                Result.failure(Exception("TTS failed: ${response.code} - ${response.message}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

/**
 * Data class for STT response
 */
data class SttResponse(
    val transcript: String,
    val confidence: Float = 0f
)

/**
 * Data class for TTS request
 */
data class TtsRequest(
    val text: String,
    val language: String,
    val voice: String = "meera",
    val model: String = "sarvam-1"
)

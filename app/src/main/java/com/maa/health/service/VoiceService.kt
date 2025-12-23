package com.maa.health.service

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaPlayer
import android.media.MediaRecorder
import androidx.core.content.ContextCompat
import com.maa.health.data.model.SupportedLanguage
import com.maa.health.data.remote.sarvam.SarvamApiService
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Global Voice Service for Maa App
 *
 * Provides voice-first interaction across all screens:
 * - Speech-to-Text (STT) via Sarvam Saarika
 * - Text-to-Speech (TTS) via Sarvam Bulbul v2
 * - Voice confirmation pattern
 * - Audio recording management
 *
 * Voice-First Pattern:
 * 1. User taps mic → recording starts
 * 2. User speaks → audio captured
 * 3. Audio sent to Sarvam STT → text returned
 * 4. Text shown for confirmation → user approves
 * 5. Action executed
 */
@Singleton
class VoiceService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val sarvamApiService: SarvamApiService
) {
    // Voice state
    private val _voiceState = MutableStateFlow(VoiceState.IDLE)
    val voiceState: StateFlow<VoiceState> = _voiceState.asStateFlow()

    // Last transcription result
    private val _transcription = MutableStateFlow<String?>(null)
    val transcription: StateFlow<String?> = _transcription.asStateFlow()

    // Current language
    private val _currentLanguage = MutableStateFlow(SupportedLanguage.HINDI)
    val currentLanguage: StateFlow<SupportedLanguage> = _currentLanguage.asStateFlow()

    // Audio recording
    private var audioRecord: AudioRecord? = null
    private var isRecording = false
    private var recordingFile: File? = null
    private var mediaPlayer: MediaPlayer? = null

    // Audio settings
    private val sampleRate = 16000
    private val channelConfig = AudioFormat.CHANNEL_IN_MONO
    private val audioFormat = AudioFormat.ENCODING_PCM_16BIT
    private val bufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)

    /**
     * Check if microphone permission is granted
     */
    fun hasMicrophonePermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Set current language for voice interactions
     */
    fun setLanguage(language: SupportedLanguage) {
        _currentLanguage.value = language
    }

    /**
     * Start voice recording
     */
    suspend fun startRecording(): Result<Unit> = withContext(Dispatchers.IO) {
        if (!hasMicrophonePermission()) {
            _voiceState.value = VoiceState.ERROR("Microphone permission required")
            return@withContext Result.failure(Exception("Microphone permission not granted"))
        }

        if (!_currentLanguage.value.hasVoice) {
            _voiceState.value = VoiceState.ERROR("Voice not supported for this language")
            return@withContext Result.failure(Exception("Voice not supported for ${_currentLanguage.value.englishName}"))
        }

        try {
            // Create temp file for recording
            recordingFile = File.createTempFile("voice_", ".wav", context.cacheDir)

            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                sampleRate,
                channelConfig,
                audioFormat,
                bufferSize
            )

            if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
                _voiceState.value = VoiceState.ERROR("Failed to initialize recorder")
                return@withContext Result.failure(Exception("AudioRecord initialization failed"))
            }

            _voiceState.value = VoiceState.LISTENING
            isRecording = true
            audioRecord?.startRecording()

            // Record audio in background
            val audioData = mutableListOf<Byte>()
            val buffer = ByteArray(bufferSize)

            while (isRecording) {
                val bytesRead = audioRecord?.read(buffer, 0, bufferSize) ?: 0
                if (bytesRead > 0) {
                    audioData.addAll(buffer.take(bytesRead))
                }
            }

            // Write to WAV file
            recordingFile?.let { file ->
                writeWavFile(file, audioData.toByteArray())
            }

            Result.success(Unit)
        } catch (e: Exception) {
            _voiceState.value = VoiceState.ERROR(e.message ?: "Recording failed")
            Result.failure(e)
        }
    }

    /**
     * Stop recording and transcribe
     */
    suspend fun stopRecordingAndTranscribe(): Result<String> = withContext(Dispatchers.IO) {
        isRecording = false
        audioRecord?.stop()
        audioRecord?.release()
        audioRecord = null

        _voiceState.value = VoiceState.PROCESSING

        val file = recordingFile ?: return@withContext Result.failure(Exception("No recording file"))

        try {
            val result = sarvamApiService.speechToText(
                audioFile = file,
                languageCode = _currentLanguage.value.sarvamCode
            )

            result.fold(
                onSuccess = { transcript ->
                    _transcription.value = transcript
                    _voiceState.value = VoiceState.CONFIRMING(transcript)
                    Result.success(transcript)
                },
                onFailure = { error ->
                    _voiceState.value = VoiceState.ERROR(error.message ?: "Transcription failed")
                    Result.failure(error)
                }
            )
        } finally {
            // Clean up temp file
            file.delete()
            recordingFile = null
        }
    }

    /**
     * Cancel current recording
     */
    fun cancelRecording() {
        isRecording = false
        audioRecord?.stop()
        audioRecord?.release()
        audioRecord = null
        recordingFile?.delete()
        recordingFile = null
        _voiceState.value = VoiceState.IDLE
        _transcription.value = null
    }

    /**
     * Confirm transcription - action can proceed
     */
    fun confirmTranscription() {
        _voiceState.value = VoiceState.CONFIRMED
    }

    /**
     * Reset to idle state
     */
    fun reset() {
        _voiceState.value = VoiceState.IDLE
        _transcription.value = null
    }

    /**
     * Speak text aloud (TTS)
     */
    suspend fun speak(text: String, onComplete: (() -> Unit)? = null): Result<Unit> = withContext(Dispatchers.IO) {
        if (!_currentLanguage.value.hasVoice) {
            return@withContext Result.failure(Exception("Voice not supported for ${_currentLanguage.value.englishName}"))
        }

        try {
            val result = sarvamApiService.textToSpeech(
                text = text,
                languageCode = _currentLanguage.value.sarvamCode
            )

            result.fold(
                onSuccess = { audioBytes ->
                    playAudio(audioBytes, onComplete)
                    Result.success(Unit)
                },
                onFailure = { error ->
                    Result.failure(error)
                }
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Speak greeting in detected language
     */
    suspend fun speakGreeting(language: SupportedLanguage): Result<Unit> {
        val greeting = getGreeting(language)
        setLanguage(language)
        return speak(greeting)
    }

    /**
     * Get localized greeting
     */
    private fun getGreeting(language: SupportedLanguage): String {
        return when (language) {
            SupportedLanguage.HINDI -> "नमस्ते! मैं माँ हूं। आपकी भाषा हिंदी है, क्या यह सही है?"
            SupportedLanguage.BENGALI -> "নমস্কার! আমি মা। আপনার ভাষা বাংলা, এটি কি সঠিক?"
            SupportedLanguage.TAMIL -> "வணக்கம்! நான் மா. உங்கள் மொழி தமிழ், இது சரியா?"
            SupportedLanguage.TELUGU -> "నమస్కారం! నేను మా. మీ భాష తెలుగు, ఇది సరైనదా?"
            SupportedLanguage.KANNADA -> "ನಮಸ್ಕಾರ! ನಾನು ಮಾ. ನಿಮ್ಮ ಭಾಷೆ ಕನ್ನಡ, ಇದು ಸರಿಯೇ?"
            SupportedLanguage.MALAYALAM -> "നമസ്കാരം! ഞാൻ മാ. നിങ്ങളുടെ ഭാഷ മലയാളം, ഇത് ശരിയാണോ?"
            SupportedLanguage.MARATHI -> "नमस्कार! मी माँ आहे. तुमची भाषा मराठी आहे, हे बरोबर आहे का?"
            SupportedLanguage.GUJARATI -> "નમસ્તે! હું માં છું. તમારી ભાષા ગુજરાતી છે, આ સાચું છે?"
            SupportedLanguage.PUNJABI -> "ਸਤ ਸ੍ਰੀ ਅਕਾਲ! ਮੈਂ ਮਾਂ ਹਾਂ। ਤੁਹਾਡੀ ਭਾਸ਼ਾ ਪੰਜਾਬੀ ਹੈ, ਕੀ ਇਹ ਸਹੀ ਹੈ?"
            SupportedLanguage.ODIA -> "ନମସ୍କାର! ମୁଁ ମା। ତୁମର ଭାଷା ଓଡ଼ିଆ, ଏହା ଠିକ୍ କି?"
            SupportedLanguage.ENGLISH -> "Hello! I'm Maa. Your language is English, is that correct?"
            else -> "Hello! I'm Maa, your health companion."
        }
    }

    /**
     * Get voice prompt for specific action
     */
    fun getVoicePrompt(action: VoiceAction): String {
        val lang = _currentLanguage.value
        return when (action) {
            VoiceAction.LOG_SYMPTOM -> when (lang) {
                SupportedLanguage.HINDI -> "आपको क्या तकलीफ है? बताइए..."
                SupportedLanguage.KANNADA -> "ನಿಮಗೆ ಏನು ತೊಂದರೆ ಇದೆ? ಹೇಳಿ..."
                else -> "What symptoms are you experiencing? Tell me..."
            }
            VoiceAction.LOG_MOOD -> when (lang) {
                SupportedLanguage.HINDI -> "आज आप कैसा महसूस कर रहे हैं?"
                SupportedLanguage.KANNADA -> "ಇಂದು ನೀವು ಹೇಗೆ ಅನುಭವಿಸುತ್ತಿದ್ದೀರಿ?"
                else -> "How are you feeling today?"
            }
            VoiceAction.LOG_CYCLE -> when (lang) {
                SupportedLanguage.HINDI -> "आपके पीरियड के बारे में बताइए..."
                SupportedLanguage.KANNADA -> "ನಿಮ್ಮ ಮುಟ್ಟಿನ ಬಗ್ಗೆ ಹೇಳಿ..."
                else -> "Tell me about your period..."
            }
            VoiceAction.SEARCH -> when (lang) {
                SupportedLanguage.HINDI -> "क्या खोजना है?"
                SupportedLanguage.KANNADA -> "ಏನು ಹುಡುಕಬೇಕು?"
                else -> "What would you like to search for?"
            }
            VoiceAction.CONFIRM -> when (lang) {
                SupportedLanguage.HINDI -> "क्या यह सही है?"
                SupportedLanguage.KANNADA -> "ಇದು ಸರಿಯೇ?"
                else -> "Is this correct?"
            }
        }
    }

    private fun playAudio(audioBytes: ByteArray, onComplete: (() -> Unit)?) {
        try {
            val tempFile = File.createTempFile("tts_", ".mp3", context.cacheDir)
            FileOutputStream(tempFile).use { it.write(audioBytes) }

            mediaPlayer?.release()
            mediaPlayer = MediaPlayer().apply {
                setDataSource(tempFile.absolutePath)
                prepare()
                setOnCompletionListener {
                    tempFile.delete()
                    onComplete?.invoke()
                }
                start()
            }
        } catch (e: Exception) {
            onComplete?.invoke()
        }
    }

    private fun writeWavFile(file: File, audioData: ByteArray) {
        FileOutputStream(file).use { output ->
            // WAV header
            val totalDataLen = audioData.size + 36
            val channels = 1
            val byteRate = sampleRate * channels * 2

            output.write("RIFF".toByteArray())
            output.write(intToByteArray(totalDataLen))
            output.write("WAVE".toByteArray())
            output.write("fmt ".toByteArray())
            output.write(intToByteArray(16)) // Subchunk1Size
            output.write(shortToByteArray(1)) // AudioFormat (PCM)
            output.write(shortToByteArray(channels.toShort()))
            output.write(intToByteArray(sampleRate))
            output.write(intToByteArray(byteRate))
            output.write(shortToByteArray((channels * 2).toShort())) // BlockAlign
            output.write(shortToByteArray(16)) // BitsPerSample
            output.write("data".toByteArray())
            output.write(intToByteArray(audioData.size))
            output.write(audioData)
        }
    }

    private fun intToByteArray(value: Int): ByteArray {
        return byteArrayOf(
            (value and 0xff).toByte(),
            ((value shr 8) and 0xff).toByte(),
            ((value shr 16) and 0xff).toByte(),
            ((value shr 24) and 0xff).toByte()
        )
    }

    private fun shortToByteArray(value: Short): ByteArray {
        return byteArrayOf(
            (value.toInt() and 0xff).toByte(),
            ((value.toInt() shr 8) and 0xff).toByte()
        )
    }

    /**
     * Clean up resources
     */
    fun release() {
        cancelRecording()
        mediaPlayer?.release()
        mediaPlayer = null
    }
}

/**
 * Voice interaction states
 */
sealed class VoiceState {
    data object IDLE : VoiceState()
    data object LISTENING : VoiceState()
    data object PROCESSING : VoiceState()
    data class CONFIRMING(val text: String) : VoiceState()
    data object CONFIRMED : VoiceState()
    data class ERROR(val message: String) : VoiceState()
}

/**
 * Voice action types for contextual prompts
 */
enum class VoiceAction {
    LOG_SYMPTOM,
    LOG_MOOD,
    LOG_CYCLE,
    SEARCH,
    CONFIRM
}

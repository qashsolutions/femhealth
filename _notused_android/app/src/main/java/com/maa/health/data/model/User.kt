package com.maa.health.data.model

import java.time.Instant
import java.time.LocalDate

/**
 * User profile domain model
 */
data class User(
    val id: String,
    val phoneNumber: String,
    val language: SupportedLanguage,
    val lifecycleStage: LifecycleStage,
    val dateOfBirth: LocalDate? = null,
    val biometricEnabled: Boolean = false,
    val subscriptionStatus: SubscriptionStatus = SubscriptionStatus.FREE,
    val createdAt: Instant = Instant.now(),
    val lastActiveAt: Instant = Instant.now()
)

/**
 * Lifecycle stages for women across the lifespan
 */
enum class LifecycleStage {
    ADOLESCENCE,      // 13-19 years
    REPRODUCTIVE,     // 18-40 years, not pregnant
    PREGNANCY,        // Currently pregnant
    POSTPARTUM,       // 0-12 months after birth
    CHILD_CARE,       // Has child 0-5 years
    MIDLIFE,          // 40-55 years
    ELDER             // 55+ years
}

/**
 * Subscription status - App is now free for all users
 */
enum class SubscriptionStatus {
    FREE  // All features are now free
}

/**
 * Supported languages via Sarvam AI
 *
 * All 22 scheduled Indian languages are supported for translation.
 * STT (Saarika) and TTS (Bulbul v2) support 11 languages marked with hasVoice = true.
 *
 * Sarvam advantages over Google:
 * - Trained on Indian voices with state/regional variations
 * - Better code-mixing support (Hindi-English, etc.)
 * - 5x cheaper, 2x faster
 * - Optimized for Indian accents and dialects
 */
enum class SupportedLanguage(
    val code: String,
    val nativeName: String,
    val englishName: String,
    val sarvamCode: String,
    val hasVoice: Boolean = false  // STT/TTS support
) {
    // Languages with full STT/TTS support (11 languages)
    HINDI("hi", "हिन्दी", "Hindi", "hi-IN", true),
    BENGALI("bn", "বাংলা", "Bengali", "bn-IN", true),
    KANNADA("kn", "ಕನ್ನಡ", "Kannada", "kn-IN", true),
    MALAYALAM("ml", "മലയാളം", "Malayalam", "ml-IN", true),
    MARATHI("mr", "मराठी", "Marathi", "mr-IN", true),
    ODIA("or", "ଓଡ଼ିଆ", "Odia", "or-IN", true),
    PUNJABI("pa", "ਪੰਜਾਬੀ", "Punjabi", "pa-IN", true),
    TAMIL("ta", "தமிழ்", "Tamil", "ta-IN", true),
    TELUGU("te", "తెలుగు", "Telugu", "te-IN", true),
    GUJARATI("gu", "ગુજરાતી", "Gujarati", "gu-IN", true),
    ENGLISH("en", "English", "English", "en-IN", true),

    // Languages with translation support only (11 more languages)
    ASSAMESE("as", "অসমীয়া", "Assamese", "as-IN", false),
    BODO("brx", "बड़ो", "Bodo", "brx-IN", false),
    DOGRI("doi", "डोगरी", "Dogri", "doi-IN", false),
    KASHMIRI("ks", "कॉशुर", "Kashmiri", "ks-IN", false),
    KONKANI("kok", "कोंकणी", "Konkani", "kok-IN", false),
    MAITHILI("mai", "मैथिली", "Maithili", "mai-IN", false),
    MANIPURI("mni", "মৈতৈলোন্", "Manipuri", "mni-IN", false),
    NEPALI("ne", "नेपाली", "Nepali", "ne-IN", false),
    SANSKRIT("sa", "संस्कृतम्", "Sanskrit", "sa-IN", false),
    SANTALI("sat", "ᱥᱟᱱᱛᱟᱲᱤ", "Santali", "sat-IN", false),
    SINDHI("sd", "سنڌي", "Sindhi", "sd-IN", false),
    URDU("ur", "اردو", "Urdu", "ur-IN", false);

    companion object {
        fun fromCode(code: String): SupportedLanguage {
            return entries.find { it.code == code } ?: HINDI
        }

        /**
         * Get languages with voice support (STT/TTS)
         */
        fun getVoiceLanguages(): List<SupportedLanguage> {
            return entries.filter { it.hasVoice }
        }

        /**
         * Get all languages for translation
         */
        fun getAllLanguages(): List<SupportedLanguage> {
            return entries.toList()
        }

        /**
         * Get languages grouped by voice support
         */
        fun getGroupedLanguages(): Pair<List<SupportedLanguage>, List<SupportedLanguage>> {
            return entries.partition { it.hasVoice }
        }
    }
}

/**
 * Gender enumeration
 */
enum class Gender {
    FEMALE,
    MALE,
    OTHER
}

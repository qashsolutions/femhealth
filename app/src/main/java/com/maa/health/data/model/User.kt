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
 * Subscription status
 */
enum class SubscriptionStatus {
    FREE,
    TRIAL,
    PREMIUM_MONTHLY,
    PREMIUM_YEARLY,
    FAMILY_MONTHLY,
    FAMILY_YEARLY,
    EXPIRED
}

/**
 * Supported languages via Sarvam AI
 */
enum class SupportedLanguage(
    val code: String,
    val nativeName: String,
    val englishName: String,
    val sarvamCode: String
) {
    HINDI("hi", "हिन्दी", "Hindi", "hi-IN"),
    TAMIL("ta", "தமிழ்", "Tamil", "ta-IN"),
    TELUGU("te", "తెలుగు", "Telugu", "te-IN"),
    BENGALI("bn", "বাংলা", "Bengali", "bn-IN"),
    MARATHI("mr", "मराठी", "Marathi", "mr-IN"),
    KANNADA("kn", "ಕನ್ನಡ", "Kannada", "kn-IN"),
    MALAYALAM("ml", "മലയാളം", "Malayalam", "ml-IN"),
    GUJARATI("gu", "ગુજરાતી", "Gujarati", "gu-IN"),
    ODIA("or", "ଓଡ଼ିଆ", "Odia", "or-IN"),
    PUNJABI("pa", "ਪੰਜਾਬੀ", "Punjabi", "pa-IN");

    companion object {
        fun fromCode(code: String): SupportedLanguage {
            return entries.find { it.code == code } ?: HINDI
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

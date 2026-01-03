package com.maa.health.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Tracks all user interactions for the agentic learning system
 * This enables the app to understand user behavior and preferences
 */
@Entity(
    tableName = "user_interactions",
    indices = [
        Index(value = ["userId", "timestamp"]),
        Index(value = ["userId", "interactionType"])
    ]
)
data class UserInteractionEntity(
    @PrimaryKey
    val id: String,
    val userId: String,
    val timestamp: Long,
    val interactionType: String,  // SYMPTOM_LOG, MOOD_LOG, SCREENING, ARTICLE_VIEW, SEARCH, etc.
    val targetId: String?,        // ID of what was interacted with
    val targetType: String?,      // Type of target (article, symptom, screening, etc.)
    val metadata: String?,        // JSON with additional context
    val sessionId: String?,       // For grouping interactions in a session
    val durationMs: Long?         // How long they engaged (for articles, etc.)
)

/**
 * Tracks user feedback on content
 * Used to learn what content is helpful vs not helpful
 */
@Entity(
    tableName = "content_feedback",
    indices = [
        Index(value = ["userId", "contentId"]),
        Index(value = ["userId", "wasHelpful"])
    ]
)
data class ContentFeedbackEntity(
    @PrimaryKey
    val id: String,
    val userId: String,
    val contentId: String,
    val contentType: String,      // ARTICLE, OBSERVATION, RECOMMENDATION, SCREENING_RESULT
    val contentTopic: String?,    // Topic/category of content
    val wasHelpful: Boolean,
    val feedbackText: String?,    // Optional text feedback
    val timestamp: Long
)

/**
 * Stores AI observation history
 * Used to track what observations were made and their outcomes
 */
@Entity(
    tableName = "observation_history",
    indices = [
        Index(value = ["userId", "timestamp"]),
        Index(value = ["userId", "observationType"])
    ]
)
data class ObservationHistoryEntity(
    @PrimaryKey
    val id: String,
    val userId: String,
    val timestamp: Long,
    val observationType: String,  // MOOD_PATTERN, SYMPTOM_PATTERN, CYCLE_PATTERN, GROWTH_PATTERN
    val observation: String,      // The AI-generated observation text
    val dataPointsAnalyzed: Int,
    val timeSpanDays: Int,
    val suggestsFollowUp: Boolean,
    val wasActedUpon: Boolean?,   // Did user take action based on this?
    val userFeedback: String?     // helpful, not_helpful, null
)

/**
 * User preferences learned from interactions
 */
@Entity(
    tableName = "learned_preferences",
    indices = [Index(value = ["userId"])]
)
data class LearnedPreferenceEntity(
    @PrimaryKey
    val id: String,
    val userId: String,
    val preferenceType: String,   // CONTENT_TYPE, NOTIFICATION_TIME, LANGUAGE_STYLE, etc.
    val preferenceKey: String,    // Specific preference
    val preferenceValue: String,  // Value or weight
    val confidence: Float,        // How confident we are (0-1)
    val lastUpdated: Long,
    val dataPoints: Int           // How many interactions informed this
)

/**
 * Types of user interactions we track
 */
object InteractionTypes {
    const val SYMPTOM_LOG = "SYMPTOM_LOG"
    const val MOOD_LOG = "MOOD_LOG"
    const val CYCLE_LOG = "CYCLE_LOG"
    const val SCREENING_COMPLETE = "SCREENING_COMPLETE"
    const val ARTICLE_VIEW = "ARTICLE_VIEW"
    const val ARTICLE_COMPLETE = "ARTICLE_COMPLETE"  // Read full article
    const val SEARCH = "SEARCH"
    const val OBSERVATION_VIEW = "OBSERVATION_VIEW"
    const val RECOMMENDATION_CLICK = "RECOMMENDATION_CLICK"
    const val DANGER_SIGN_CHECK = "DANGER_SIGN_CHECK"
    const val DRUG_LOOKUP = "DRUG_LOOKUP"
    const val FEEDBACK_GIVEN = "FEEDBACK_GIVEN"
    const val NOTIFICATION_INTERACT = "NOTIFICATION_INTERACT"
    const val FEATURE_USED = "FEATURE_USED"
}

/**
 * Types of content for feedback tracking
 */
object ContentTypes {
    const val ARTICLE = "ARTICLE"
    const val OBSERVATION = "OBSERVATION"
    const val RECOMMENDATION = "RECOMMENDATION"
    const val SCREENING_RESULT = "SCREENING_RESULT"
    const val HEALTH_TIP = "HEALTH_TIP"
    const val PATTERN_INSIGHT = "PATTERN_INSIGHT"
}

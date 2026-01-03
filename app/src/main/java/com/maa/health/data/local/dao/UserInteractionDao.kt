package com.maa.health.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.maa.health.data.local.entity.UserInteractionEntity
import com.maa.health.data.local.entity.ContentFeedbackEntity
import com.maa.health.data.local.entity.ObservationHistoryEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO for tracking user interactions
 * Used by the agentic system to learn user preferences and behavior
 */
@Dao
interface UserInteractionDao {

    // User Interactions
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInteraction(interaction: UserInteractionEntity)

    @Query("SELECT * FROM user_interactions WHERE userId = :userId ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecentInteractions(userId: String, limit: Int = 100): List<UserInteractionEntity>

    @Query("SELECT * FROM user_interactions WHERE userId = :userId AND interactionType = :type ORDER BY timestamp DESC")
    suspend fun getInteractionsByType(userId: String, type: String): List<UserInteractionEntity>

    @Query("SELECT interactionType, COUNT(*) as count FROM user_interactions WHERE userId = :userId GROUP BY interactionType ORDER BY count DESC")
    suspend fun getInteractionCounts(userId: String): List<InteractionCount>

    @Query("SELECT * FROM user_interactions WHERE userId = :userId AND timestamp > :since ORDER BY timestamp DESC")
    suspend fun getInteractionsSince(userId: String, since: Long): List<UserInteractionEntity>

    // Content Feedback
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFeedback(feedback: ContentFeedbackEntity)

    @Query("SELECT * FROM content_feedback WHERE userId = :userId AND wasHelpful = 1 ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getHelpfulContent(userId: String, limit: Int = 20): List<ContentFeedbackEntity>

    @Query("SELECT * FROM content_feedback WHERE userId = :userId ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecentFeedback(userId: String, limit: Int = 50): List<ContentFeedbackEntity>

    @Query("SELECT contentType, AVG(CASE WHEN wasHelpful THEN 1.0 ELSE 0.0 END) as helpfulRate FROM content_feedback WHERE userId = :userId GROUP BY contentType")
    suspend fun getContentTypePreferences(userId: String): List<ContentPreference>

    // Observation History
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertObservation(observation: ObservationHistoryEntity)

    @Query("SELECT * FROM observation_history WHERE userId = :userId ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecentObservations(userId: String, limit: Int = 20): List<ObservationHistoryEntity>

    @Query("SELECT * FROM observation_history WHERE userId = :userId AND observationType = :type ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLastObservation(userId: String, type: String): ObservationHistoryEntity?

    @Query("SELECT * FROM observation_history WHERE userId = :userId AND suggestsFollowUp = 1 ORDER BY timestamp DESC")
    suspend fun getObservationsNeedingFollowUp(userId: String): List<ObservationHistoryEntity>

    // Cleanup
    @Query("DELETE FROM user_interactions WHERE timestamp < :before")
    suspend fun deleteOldInteractions(before: Long)

    @Query("DELETE FROM content_feedback WHERE timestamp < :before")
    suspend fun deleteOldFeedback(before: Long)
}

data class InteractionCount(
    val interactionType: String,
    val count: Int
)

data class ContentPreference(
    val contentType: String,
    val helpfulRate: Float
)

package com.maa.health.ui.insights

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maa.health.data.local.entity.ContentTypes
import com.maa.health.data.local.entity.InteractionTypes
import com.maa.health.data.local.entity.ObservationHistoryEntity
import com.maa.health.data.remote.ai.PatternObservation
import com.maa.health.data.remote.ai.PersonalizedObservation
import com.maa.health.data.repository.AgenticObservationRepository
import com.maa.health.data.repository.PersonalizedContent
import com.maa.health.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the Insights/Personalization screen
 *
 * Connects the agentic observation system to the UI:
 * - Shows personalized observations based on user's health data
 * - Displays pattern analysis (mood, symptoms, cycles)
 * - Provides personalized content recommendations from verified sources
 * - Handles user feedback to improve personalization
 */
@HiltViewModel
class InsightsViewModel @Inject constructor(
    private val agenticRepository: AgenticObservationRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    // UI State
    private val _uiState = MutableStateFlow(InsightsUiState())
    val uiState: StateFlow<InsightsUiState> = _uiState.asStateFlow()

    // Current user ID
    private var currentUserId: String? = null

    init {
        loadUserId()
    }

    private fun loadUserId() {
        viewModelScope.launch {
            currentUserId = userRepository.getCurrentUser()?.id
            currentUserId?.let {
                loadInsights(it)
            }
        }
    }

    /**
     * Load all insights for the user
     */
    private fun loadInsights(userId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            // Load personalized observation
            loadPersonalizedObservation(userId)

            // Load pattern analyses
            loadMoodPatterns(userId)
            loadSymptomPatterns(userId)

            // Load personalized content
            loadPersonalizedContent(userId)

            // Load observations needing follow-up
            loadFollowUpObservations(userId)

            _uiState.value = _uiState.value.copy(isLoading = false)
        }
    }

    private suspend fun loadPersonalizedObservation(userId: String) {
        val result = agenticRepository.getPersonalizedObservation(userId)
        result.onSuccess { observation ->
            _uiState.value = _uiState.value.copy(
                personalizedObservation = observation
            )
        }.onFailure {
            // Silently fail - user will just see no observation
        }
    }

    private suspend fun loadMoodPatterns(userId: String) {
        val result = agenticRepository.analyzeMoodPatterns(userId, 14)
        result.onSuccess { observation ->
            _uiState.value = _uiState.value.copy(
                moodPatternObservation = observation
            )
        }
    }

    private suspend fun loadSymptomPatterns(userId: String) {
        val result = agenticRepository.analyzeSymptomPatterns(userId, 30)
        result.onSuccess { observation ->
            _uiState.value = _uiState.value.copy(
                symptomPatternObservation = observation
            )
        }
    }

    private suspend fun loadPersonalizedContent(userId: String) {
        val result = agenticRepository.getPersonalizedContent(userId)
        result.onSuccess { content ->
            _uiState.value = _uiState.value.copy(
                personalizedContent = content
            )
        }
    }

    private suspend fun loadFollowUpObservations(userId: String) {
        val observations = agenticRepository.getObservationsNeedingFollowUp(userId)
        _uiState.value = _uiState.value.copy(
            observationsNeedingFollowUp = observations
        )
    }

    /**
     * Refresh all insights
     */
    fun refresh() {
        currentUserId?.let { loadInsights(it) }
    }

    /**
     * Record that user viewed content
     */
    fun onContentViewed(content: PersonalizedContent, viewDurationMs: Long) {
        currentUserId?.let { userId ->
            viewModelScope.launch {
                agenticRepository.onContentViewed(
                    userId = userId,
                    contentId = content.id,
                    contentType = content.contentType,
                    contentTopic = content.title,
                    viewDurationMs = viewDurationMs
                )
            }
        }
    }

    /**
     * Record feedback on content
     */
    fun onContentFeedback(content: PersonalizedContent, wasHelpful: Boolean) {
        currentUserId?.let { userId ->
            viewModelScope.launch {
                agenticRepository.recordFeedback(
                    userId = userId,
                    contentId = content.id,
                    contentType = ContentTypes.ARTICLE,
                    contentTopic = content.title,
                    wasHelpful = wasHelpful
                )

                // Show feedback confirmation
                _uiState.value = _uiState.value.copy(
                    feedbackMessage = if (wasHelpful) {
                        "Thanks! We'll show you more like this."
                    } else {
                        "Got it. We'll adjust your recommendations."
                    }
                )
            }
        }
    }

    /**
     * Record feedback on an observation
     */
    fun onObservationFeedback(observationType: String, wasHelpful: Boolean) {
        currentUserId?.let { userId ->
            viewModelScope.launch {
                agenticRepository.recordFeedback(
                    userId = userId,
                    contentId = observationType,
                    contentType = ContentTypes.OBSERVATION,
                    contentTopic = observationType,
                    wasHelpful = wasHelpful
                )
            }
        }
    }

    /**
     * Clear feedback message after showing
     */
    fun clearFeedbackMessage() {
        _uiState.value = _uiState.value.copy(feedbackMessage = null)
    }

    /**
     * Stream mood observation for real-time display
     */
    fun streamMoodObservation() = currentUserId?.let { userId ->
        agenticRepository.streamMoodObservation(userId, 14)
    }
}

/**
 * UI State for Insights screen
 */
data class InsightsUiState(
    val isLoading: Boolean = false,
    val personalizedObservation: PersonalizedObservation? = null,
    val moodPatternObservation: PatternObservation? = null,
    val symptomPatternObservation: PatternObservation? = null,
    val personalizedContent: List<PersonalizedContent> = emptyList(),
    val observationsNeedingFollowUp: List<ObservationHistoryEntity> = emptyList(),
    val feedbackMessage: String? = null,
    val error: String? = null
)

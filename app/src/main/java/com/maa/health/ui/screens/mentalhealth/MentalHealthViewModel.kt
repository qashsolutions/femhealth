package com.maa.health.ui.screens.mentalhealth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maa.health.data.model.MoodLog
import com.maa.health.data.model.MoodPattern
import com.maa.health.data.model.ScreeningResult
import com.maa.health.data.model.ScreeningType
import com.maa.health.data.model.Severity
import com.maa.health.data.remote.medgemma.LiteracyLevel
import com.maa.health.data.remote.medgemma.UserContext
import com.maa.health.data.repository.MoodRepository
import com.maa.health.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Mental Health module
 */
@HiltViewModel
class MentalHealthViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val moodRepository: MoodRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MentalHealthUiState())
    val uiState: StateFlow<MentalHealthUiState> = _uiState.asStateFlow()

    init {
        loadMentalHealthData()
    }

    private fun loadMentalHealthData() {
        viewModelScope.launch {
            val user = userRepository.getCurrentUser() ?: return@launch

            // Load today's mood
            val todaysMood = moodRepository.getTodaysMood(user.id)
            _uiState.update { it.copy(todaysMood = todaysMood) }

            // Load mood pattern
            val pattern = moodRepository.getMoodPattern(user.id)
            _uiState.update { it.copy(moodPattern = pattern) }
        }

        viewModelScope.launch {
            val user = userRepository.getCurrentUser() ?: return@launch

            // Load mood history
            moodRepository.getRecentMoods(user.id, 14).collect { moods ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        recentMoods = moods
                    )
                }
            }
        }

        viewModelScope.launch {
            val user = userRepository.getCurrentUser() ?: return@launch

            // Load screening history
            moodRepository.getScreeningHistory(user.id).collect { screenings ->
                _uiState.update { it.copy(screeningHistory = screenings) }
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // MOOD LOGGING
    // ═══════════════════════════════════════════════════════════════════════════

    fun showMoodDialog() {
        _uiState.update { it.copy(showMoodDialog = true) }
    }

    fun dismissMoodDialog() {
        _uiState.update {
            it.copy(
                showMoodDialog = false,
                selectedMoodScore = null,
                selectedEnergyLevel = null,
                selectedSleepQuality = null,
                selectedAnxietyLevel = null,
                moodNotes = ""
            )
        }
    }

    fun setMoodScore(score: Int) {
        _uiState.update { it.copy(selectedMoodScore = score) }
    }

    fun setEnergyLevel(level: Int) {
        _uiState.update { it.copy(selectedEnergyLevel = level) }
    }

    fun setSleepQuality(quality: Int) {
        _uiState.update { it.copy(selectedSleepQuality = quality) }
    }

    fun setAnxietyLevel(level: Int) {
        _uiState.update { it.copy(selectedAnxietyLevel = level) }
    }

    fun setMoodNotes(notes: String) {
        _uiState.update { it.copy(moodNotes = notes) }
    }

    fun saveMoodEntry() {
        val state = _uiState.value
        val score = state.selectedMoodScore ?: return

        viewModelScope.launch {
            val user = userRepository.getCurrentUser() ?: return@launch
            try {
                val mood = moodRepository.logMood(
                    userId = user.id,
                    moodScore = score,
                    energyLevel = state.selectedEnergyLevel,
                    sleepQuality = state.selectedSleepQuality,
                    anxietyLevel = state.selectedAnxietyLevel,
                    notes = state.moodNotes.takeIf { it.isNotBlank() }
                )
                _uiState.update { it.copy(todaysMood = mood) }
                dismissMoodDialog()
                loadMentalHealthData()
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // SCREENINGS
    // ═══════════════════════════════════════════════════════════════════════════

    fun startScreening(type: ScreeningType) {
        val questions = getQuestionsForScreening(type)
        _uiState.update {
            it.copy(
                activeScreeningType = type,
                screeningQuestions = questions,
                screeningResponses = List(questions.size) { 0 },
                currentQuestionIndex = 0,
                showScreeningDialog = true
            )
        }
    }

    fun setScreeningResponse(questionIndex: Int, response: Int) {
        _uiState.update { state ->
            val responses = state.screeningResponses.toMutableList()
            responses[questionIndex] = response
            state.copy(screeningResponses = responses)
        }
    }

    fun nextQuestion() {
        _uiState.update { state ->
            if (state.currentQuestionIndex < state.screeningQuestions.size - 1) {
                state.copy(currentQuestionIndex = state.currentQuestionIndex + 1)
            } else {
                state
            }
        }
    }

    fun previousQuestion() {
        _uiState.update { state ->
            if (state.currentQuestionIndex > 0) {
                state.copy(currentQuestionIndex = state.currentQuestionIndex - 1)
            } else {
                state
            }
        }
    }

    fun submitScreening() {
        val state = _uiState.value
        val type = state.activeScreeningType ?: return

        viewModelScope.launch {
            val user = userRepository.getCurrentUser() ?: return@launch
            _uiState.update { it.copy(isLoading = true) }

            try {
                val stages = userRepository.getLifecycleStages()
                val language = userRepository.getLanguageFlow().toString()

                val userContext = UserContext(
                    lifecycleStage = stages.firstOrNull()?.name ?: "REPRODUCTIVE",
                    age = user.dateOfBirth?.let {
                        java.time.Period.between(it, java.time.LocalDate.now()).years
                    } ?: 30,
                    language = language,
                    literacyLevel = LiteracyLevel.STANDARD,
                    isPregnant = com.maa.health.data.model.LifecycleStage.PREGNANCY in stages
                )

                val result = moodRepository.submitScreening(
                    userId = user.id,
                    screeningType = type,
                    responses = state.screeningResponses,
                    userContext = userContext
                )

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        lastScreeningResult = result,
                        showScreeningDialog = false,
                        showScreeningResult = true
                    )
                }
                loadMentalHealthData()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, error = e.message)
                }
            }
        }
    }

    fun dismissScreening() {
        _uiState.update {
            it.copy(
                showScreeningDialog = false,
                activeScreeningType = null,
                screeningQuestions = emptyList(),
                screeningResponses = emptyList(),
                currentQuestionIndex = 0
            )
        }
    }

    fun dismissScreeningResult() {
        _uiState.update { it.copy(showScreeningResult = false) }
    }

    private fun getQuestionsForScreening(type: ScreeningType): List<ScreeningQuestion> {
        return when (type) {
            ScreeningType.EPDS -> epdsQuestions
            ScreeningType.PHQ9 -> phq9Questions
            ScreeningType.PHQ_A -> phqaQuestions
            ScreeningType.GAD7 -> gad7Questions
            else -> emptyList()
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // COPING STRATEGIES
    // ═══════════════════════════════════════════════════════════════════════════

    fun startBreathingExercise() {
        _uiState.update {
            it.copy(
                showBreathingExercise = true,
                breathingPhase = BreathingPhase.INHALE,
                breathingCycleCount = 0
            )
        }
    }

    fun completeBreathingCycle() {
        _uiState.update {
            it.copy(breathingCycleCount = it.breathingCycleCount + 1)
        }
    }

    fun setBreathingPhase(phase: BreathingPhase) {
        _uiState.update { it.copy(breathingPhase = phase) }
    }

    fun dismissBreathingExercise() {
        _uiState.update { it.copy(showBreathingExercise = false) }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    companion object {
        // EPDS Questions
        private val epdsQuestions = listOf(
            ScreeningQuestion("I have been able to laugh and see the funny side of things",
                listOf("As much as I always could", "Not quite so much now", "Definitely not so much now", "Not at all")),
            ScreeningQuestion("I have looked forward with enjoyment to things",
                listOf("As much as I ever did", "Rather less than I used to", "Definitely less than I used to", "Hardly at all")),
            ScreeningQuestion("I have blamed myself unnecessarily when things went wrong",
                listOf("No, never", "Not very often", "Yes, some of the time", "Yes, most of the time")),
            ScreeningQuestion("I have been anxious or worried for no good reason",
                listOf("No, not at all", "Hardly ever", "Yes, sometimes", "Yes, very often")),
            ScreeningQuestion("I have felt scared or panicky for no very good reason",
                listOf("No, not at all", "No, not much", "Yes, sometimes", "Yes, quite a lot")),
            ScreeningQuestion("Things have been getting on top of me",
                listOf("No, I have been coping as well as ever", "No, most of the time I have coped quite well",
                    "Yes, sometimes I haven't been coping as well as usual", "Yes, most of the time I haven't been able to cope at all")),
            ScreeningQuestion("I have been so unhappy that I have had difficulty sleeping",
                listOf("No, not at all", "Not very often", "Yes, sometimes", "Yes, most of the time")),
            ScreeningQuestion("I have felt sad or miserable",
                listOf("No, not at all", "Not very often", "Yes, quite often", "Yes, most of the time")),
            ScreeningQuestion("I have been so unhappy that I have been crying",
                listOf("No, never", "Only occasionally", "Yes, quite often", "Yes, most of the time")),
            ScreeningQuestion("The thought of harming myself has occurred to me",
                listOf("Never", "Hardly ever", "Sometimes", "Yes, quite often"))
        )

        // PHQ-9 Questions
        private val phq9Questions = listOf(
            ScreeningQuestion("Little interest or pleasure in doing things",
                listOf("Not at all", "Several days", "More than half the days", "Nearly every day")),
            ScreeningQuestion("Feeling down, depressed, or hopeless",
                listOf("Not at all", "Several days", "More than half the days", "Nearly every day")),
            ScreeningQuestion("Trouble falling or staying asleep, or sleeping too much",
                listOf("Not at all", "Several days", "More than half the days", "Nearly every day")),
            ScreeningQuestion("Feeling tired or having little energy",
                listOf("Not at all", "Several days", "More than half the days", "Nearly every day")),
            ScreeningQuestion("Poor appetite or overeating",
                listOf("Not at all", "Several days", "More than half the days", "Nearly every day")),
            ScreeningQuestion("Feeling bad about yourself",
                listOf("Not at all", "Several days", "More than half the days", "Nearly every day")),
            ScreeningQuestion("Trouble concentrating on things",
                listOf("Not at all", "Several days", "More than half the days", "Nearly every day")),
            ScreeningQuestion("Moving or speaking slowly, or being fidgety/restless",
                listOf("Not at all", "Several days", "More than half the days", "Nearly every day")),
            ScreeningQuestion("Thoughts of hurting yourself or that you would be better off dead",
                listOf("Not at all", "Several days", "More than half the days", "Nearly every day"))
        )

        // PHQ-A (Adolescent) - same as PHQ-9
        private val phqaQuestions = phq9Questions

        // GAD-7 Questions
        private val gad7Questions = listOf(
            ScreeningQuestion("Feeling nervous, anxious, or on edge",
                listOf("Not at all", "Several days", "More than half the days", "Nearly every day")),
            ScreeningQuestion("Not being able to stop or control worrying",
                listOf("Not at all", "Several days", "More than half the days", "Nearly every day")),
            ScreeningQuestion("Worrying too much about different things",
                listOf("Not at all", "Several days", "More than half the days", "Nearly every day")),
            ScreeningQuestion("Trouble relaxing",
                listOf("Not at all", "Several days", "More than half the days", "Nearly every day")),
            ScreeningQuestion("Being so restless that it's hard to sit still",
                listOf("Not at all", "Several days", "More than half the days", "Nearly every day")),
            ScreeningQuestion("Becoming easily annoyed or irritable",
                listOf("Not at all", "Several days", "More than half the days", "Nearly every day")),
            ScreeningQuestion("Feeling afraid as if something awful might happen",
                listOf("Not at all", "Several days", "More than half the days", "Nearly every day"))
        )
    }
}

data class MentalHealthUiState(
    val isLoading: Boolean = true,
    val error: String? = null,

    // Mood
    val todaysMood: MoodLog? = null,
    val recentMoods: List<MoodLog> = emptyList(),
    val moodPattern: MoodPattern? = null,

    // Mood dialog
    val showMoodDialog: Boolean = false,
    val selectedMoodScore: Int? = null,
    val selectedEnergyLevel: Int? = null,
    val selectedSleepQuality: Int? = null,
    val selectedAnxietyLevel: Int? = null,
    val moodNotes: String = "",

    // Screenings
    val screeningHistory: List<ScreeningResult> = emptyList(),
    val showScreeningDialog: Boolean = false,
    val activeScreeningType: ScreeningType? = null,
    val screeningQuestions: List<ScreeningQuestion> = emptyList(),
    val screeningResponses: List<Int> = emptyList(),
    val currentQuestionIndex: Int = 0,
    val lastScreeningResult: ScreeningResult? = null,
    val showScreeningResult: Boolean = false,

    // Breathing exercise
    val showBreathingExercise: Boolean = false,
    val breathingPhase: BreathingPhase = BreathingPhase.INHALE,
    val breathingCycleCount: Int = 0
)

data class ScreeningQuestion(
    val question: String,
    val options: List<String>
)

enum class BreathingPhase {
    INHALE,
    HOLD,
    EXHALE
}

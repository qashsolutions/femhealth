package com.maa.health.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maa.health.data.model.BodyRegion
import com.maa.health.data.model.Child
import com.maa.health.data.model.LifecycleStage
import com.maa.health.data.model.MoodLog
import com.maa.health.data.model.Pregnancy
import com.maa.health.data.model.SymptomLog
import com.maa.health.data.model.User
import com.maa.health.data.model.VaccinationRecord
import com.maa.health.data.repository.ChildRepository
import com.maa.health.data.repository.CycleRepository
import com.maa.health.data.repository.FertileWindow
import com.maa.health.data.repository.MoodRepository
import com.maa.health.data.repository.PregnancyRepository
import com.maa.health.data.repository.SymptomRepository
import com.maa.health.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

/**
 * ViewModel for Home screen
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val pregnancyRepository: PregnancyRepository,
    private val childRepository: ChildRepository,
    private val cycleRepository: CycleRepository,
    private val moodRepository: MoodRepository,
    private val symptomRepository: SymptomRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadUserData()
        loadDashboardData()
    }

    private fun loadUserData() {
        viewModelScope.launch {
            val stages = userRepository.getLifecycleStages()
            val user = userRepository.getCurrentUser()

            _uiState.update {
                it.copy(
                    user = user,
                    lifecycleStages = stages,
                    isPregnant = LifecycleStage.PREGNANCY in stages,
                    hasChildren = LifecycleStage.CHILD_CARE in stages
                )
            }

            // Load stage-specific data
            if (LifecycleStage.PREGNANCY in stages) {
                loadPregnancyData(user?.id ?: "")
            }
            if (LifecycleStage.CHILD_CARE in stages) {
                loadChildrenData(user?.id ?: "")
            }
            if (LifecycleStage.REPRODUCTIVE in stages) {
                loadCycleData(user?.id ?: "")
            }
        }
    }

    private fun loadDashboardData() {
        viewModelScope.launch {
            val user = userRepository.getCurrentUser() ?: return@launch

            // Load today's mood
            val todaysMood = moodRepository.getTodaysMood(user.id)
            _uiState.update { it.copy(todaysMood = todaysMood) }

            // Load recent symptoms
            symptomRepository.getSymptomHistory(user.id, 7).collect { symptoms ->
                _uiState.update { it.copy(recentSymptoms = symptoms.take(3)) }
            }
        }
    }

    private fun loadPregnancyData(userId: String) {
        viewModelScope.launch {
            pregnancyRepository.getActivePregnancy(userId).collect { pregnancy ->
                if (pregnancy != null) {
                    val weekInfo = pregnancyRepository.getWeekInfo(pregnancy.gestationalWeek)
                    val daysUntilDue = pregnancy.eddDate?.let {
                        pregnancyRepository.getDaysUntilDue(it)
                    } ?: 0

                    _uiState.update {
                        it.copy(
                            activePregnancy = pregnancy,
                            gestationalWeek = pregnancy.gestationalWeek,
                            daysUntilDue = daysUntilDue.toInt(),
                            babySize = weekInfo.babySize
                        )
                    }
                }
            }
        }
    }

    private fun loadChildrenData(userId: String) {
        viewModelScope.launch {
            childRepository.getChildren(userId).collect { children ->
                _uiState.update { it.copy(children = children) }

                // Load upcoming vaccinations for first child
                children.firstOrNull()?.let { child ->
                    childRepository.getUpcomingVaccinations(child.id, 30).collect { vaccines ->
                        _uiState.update { it.copy(upcomingVaccinations = vaccines) }
                    }
                }
            }
        }
    }

    private fun loadCycleData(userId: String) {
        viewModelScope.launch {
            val pattern = cycleRepository.getCyclePattern(userId)
            _uiState.update {
                it.copy(
                    nextPeriodDate = pattern.predictedNextPeriod,
                    cycleDay = calculateCycleDay(pattern.predictedNextPeriod, pattern.averageCycleLength)
                )
            }

            cycleRepository.getFertileWindow(userId).collect { window ->
                _uiState.update { it.copy(fertileWindow = window) }
            }
        }
    }

    private fun calculateCycleDay(nextPeriod: LocalDate?, cycleLength: Int): Int? {
        if (nextPeriod == null) return null
        val daysUntilNext = java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), nextPeriod).toInt()
        return cycleLength - daysUntilNext
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // BODY MAP INTERACTIONS
    // ═══════════════════════════════════════════════════════════════════════════

    fun selectBodyRegion(region: BodyRegion) {
        _uiState.update { it.copy(selectedBodyRegion = region) }
    }

    fun clearBodyRegion() {
        _uiState.update { it.copy(selectedBodyRegion = null) }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // QUICK ACTIONS
    // ═══════════════════════════════════════════════════════════════════════════

    fun logQuickMood(score: Int) {
        viewModelScope.launch {
            val user = userRepository.getCurrentUser() ?: return@launch
            val moodLog = moodRepository.logMood(user.id, score)
            _uiState.update { it.copy(todaysMood = moodLog) }
        }
    }

    fun refreshData() {
        _uiState.update { it.copy(isRefreshing = true) }
        loadUserData()
        loadDashboardData()
        _uiState.update { it.copy(isRefreshing = false) }
    }
}

data class HomeUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val user: User? = null,
    val lifecycleStages: Set<LifecycleStage> = emptySet(),

    // Body map
    val selectedBodyRegion: BodyRegion? = null,

    // Pregnancy
    val isPregnant: Boolean = false,
    val activePregnancy: Pregnancy? = null,
    val gestationalWeek: Int = 0,
    val daysUntilDue: Int = 0,
    val babySize: String = "",

    // Children
    val hasChildren: Boolean = false,
    val children: List<Child> = emptyList(),
    val upcomingVaccinations: List<VaccinationRecord> = emptyList(),

    // Cycle
    val nextPeriodDate: LocalDate? = null,
    val cycleDay: Int? = null,
    val fertileWindow: FertileWindow? = null,

    // Mood
    val todaysMood: MoodLog? = null,

    // Recent activity
    val recentSymptoms: List<SymptomLog> = emptyList()
)

package com.maa.health.ui.screens.cycle

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maa.health.data.model.CycleLog
import com.maa.health.data.model.CyclePattern
import com.maa.health.data.model.CycleSymptom
import com.maa.health.data.model.FlowIntensity
import com.maa.health.data.repository.CycleRepository
import com.maa.health.data.repository.FertileWindow
import com.maa.health.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

/**
 * ViewModel for Cycle Tracking module
 */
@HiltViewModel
class CycleViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val cycleRepository: CycleRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CycleUiState())
    val uiState: StateFlow<CycleUiState> = _uiState.asStateFlow()

    init {
        loadCycleData()
    }

    private fun loadCycleData() {
        viewModelScope.launch {
            val user = userRepository.getCurrentUser() ?: return@launch
            val userId = user.id

            // Load cycle pattern
            val pattern = cycleRepository.getCyclePattern(userId)
            _uiState.update {
                it.copy(
                    cyclePattern = pattern,
                    averageCycleLength = pattern.averageCycleLength,
                    nextPeriodDate = pattern.predictedNextPeriod,
                    ovulationDate = pattern.predictedOvulation
                )
            }

            // Load active cycle
            cycleRepository.getActiveCycle(userId).collect { activeCycle ->
                val cycleDay = activeCycle?.let { cycle ->
                    java.time.temporal.ChronoUnit.DAYS.between(cycle.startDate, LocalDate.now()).toInt() + 1
                }
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        activeCycle = activeCycle,
                        isPeriodActive = activeCycle?.endDate == null,
                        currentCycleDay = cycleDay
                    )
                }
            }
        }

        viewModelScope.launch {
            val user = userRepository.getCurrentUser() ?: return@launch

            // Load cycle history
            cycleRepository.getCycleHistory(user.id).collect { history ->
                _uiState.update { it.copy(cycleHistory = history) }
            }
        }

        viewModelScope.launch {
            val user = userRepository.getCurrentUser() ?: return@launch

            // Load fertile window
            cycleRepository.getFertileWindow(user.id).collect { window ->
                _uiState.update { it.copy(fertileWindow = window) }
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // PERIOD LOGGING
    // ═══════════════════════════════════════════════════════════════════════════

    fun startPeriod() {
        viewModelScope.launch {
            val user = userRepository.getCurrentUser() ?: return@launch
            try {
                cycleRepository.startPeriod(user.id)
                _uiState.update { it.copy(isPeriodActive = true) }
                loadCycleData()
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun startPeriodOnDate(date: LocalDate) {
        viewModelScope.launch {
            val user = userRepository.getCurrentUser() ?: return@launch
            try {
                cycleRepository.startPeriod(user.id, date)
                loadCycleData()
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun endPeriod() {
        viewModelScope.launch {
            val user = userRepository.getCurrentUser() ?: return@launch
            try {
                cycleRepository.endPeriod(user.id)
                _uiState.update { it.copy(isPeriodActive = false) }
                loadCycleData()
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun logFlowDay(date: LocalDate, intensity: FlowIntensity, symptoms: List<CycleSymptom> = emptyList()) {
        viewModelScope.launch {
            val user = userRepository.getCurrentUser() ?: return@launch
            try {
                cycleRepository.logFlowDay(user.id, date, intensity, symptoms)
                loadCycleData()
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // FLOW LOGGING DIALOG
    // ═══════════════════════════════════════════════════════════════════════════

    fun showFlowDialog(date: LocalDate = LocalDate.now()) {
        _uiState.update {
            it.copy(
                showFlowDialog = true,
                selectedDate = date,
                selectedFlowIntensity = FlowIntensity.MEDIUM,
                selectedSymptoms = emptySet()
            )
        }
    }

    fun dismissFlowDialog() {
        _uiState.update { it.copy(showFlowDialog = false) }
    }

    fun setFlowIntensity(intensity: FlowIntensity) {
        _uiState.update { it.copy(selectedFlowIntensity = intensity) }
    }

    fun toggleSymptom(symptom: CycleSymptom) {
        _uiState.update { state ->
            val current = state.selectedSymptoms
            val updated = if (symptom in current) current - symptom else current + symptom
            state.copy(selectedSymptoms = updated)
        }
    }

    fun saveFlowEntry() {
        val state = _uiState.value
        val date = state.selectedDate ?: return
        val intensity = state.selectedFlowIntensity ?: return

        logFlowDay(date, intensity, state.selectedSymptoms.toList())
        dismissFlowDialog()
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // CALENDAR
    // ═══════════════════════════════════════════════════════════════════════════

    fun setSelectedMonth(yearMonth: YearMonth) {
        _uiState.update { it.copy(selectedMonth = yearMonth) }
    }

    fun selectDate(date: LocalDate) {
        _uiState.update { it.copy(selectedDate = date) }
    }

    fun goToPreviousMonth() {
        _uiState.update { it.copy(selectedMonth = it.selectedMonth.minusMonths(1)) }
    }

    fun goToNextMonth() {
        _uiState.update { it.copy(selectedMonth = it.selectedMonth.plusMonths(1)) }
    }

    fun goToToday() {
        _uiState.update {
            it.copy(
                selectedMonth = YearMonth.now(),
                selectedDate = LocalDate.now()
            )
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // INSIGHTS
    // ═══════════════════════════════════════════════════════════════════════════

    fun getCyclePhase(day: Int, cycleLength: Int): CyclePhase {
        return when {
            day <= 5 -> CyclePhase.MENSTRUAL
            day <= 13 -> CyclePhase.FOLLICULAR
            day <= 16 -> CyclePhase.OVULATION
            else -> CyclePhase.LUTEAL
        }
    }

    fun getDaysUntilPeriod(): Int? {
        val nextPeriod = _uiState.value.nextPeriodDate ?: return null
        return java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), nextPeriod).toInt()
    }

    fun isInFertileWindow(): Boolean {
        val window = _uiState.value.fertileWindow ?: return false
        val today = LocalDate.now()
        return today >= window.startDate && today <= window.endDate
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

data class CycleUiState(
    val isLoading: Boolean = true,
    val error: String? = null,

    // Current cycle
    val activeCycle: CycleLog? = null,
    val isPeriodActive: Boolean = false,
    val currentCycleDay: Int? = null,

    // Predictions
    val cyclePattern: CyclePattern? = null,
    val averageCycleLength: Int = 28,
    val nextPeriodDate: LocalDate? = null,
    val ovulationDate: LocalDate? = null,
    val fertileWindow: FertileWindow? = null,

    // History
    val cycleHistory: List<CycleLog> = emptyList(),

    // Calendar
    val selectedMonth: YearMonth = YearMonth.now(),
    val selectedDate: LocalDate? = null,

    // Flow dialog
    val showFlowDialog: Boolean = false,
    val selectedFlowIntensity: FlowIntensity? = null,
    val selectedSymptoms: Set<CycleSymptom> = emptySet()
)

enum class CyclePhase(val displayName: String) {
    MENSTRUAL("Menstrual"),
    FOLLICULAR("Follicular"),
    OVULATION("Ovulation"),
    LUTEAL("Luteal")
}

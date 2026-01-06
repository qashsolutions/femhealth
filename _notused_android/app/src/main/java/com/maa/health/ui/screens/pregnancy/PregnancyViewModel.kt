package com.maa.health.ui.screens.pregnancy

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maa.health.data.model.Pregnancy
import com.maa.health.data.model.PregnancyOutcome
import com.maa.health.data.model.RiskFactor
import com.maa.health.data.model.SymptomType
import com.maa.health.data.repository.PregnancyRepository
import com.maa.health.data.repository.SymptomRepository
import com.maa.health.data.repository.UserRepository
import com.maa.health.data.repository.WeekInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import javax.inject.Inject

/**
 * ViewModel for Pregnancy module
 */
@HiltViewModel
class PregnancyViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val pregnancyRepository: PregnancyRepository,
    private val symptomRepository: SymptomRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PregnancyUiState())
    val uiState: StateFlow<PregnancyUiState> = _uiState.asStateFlow()

    init {
        loadPregnancyData()
    }

    private fun loadPregnancyData() {
        viewModelScope.launch {
            val user = userRepository.getCurrentUser() ?: return@launch

            pregnancyRepository.getActivePregnancy(user.id).collect { pregnancy ->
                if (pregnancy != null) {
                    val weekInfo = pregnancyRepository.getWeekInfo(pregnancy.gestationalWeek)
                    val trimester = pregnancyRepository.getTrimester(pregnancy.gestationalWeek)
                    val daysUntilDue = pregnancy.eddDate?.let {
                        pregnancyRepository.getDaysUntilDue(it)
                    } ?: 0

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            hasActivePregnancy = true,
                            pregnancy = pregnancy,
                            gestationalWeek = pregnancy.gestationalWeek,
                            gestationalDay = pregnancy.lmpDate?.let { lmp ->
                                pregnancyRepository.calculateGestationalDay(lmp)
                            } ?: 0,
                            trimester = trimester,
                            daysUntilDue = daysUntilDue.toInt(),
                            weekInfo = weekInfo
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(isLoading = false, hasActivePregnancy = false)
                    }
                }
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // PREGNANCY SETUP
    // ═══════════════════════════════════════════════════════════════════════════

    fun setLmpDate(date: LocalDate) {
        _uiState.update { it.copy(setupLmpDate = date) }
    }

    fun createPregnancy() {
        val lmpDate = _uiState.value.setupLmpDate ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val user = userRepository.getCurrentUser() ?: return@launch
                val pregnancy = pregnancyRepository.createPregnancy(user.id, lmpDate)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        hasActivePregnancy = true,
                        pregnancy = pregnancy,
                        showSetupDialog = false
                    )
                }
                loadPregnancyData()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, error = e.message)
                }
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // KICK COUNTER
    // ═══════════════════════════════════════════════════════════════════════════

    fun startKickCounter() {
        _uiState.update {
            it.copy(
                kickCounterActive = true,
                kickCount = 0,
                kickCounterStartTime = System.currentTimeMillis()
            )
        }
    }

    fun recordKick() {
        _uiState.update {
            it.copy(kickCount = it.kickCount + 1)
        }

        // Check if target reached (10 kicks)
        if (_uiState.value.kickCount >= 10) {
            completeKickCounter()
        }
    }

    fun completeKickCounter() {
        val state = _uiState.value
        val duration = System.currentTimeMillis() - (state.kickCounterStartTime ?: 0)
        val durationMinutes = duration / 60000

        _uiState.update {
            it.copy(
                kickCounterActive = false,
                lastKickSessionDuration = durationMinutes.toInt(),
                lastKickSessionCount = it.kickCount
            )
        }

        // TODO: Save kick session to database
    }

    fun resetKickCounter() {
        _uiState.update {
            it.copy(
                kickCounterActive = false,
                kickCount = 0,
                kickCounterStartTime = null
            )
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // CONTRACTION TIMER
    // ═══════════════════════════════════════════════════════════════════════════

    fun startContraction() {
        val now = System.currentTimeMillis()
        val previousStart = _uiState.value.lastContractionStart

        val interval = if (previousStart != null) {
            ((now - previousStart) / 1000).toInt()
        } else null

        _uiState.update {
            it.copy(
                contractionActive = true,
                contractionStart = now,
                lastContractionInterval = interval
            )
        }
    }

    fun endContraction() {
        val state = _uiState.value
        val start = state.contractionStart ?: return
        val duration = ((System.currentTimeMillis() - start) / 1000).toInt()

        val newContraction = ContractionRecord(
            startTime = start,
            durationSeconds = duration,
            intervalSeconds = state.lastContractionInterval
        )

        _uiState.update {
            it.copy(
                contractionActive = false,
                lastContractionStart = start,
                lastContractionDuration = duration,
                contractionHistory = it.contractionHistory + newContraction
            )
        }
    }

    fun clearContractionHistory() {
        _uiState.update {
            it.copy(
                contractionHistory = emptyList(),
                lastContractionStart = null,
                lastContractionDuration = null,
                lastContractionInterval = null
            )
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // DANGER SIGNS
    // ═══════════════════════════════════════════════════════════════════════════

    fun checkDangerSign(symptom: SymptomType) {
        viewModelScope.launch {
            val result = symptomRepository.checkDangerSigns(
                symptoms = listOf(symptom),
                isPregnant = true,
                childAgeMonths = null
            )

            _uiState.update {
                it.copy(
                    dangerSignDetected = result.hasDangerSigns,
                    dangerSignMessage = result.immediateAction
                )
            }
        }
    }

    fun dismissDangerSignAlert() {
        _uiState.update {
            it.copy(dangerSignDetected = false, dangerSignMessage = null)
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // WEEK NAVIGATION
    // ═══════════════════════════════════════════════════════════════════════════

    fun viewWeek(week: Int) {
        val weekInfo = pregnancyRepository.getWeekInfo(week)
        _uiState.update {
            it.copy(
                selectedWeek = week,
                selectedWeekInfo = weekInfo
            )
        }
    }

    fun showSetupDialog() {
        _uiState.update { it.copy(showSetupDialog = true) }
    }

    fun dismissSetupDialog() {
        _uiState.update { it.copy(showSetupDialog = false) }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

data class PregnancyUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val hasActivePregnancy: Boolean = false,
    val pregnancy: Pregnancy? = null,
    val gestationalWeek: Int = 0,
    val gestationalDay: Int = 0,
    val trimester: Int = 1,
    val daysUntilDue: Int = 0,
    val weekInfo: WeekInfo? = null,

    // Setup
    val showSetupDialog: Boolean = false,
    val setupLmpDate: LocalDate? = null,

    // Week navigation
    val selectedWeek: Int? = null,
    val selectedWeekInfo: WeekInfo? = null,

    // Kick counter
    val kickCounterActive: Boolean = false,
    val kickCount: Int = 0,
    val kickCounterStartTime: Long? = null,
    val lastKickSessionDuration: Int? = null,
    val lastKickSessionCount: Int? = null,

    // Contraction timer
    val contractionActive: Boolean = false,
    val contractionStart: Long? = null,
    val lastContractionStart: Long? = null,
    val lastContractionDuration: Int? = null,
    val lastContractionInterval: Int? = null,
    val contractionHistory: List<ContractionRecord> = emptyList(),

    // Danger signs
    val dangerSignDetected: Boolean = false,
    val dangerSignMessage: String? = null
)

data class ContractionRecord(
    val startTime: Long,
    val durationSeconds: Int,
    val intervalSeconds: Int?
)

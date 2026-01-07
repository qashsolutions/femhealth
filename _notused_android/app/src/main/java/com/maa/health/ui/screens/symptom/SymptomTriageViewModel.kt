package com.maa.health.ui.screens.symptom

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maa.health.data.model.BodyRegion
import com.maa.health.data.model.LifecycleStage
import com.maa.health.data.model.Severity
import com.maa.health.data.model.SymptomLog
import com.maa.health.data.model.SymptomType
import com.maa.health.data.model.TriageResult
import com.maa.health.data.remote.medgemma.TriageContext
import com.maa.health.data.repository.PregnancyRepository
import com.maa.health.data.repository.SymptomRepository
import com.maa.health.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Duration
import javax.inject.Inject

/**
 * ViewModel for Symptom logging and Triage
 */
@HiltViewModel
class SymptomTriageViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val symptomRepository: SymptomRepository,
    private val pregnancyRepository: PregnancyRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SymptomTriageUiState())
    val uiState: StateFlow<SymptomTriageUiState> = _uiState.asStateFlow()

    init {
        loadContext()
    }

    private fun loadContext() {
        viewModelScope.launch {
            val stages = userRepository.getLifecycleStages()
            val isPregnant = LifecycleStage.PREGNANCY in stages
            val hasChildren = LifecycleStage.CHILD_CARE in stages

            var gestationalWeek: Int? = null
            if (isPregnant) {
                val user = userRepository.getCurrentUser()
                if (user != null) {
                    val pregnancy = pregnancyRepository.getActivePregnancy(user.id).first()
                    gestationalWeek = pregnancy?.gestationalWeek
                }
            }

            _uiState.update {
                it.copy(
                    isPregnant = isPregnant,
                    hasChildren = hasChildren,
                    gestationalWeek = gestationalWeek
                )
            }
        }

        viewModelScope.launch {
            val user = userRepository.getCurrentUser() ?: return@launch
            symptomRepository.getSymptomHistory(user.id, 7).collect { symptoms ->
                _uiState.update { it.copy(recentSymptoms = symptoms) }
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // BODY REGION SELECTION
    // ═══════════════════════════════════════════════════════════════════════════

    fun selectBodyRegion(region: BodyRegion) {
        val state = _uiState.value
        val suggestedSymptoms = symptomRepository.getSymptomsForRegion(
            region = region,
            isPregnant = state.isPregnant,
            isChild = state.isForChild
        )

        _uiState.update {
            it.copy(
                selectedRegion = region,
                suggestedSymptoms = suggestedSymptoms,
                selectedSymptoms = emptySet(),
                currentStep = TriageStep.SELECT_SYMPTOMS
            )
        }
    }

    fun clearBodyRegion() {
        _uiState.update {
            it.copy(
                selectedRegion = null,
                currentStep = TriageStep.SELECT_REGION
            )
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // SYMPTOM SELECTION
    // ═══════════════════════════════════════════════════════════════════════════

    fun toggleSymptom(symptom: SymptomType) {
        _uiState.update { state ->
            val current = state.selectedSymptoms
            val updated = if (symptom in current) current - symptom else current + symptom
            state.copy(selectedSymptoms = updated)
        }

        // Check for danger signs in real-time
        checkDangerSigns()
    }

    private fun checkDangerSigns() {
        val state = _uiState.value
        if (state.selectedSymptoms.isEmpty()) return

        viewModelScope.launch {
            val result = symptomRepository.checkDangerSigns(
                symptoms = state.selectedSymptoms.toList(),
                isPregnant = state.isPregnant,
                childAgeMonths = state.childAgeMonths
            )

            _uiState.update {
                it.copy(
                    hasDangerSigns = result.hasDangerSigns,
                    dangerSignsDetected = result.dangerSigns,
                    immediateAction = result.immediateAction
                )
            }
        }
    }

    fun proceedToSeverity() {
        if (_uiState.value.selectedSymptoms.isEmpty()) return
        _uiState.update { it.copy(currentStep = TriageStep.SELECT_SEVERITY) }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // SEVERITY SELECTION
    // ═══════════════════════════════════════════════════════════════════════════

    fun selectSeverity(severity: Severity) {
        _uiState.update { it.copy(selectedSeverity = severity) }
    }

    fun setDuration(hours: Int) {
        _uiState.update { it.copy(symptomDurationHours = hours) }
    }

    fun proceedToTriage() {
        if (_uiState.value.selectedSeverity == null) return
        performTriage()
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TRIAGE
    // ═══════════════════════════════════════════════════════════════════════════

    private fun performTriage() {
        val state = _uiState.value
        val region = state.selectedRegion ?: return
        val severity = state.selectedSeverity ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, currentStep = TriageStep.PROCESSING) }

            try {
                val user = userRepository.getCurrentUser() ?: return@launch

                val context = TriageContext(
                    isPregnant = state.isPregnant,
                    gestationalWeek = state.gestationalWeek,
                    isPostpartum = false,
                    postpartumWeeks = null,
                    childAgeMonths = state.childAgeMonths
                )

                val (symptomLog, triageResult) = symptomRepository.logSymptomWithTriage(
                    userId = user.id,
                    bodyRegion = region,
                    symptoms = state.selectedSymptoms.toList(),
                    severity = severity,
                    context = context,
                    childId = state.selectedChildId,
                    pregnancyId = null,
                    duration = state.symptomDurationHours?.let { Duration.ofHours(it.toLong()) }
                )

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        currentStep = TriageStep.RESULT,
                        triageResult = triageResult,
                        savedSymptomLog = symptomLog
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message
                    )
                }
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // CHILD MODE
    // ═══════════════════════════════════════════════════════════════════════════

    fun setForChild(childId: String, ageMonths: Int) {
        _uiState.update {
            it.copy(
                isForChild = true,
                selectedChildId = childId,
                childAgeMonths = ageMonths
            )
        }
    }

    fun clearChildMode() {
        _uiState.update {
            it.copy(
                isForChild = false,
                selectedChildId = null,
                childAgeMonths = null
            )
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // NAVIGATION
    // ═══════════════════════════════════════════════════════════════════════════

    fun goBack() {
        _uiState.update { state ->
            when (state.currentStep) {
                TriageStep.SELECT_SYMPTOMS -> state.copy(
                    currentStep = TriageStep.SELECT_REGION,
                    selectedRegion = null
                )
                TriageStep.SELECT_SEVERITY -> state.copy(
                    currentStep = TriageStep.SELECT_SYMPTOMS
                )
                TriageStep.RESULT -> state.copy(
                    currentStep = TriageStep.SELECT_REGION,
                    selectedRegion = null,
                    selectedSymptoms = emptySet(),
                    selectedSeverity = null,
                    triageResult = null
                )
                else -> state
            }
        }
    }

    fun reset() {
        _uiState.update {
            SymptomTriageUiState(
                isPregnant = it.isPregnant,
                hasChildren = it.hasChildren,
                gestationalWeek = it.gestationalWeek,
                recentSymptoms = it.recentSymptoms
            )
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

data class SymptomTriageUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val currentStep: TriageStep = TriageStep.SELECT_REGION,

    // Context
    val isPregnant: Boolean = false,
    val hasChildren: Boolean = false,
    val gestationalWeek: Int? = null,
    val isForChild: Boolean = false,
    val selectedChildId: String? = null,
    val childAgeMonths: Int? = null,

    // Selection
    val selectedRegion: BodyRegion? = null,
    val suggestedSymptoms: List<SymptomType> = emptyList(),
    val selectedSymptoms: Set<SymptomType> = emptySet(),
    val selectedSeverity: Severity? = null,
    val symptomDurationHours: Int? = null,

    // Danger signs
    val hasDangerSigns: Boolean = false,
    val dangerSignsDetected: List<String> = emptyList(),
    val immediateAction: String? = null,

    // Result
    val triageResult: TriageResult? = null,
    val savedSymptomLog: SymptomLog? = null,

    // History
    val recentSymptoms: List<SymptomLog> = emptyList()
)

enum class TriageStep {
    SELECT_REGION,
    SELECT_SYMPTOMS,
    SELECT_SEVERITY,
    PROCESSING,
    RESULT
}

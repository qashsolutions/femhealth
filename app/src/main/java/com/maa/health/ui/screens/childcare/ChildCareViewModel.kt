package com.maa.health.ui.screens.childcare

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maa.health.data.model.Child
import com.maa.health.data.model.Gender
import com.maa.health.data.model.GrowthMeasurement
import com.maa.health.data.model.Milestone
import com.maa.health.data.model.SymptomLog
import com.maa.health.data.model.VaccinationRecord
import com.maa.health.data.repository.ChildRepository
import com.maa.health.data.repository.SymptomRepository
import com.maa.health.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

/**
 * ViewModel for Child Care module
 */
@HiltViewModel
class ChildCareViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val childRepository: ChildRepository,
    private val symptomRepository: SymptomRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChildCareUiState())
    val uiState: StateFlow<ChildCareUiState> = _uiState.asStateFlow()

    init {
        loadChildren()
    }

    private fun loadChildren() {
        viewModelScope.launch {
            val user = userRepository.getCurrentUser() ?: return@launch

            childRepository.getChildren(user.id).collect { children ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        children = children,
                        selectedChild = children.firstOrNull() ?: it.selectedChild
                    )
                }

                // Load data for first child
                children.firstOrNull()?.let { child ->
                    loadChildData(child.id)
                }
            }
        }
    }

    fun selectChild(child: Child) {
        _uiState.update { it.copy(selectedChild = child) }
        loadChildData(child.id)
    }

    private fun loadChildData(childId: String) {
        viewModelScope.launch {
            // Load vaccinations
            childRepository.getVaccinationSchedule(childId).collect { vaccines ->
                val pending = vaccines.filter { it.givenDate == null && it.dueDate <= LocalDate.now() }
                val upcoming = vaccines.filter { it.givenDate == null && it.dueDate > LocalDate.now() }
                val completed = vaccines.filter { it.givenDate != null }

                _uiState.update {
                    it.copy(
                        allVaccinations = vaccines,
                        pendingVaccinations = pending,
                        upcomingVaccinations = upcoming,
                        completedVaccinations = completed
                    )
                }
            }
        }

        viewModelScope.launch {
            // Load growth history
            childRepository.getGrowthHistory(childId).collect { measurements ->
                _uiState.update { it.copy(growthHistory = measurements) }
            }
        }

        viewModelScope.launch {
            // Load symptoms
            symptomRepository.getChildSymptoms(childId).collect { symptoms ->
                _uiState.update { it.copy(childSymptoms = symptoms) }
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // ADD CHILD
    // ═══════════════════════════════════════════════════════════════════════════

    fun showAddChildDialog() {
        _uiState.update { it.copy(showAddChildDialog = true) }
    }

    fun dismissAddChildDialog() {
        _uiState.update {
            it.copy(
                showAddChildDialog = false,
                newChildName = "",
                newChildDob = null,
                newChildGender = null,
                newChildBirthWeight = "",
                newChildBirthHeight = ""
            )
        }
    }

    fun setNewChildName(name: String) {
        _uiState.update { it.copy(newChildName = name) }
    }

    fun setNewChildDob(date: LocalDate) {
        _uiState.update { it.copy(newChildDob = date) }
    }

    fun setNewChildGender(gender: Gender) {
        _uiState.update { it.copy(newChildGender = gender) }
    }

    fun setNewChildBirthWeight(weight: String) {
        _uiState.update { it.copy(newChildBirthWeight = weight) }
    }

    fun setNewChildBirthHeight(height: String) {
        _uiState.update { it.copy(newChildBirthHeight = height) }
    }

    fun addChild() {
        val state = _uiState.value
        val dob = state.newChildDob ?: return
        val gender = state.newChildGender ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val user = userRepository.getCurrentUser() ?: return@launch
                val child = childRepository.addChild(
                    userId = user.id,
                    name = state.newChildName.takeIf { it.isNotBlank() },
                    dateOfBirth = dob,
                    gender = gender,
                    birthWeight = state.newChildBirthWeight.toFloatOrNull(),
                    birthHeight = state.newChildBirthHeight.toFloatOrNull()
                )
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        selectedChild = child,
                        showAddChildDialog = false
                    )
                }
                loadChildren()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, error = e.message)
                }
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // VACCINATION
    // ═══════════════════════════════════════════════════════════════════════════

    fun selectVaccination(record: VaccinationRecord) {
        _uiState.update { it.copy(selectedVaccination = record) }
    }

    fun recordVaccination(
        recordId: String,
        givenDate: LocalDate,
        batch: String?,
        facility: String?
    ) {
        viewModelScope.launch {
            try {
                childRepository.recordVaccination(recordId, givenDate, batch, facility)
                _uiState.update { it.copy(selectedVaccination = null) }
                // Reload vaccination data
                _uiState.value.selectedChild?.let { loadChildData(it.id) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // GROWTH TRACKING
    // ═══════════════════════════════════════════════════════════════════════════

    fun showGrowthEntryDialog() {
        _uiState.update { it.copy(showGrowthEntryDialog = true) }
    }

    fun dismissGrowthEntryDialog() {
        _uiState.update {
            it.copy(
                showGrowthEntryDialog = false,
                newWeight = "",
                newHeight = "",
                newHeadCircumference = ""
            )
        }
    }

    fun setNewWeight(weight: String) {
        _uiState.update { it.copy(newWeight = weight) }
    }

    fun setNewHeight(height: String) {
        _uiState.update { it.copy(newHeight = height) }
    }

    fun setNewHeadCircumference(circumference: String) {
        _uiState.update { it.copy(newHeadCircumference = circumference) }
    }

    fun saveGrowthMeasurement() {
        val state = _uiState.value
        val childId = state.selectedChild?.id ?: return
        val weight = state.newWeight.toFloatOrNull() ?: return
        val height = state.newHeight.toFloatOrNull() ?: return

        viewModelScope.launch {
            try {
                childRepository.addGrowthMeasurement(
                    childId = childId,
                    weight = weight,
                    height = height,
                    headCircumference = state.newHeadCircumference.toFloatOrNull()
                )
                dismissGrowthEntryDialog()
                loadChildData(childId)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // SYMPTOM CHECK
    // ═══════════════════════════════════════════════════════════════════════════

    fun checkChildDangerSigns(symptoms: List<com.maa.health.data.model.SymptomType>) {
        val child = _uiState.value.selectedChild ?: return

        viewModelScope.launch {
            val result = symptomRepository.checkDangerSigns(
                symptoms = symptoms,
                isPregnant = false,
                childAgeMonths = child.ageMonths
            )

            _uiState.update {
                it.copy(
                    dangerSignDetected = result.hasDangerSigns,
                    dangerSignMessage = result.immediateAction,
                    dangerSigns = result.dangerSigns
                )
            }
        }
    }

    fun dismissDangerSignAlert() {
        _uiState.update {
            it.copy(
                dangerSignDetected = false,
                dangerSignMessage = null,
                dangerSigns = emptyList()
            )
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

data class ChildCareUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val children: List<Child> = emptyList(),
    val selectedChild: Child? = null,

    // Add child dialog
    val showAddChildDialog: Boolean = false,
    val newChildName: String = "",
    val newChildDob: LocalDate? = null,
    val newChildGender: Gender? = null,
    val newChildBirthWeight: String = "",
    val newChildBirthHeight: String = "",

    // Vaccinations
    val allVaccinations: List<VaccinationRecord> = emptyList(),
    val pendingVaccinations: List<VaccinationRecord> = emptyList(),
    val upcomingVaccinations: List<VaccinationRecord> = emptyList(),
    val completedVaccinations: List<VaccinationRecord> = emptyList(),
    val selectedVaccination: VaccinationRecord? = null,

    // Growth
    val growthHistory: List<GrowthMeasurement> = emptyList(),
    val showGrowthEntryDialog: Boolean = false,
    val newWeight: String = "",
    val newHeight: String = "",
    val newHeadCircumference: String = "",

    // Milestones
    val milestones: List<Milestone> = emptyList(),

    // Symptoms
    val childSymptoms: List<SymptomLog> = emptyList(),
    val dangerSignDetected: Boolean = false,
    val dangerSignMessage: String? = null,
    val dangerSigns: List<String> = emptyList()
)

package com.maa.health.ui.screens.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maa.health.data.model.LifecycleStage
import com.maa.health.data.model.SupportedCountry
import com.maa.health.data.model.SupportedLanguage
import com.maa.health.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for onboarding flow
 */
@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    init {
        checkOnboardingStatus()
    }

    private fun checkOnboardingStatus() {
        viewModelScope.launch {
            val isComplete = userRepository.isOnboardingComplete()
            _uiState.update { it.copy(isOnboardingComplete = isComplete) }
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // LANGUAGE SELECTION
    // ═══════════════════════════════════════════════════════════════════════════

    fun selectLanguage(language: SupportedLanguage) {
        _uiState.update { it.copy(selectedLanguage = language) }
    }

    fun confirmLanguage() {
        val language = _uiState.value.selectedLanguage ?: return
        viewModelScope.launch {
            userRepository.setLanguage(language)
            _uiState.update { it.copy(languageConfirmed = true) }
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // PHONE AUTH
    // ═══════════════════════════════════════════════════════════════════════════

    fun setPhoneNumber(phone: String) {
        _uiState.update { it.copy(phoneNumber = phone) }
    }

    fun setSelectedCountry(country: SupportedCountry) {
        _uiState.update { it.copy(selectedCountry = country) }
    }

    fun sendOtp() {
        val phone = _uiState.value.phoneNumber
        // Flexible validation: minimum 6 digits for international numbers
        if (phone.length < 6) {
            _uiState.update { it.copy(phoneError = "Please enter a valid phone number") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, phoneError = null) }
            try {
                // TODO: Implement Firebase phone auth
                // For now, simulate success
                kotlinx.coroutines.delay(1000)
                _uiState.update { it.copy(isLoading = false, otpSent = true) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, phoneError = e.message ?: "Failed to send OTP")
                }
            }
        }
    }

    fun setOtp(otp: String) {
        _uiState.update { it.copy(otp = otp) }
    }

    fun verifyOtp() {
        val otp = _uiState.value.otp
        if (otp.length != 6) {
            _uiState.update { it.copy(otpError = "Please enter 6-digit OTP") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, otpError = null) }
            try {
                // TODO: Verify OTP with Firebase
                kotlinx.coroutines.delay(1000)
                _uiState.update { it.copy(isLoading = false, otpVerified = true) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, otpError = e.message ?: "Invalid OTP")
                }
            }
        }
    }

    fun resendOtp() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                // TODO: Resend OTP
                kotlinx.coroutines.delay(1000)
                _uiState.update { it.copy(isLoading = false, otpResent = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // PROFILE SETUP
    // ═══════════════════════════════════════════════════════════════════════════

    fun setProfileName(name: String) {
        _uiState.update { it.copy(profileName = name) }
    }

    fun setProfileAge(age: String) {
        _uiState.update { it.copy(profileAge = age) }
    }

    fun setProfilePincode(pincode: String) {
        _uiState.update { it.copy(profilePincode = pincode) }
    }

    fun saveProfile() {
        val age = _uiState.value.profileAge.toIntOrNull()
        if (age == null || age < 10 || age > 100) {
            _uiState.update { it.copy(profileError = "Please enter a valid age") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, profileError = null) }
            try {
                val country = _uiState.value.selectedCountry
                val fullPhoneNumber = "${country.dialCode}${_uiState.value.phoneNumber}"
                userRepository.createUser(
                    phoneNumber = fullPhoneNumber,
                    name = _uiState.value.profileName.takeIf { it.isNotBlank() },
                    age = age,
                    pincode = _uiState.value.profilePincode.takeIf { it.isNotBlank() }
                )
                _uiState.update { it.copy(isLoading = false, profileSaved = true) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, profileError = e.message ?: "Failed to save profile")
                }
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // LIFECYCLE STAGE SELECTION
    // ═══════════════════════════════════════════════════════════════════════════

    fun toggleLifecycleStage(stage: LifecycleStage) {
        _uiState.update { state ->
            val currentStages = state.selectedLifecycleStages
            val newStages = if (stage in currentStages) {
                currentStages - stage
            } else {
                currentStages + stage
            }
            state.copy(selectedLifecycleStages = newStages)
        }
    }

    fun saveLifecycleStages() {
        val stages = _uiState.value.selectedLifecycleStages
        if (stages.isEmpty()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                userRepository.setLifecycleStages(stages)
                userRepository.setOnboardingComplete()
                _uiState.update {
                    it.copy(isLoading = false, onboardingComplete = true, isOnboardingComplete = true)
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // SKIP (for testing)
    // ═══════════════════════════════════════════════════════════════════════════

    fun skipAuth() {
        viewModelScope.launch {
            _uiState.update { it.copy(otpVerified = true) }
        }
    }
}

data class OnboardingUiState(
    val isLoading: Boolean = false,
    val isOnboardingComplete: Boolean = false,

    // Language
    val selectedLanguage: SupportedLanguage? = null,
    val languageConfirmed: Boolean = false,

    // Phone Auth
    val phoneNumber: String = "",
    val selectedCountry: SupportedCountry = SupportedCountry.getDefault(),
    val phoneError: String? = null,
    val otpSent: Boolean = false,
    val otp: String = "",
    val otpError: String? = null,
    val otpVerified: Boolean = false,
    val otpResent: Boolean = false,

    // Profile
    val profileName: String = "",
    val profileAge: String = "",
    val profilePincode: String = "",
    val profileError: String? = null,
    val profileSaved: Boolean = false,

    // Lifecycle Stages
    val selectedLifecycleStages: Set<LifecycleStage> = emptySet(),
    val onboardingComplete: Boolean = false
)

package com.maa.health.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maa.health.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Settings screen
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            val user = userRepository.getCurrentUser()
            val language = userRepository.getSelectedLanguage()
            val stages = userRepository.getLifecycleStages()

            _uiState.update {
                it.copy(
                    userName = user?.name,
                    userPhone = user?.phoneNumber,
                    currentLanguage = language.displayName,
                    lifecycleStages = stages.map { stage -> stage.name.lowercase().replaceFirstChar { c -> c.uppercase() } },
                    appVersion = "1.0.0"
                )
            }
        }

        // Load preferences
        viewModelScope.launch {
            userRepository.userPreferences.collect { prefs ->
                _uiState.update {
                    it.copy(
                        notificationsEnabled = prefs.notificationsEnabled,
                        offlineModeEnabled = prefs.offlineContentEnabled,
                        biometricEnabled = prefs.biometricEnabled,
                        pinEnabled = prefs.pinEnabled
                    )
                }
            }
        }
    }

    fun toggleOfflineMode() {
        viewModelScope.launch {
            val current = _uiState.value.offlineModeEnabled
            userRepository.setOfflineMode(!current)
            _uiState.update { it.copy(offlineModeEnabled = !current) }
        }
    }

    fun toggleBiometric() {
        viewModelScope.launch {
            val current = _uiState.value.biometricEnabled
            userRepository.setBiometricEnabled(!current)
            _uiState.update { it.copy(biometricEnabled = !current) }
        }
    }

    fun togglePin() {
        viewModelScope.launch {
            val current = _uiState.value.pinEnabled
            userRepository.setPinEnabled(!current)
            _uiState.update { it.copy(pinEnabled = !current) }
        }
    }

    fun rateApp() {
        // Would open app store for rating
    }

    fun deleteAccount() {
        viewModelScope.launch {
            userRepository.deleteAllUserData()
        }
    }

    fun logout() {
        viewModelScope.launch {
            userRepository.logout()
        }
    }
}

data class SettingsUiState(
    val userName: String? = null,
    val userPhone: String? = null,
    val currentLanguage: String = "English",
    val lifecycleStages: List<String> = emptyList(),
    val notificationsEnabled: Boolean = true,
    val offlineModeEnabled: Boolean = false,
    val biometricEnabled: Boolean = false,
    val pinEnabled: Boolean = false,
    val appVersion: String = "1.0.0"
)

package com.openclaw.companions.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.openclaw.companions.data.repository.SettingsRepository
import com.openclaw.companions.domain.model.AppSettings
import com.openclaw.companions.domain.model.DarkMode
import com.openclaw.companions.domain.model.GatewayConfig
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val settings: StateFlow<AppSettings> = settingsRepository.settings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), AppSettings())

    val gatewayConfig: StateFlow<GatewayConfig?> = settingsRepository.gatewayConfig
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)

    fun updateDarkMode(mode: DarkMode) {
        viewModelScope.launch {
            settingsRepository.updateDarkMode(mode)
        }
    }

    fun updateWakeWord(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.updateWakeWord(enabled)
        }
    }

    fun updateNotifications(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.updateNotifications(enabled)
        }
    }

    fun updateBiometric(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.updateBiometric(enabled)
        }
    }

    fun updateSoundEffects(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.updateSoundEffects(enabled)
        }
    }

    fun updateHaptic(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.updateHaptic(enabled)
        }
    }

    fun saveGatewayConfig(config: GatewayConfig) {
        viewModelScope.launch {
            settingsRepository.saveGatewayConfig(config)
        }
    }
}

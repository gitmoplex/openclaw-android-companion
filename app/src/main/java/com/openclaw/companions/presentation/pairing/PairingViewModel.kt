package com.openclaw.companions.presentation.pairing

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.openclaw.companions.data.remote.WebSocketService
import com.openclaw.companions.domain.model.GatewayConfig
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import javax.inject.Inject

@HiltViewModel
class PairingViewModel @Inject constructor(
    private val webSocketService: WebSocketService
) : ViewModel() {

    private val _uiState = MutableStateFlow(PairingUiState())
    val uiState: StateFlow<PairingUiState> = _uiState.asStateFlow()

    fun pairWithGateway(qrCode: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                // Parse QR code payload
                val config = parseQrCode(qrCode)
                _uiState.value = _uiState.value.copy(
                    config = config,
                    isLoading = false
                )
                // Initiate connection
                webSocketService.connect(config)
            } catch (e: Exception) {
                Log.e("Pairing", "Failed to parse QR: ${e.message}")
                _uiState.value = _uiState.value.copy(
                    error = e.message,
                    isLoading = false
                )
            }
        }
    }

    private fun parseQrCode(qrCode: String): GatewayConfig {
        // QR code format: ws://host:port?token=xxx or JSON
        return try {
            if (qrCode.startsWith("{")) {
                Json.decodeFromString(GatewayConfig.serializer(), qrCode)
            } else {
                // Parse URL format
                val regex = Regex("^(wss?://)?([^:]+):?(\\d+)?")
                val match = regex.find(qrCode)
                GatewayConfig(
                    host = match?.groupValues?.get(2) ?: "192.168.1.126",
                    port = match?.groupValues?.get(3)?.toIntOrNull() ?: 18789,
                    useSsl = qrCode.startsWith("wss://")
                )
            }
        } catch (e: Exception) {
            GatewayConfig(host = "192.168.1.126", port = 18789)
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

data class PairingUiState(
    val isLoading: Boolean = false,
    val config: GatewayConfig? = null,
    val error: String? = null
)

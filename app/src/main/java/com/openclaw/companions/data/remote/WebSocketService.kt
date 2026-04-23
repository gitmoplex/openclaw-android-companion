package com.openclaw.companions.data.remote

import android.util.Log
import com.openclaw.companions.domain.model.GatewayConfig
import com.openclaw.companions.domain.model.Message
import io.ktor.client.*
import io.ktor.client.plugins.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WebSocketService @Inject constructor(
    private val client: HttpClient
) {
    private var webSocketSession: DefaultClientWebSocketSession? = null
    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    private val _messages = MutableSharedFlow<Message>()
    val messages: SharedFlow<Message> = _messages.asSharedFlow()

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var reconnectJob: Job? = null

    private val json = Json { ignoreUnknownKeys = true }

    fun connect(config: GatewayConfig) {
        scope.launch {
            _connectionState.value = ConnectionState.Connecting
            try {
                val protocol = if (config.useSsl) "wss" else "ws"
                val url = "$protocol://${config.host}:${config.port}/ws"

                client.webSocket(urlString = url) {
                    webSocketSession = this
                    _connectionState.value = ConnectionState.Connected

                    // Send auth if token provided
                    config.authToken?.let { token ->
                        send(Frame.Text("{\"type\":\"auth\",\"token\":\"$token\"}"))
                    }

                    // Listen for messages
                    for (frame in incoming) {
                        when (frame) {
                            is Frame.Text -> handleMessage(frame.readText())
                            is Frame.Close -> {
                                _connectionState.value = ConnectionState.Disconnected
                                scheduleReconnect(config)
                            }
                            else -> {}
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("WebSocket", "Connection failed: ${e.message}")
                _connectionState.value = ConnectionState.Error(e.message ?: "Unknown error")
                scheduleReconnect(config)
            }
        }
    }

    suspend fun sendMessage(message: Message) {
        val payload = json.encodeToString(message)
        webSocketSession?.send(Frame.Text(payload))
            ?: throw IllegalStateException("Not connected")
    }

    suspend fun sendVoiceData(audioData: ByteArray) {
        webSocketSession?.send(Frame.Binary(true, audioData))
            ?: throw IllegalStateException("Not connected")
    }

    fun disconnect() {
        reconnectJob?.cancel()
        scope.launch {
            webSocketSession?.close()
            webSocketSession = null
            _connectionState.value = ConnectionState.Disconnected
        }
    }

    private fun handleMessage(text: String) {
        try {
            val message = json.decodeFromString<Message>(text)
            scope.launch {
                _messages.emit(message)
            }
        } catch (e: Exception) {
            Log.e("WebSocket", "Failed to parse message: ${e.message}")
        }
    }

    private fun scheduleReconnect(config: GatewayConfig) {
        reconnectJob?.cancel()
        reconnectJob = scope.launch {
            delay(5000)
            if (_connectionState.value != ConnectionState.Connected) {
                connect(config)
            }
        }
    }

    sealed class ConnectionState {
        object Disconnected : ConnectionState()
        object Connecting : ConnectionState()
        object Connected : ConnectionState()
        data class Error(val message: String) : ConnectionState()
    }
}

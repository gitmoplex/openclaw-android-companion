package com.openclaw.companions.presentation.chat

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.openclaw.companions.data.local.dao.MessageDao
import com.openclaw.companions.data.local.entity.MessageEntity
import com.openclaw.companions.data.remote.WebSocketService
import com.openclaw.companions.domain.model.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val webSocketService: WebSocketService,
    private val messageDao: MessageDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    val connectionState = webSocketService.connectionState
    val messages = messageDao.getMessages()
        .map { entities ->
            entities.map { it.toMessage() }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    init {
        collectMessages()
    }

    fun connect(config: GatewayConfig) {
        webSocketService.connect(config)
    }

    fun sendMessage(content: String) {
        val message = Message(
            id = UUID.randomUUID().toString(),
            content = content,
            sender = SenderType.USER
        )

        viewModelScope.launch {
            // Save locally
            messageDao.insertMessage(message.toEntity())
            // Send over WebSocket
            try {
                webSocketService.sendMessage(message)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun sendImage(uri: Uri) {
        // TODO: Implement image upload
        val message = Message(
            id = UUID.randomUUID().toString(),
            content = "",
            sender = SenderType.USER,
            attachments = listOf(
                Attachment(
                    id = UUID.randomUUID().toString(),
                    type = AttachmentType.IMAGE,
                    url = uri.toString(),
                    mimeType = "image/jpeg"
                )
            )
        )
        viewModelScope.launch {
            messageDao.insertMessage(message.toEntity())
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    private fun collectMessages() {
        viewModelScope.launch {
            webSocketService.messages.collect { message ->
                messageDao.insertMessage(message.toEntity())
            }
        }
    }

    private fun Message.toEntity() = MessageEntity(
        id = id,
        content = content,
        sender = sender.name,
        timestamp = timestamp,
        attachmentsJson = attachments.takeIf { it.isNotEmpty() }?.toString(),
        isPending = isPending
    )

    private fun MessageEntity.toMessage() = Message(
        id = id,
        content = content,
        sender = SenderType.valueOf(sender),
        timestamp = timestamp,
        isPending = isPending
    )

    override fun onCleared() {
        super.onCleared()
        webSocketService.disconnect()
    }
}

data class ChatUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isRecording: Boolean = false
)

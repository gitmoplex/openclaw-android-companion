package com.openclaw.companions.data.repository

import com.openclaw.companions.data.local.dao.MessageDao
import com.openclaw.companions.data.local.entity.MessageEntity
import com.openclaw.companions.data.remote.WebSocketService
import com.openclaw.companions.domain.model.Message
import com.openclaw.companions.domain.model.SenderType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRepository @Inject constructor(
    private val messageDao: MessageDao,
    private val webSocketService: WebSocketService
) {
    fun getMessages(): Flow<List<Message>> {
        return messageDao.getMessages().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    suspend fun sendMessage(content: String) {
        val message = Message(
            id = UUID.randomUUID().toString(),
            content = content,
            sender = SenderType.USER,
            isPending = true
        )
        messageDao.insertMessage(message.toEntity())
        webSocketService.sendMessage(message)
    }

    suspend fun insertMessage(message: Message) {
        messageDao.insertMessage(message.toEntity())
    }

    suspend fun clearHistory() {
        messageDao.clearConversation()
    }

    private fun MessageEntity.toDomainModel() = Message(
        id = id,
        content = content,
        sender = SenderType.valueOf(sender),
        timestamp = timestamp,
        isPending = isPending
    )

    private fun Message.toEntity() = MessageEntity(
        id = id,
        content = content,
        sender = sender.name,
        timestamp = timestamp,
        isPending = isPending
    )
}

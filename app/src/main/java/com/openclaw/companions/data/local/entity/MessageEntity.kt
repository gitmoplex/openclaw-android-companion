package com.openclaw.companions.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey
    val id: String,
    val content: String,
    val sender: String, // "USER", "ASSISTANT", "SYSTEM"
    val timestamp: Long,
    val attachmentsJson: String? = null,
    val isPending: Boolean = false,
    val conversationId: String = "default"
)

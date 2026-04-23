package com.openclaw.companions.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Message(
    val id: String,
    val content: String,
    val sender: SenderType,
    val timestamp: Long = System.currentTimeMillis(),
    val attachments: List<Attachment> = emptyList(),
    val isPending: Boolean = false
)

enum class SenderType {
    USER,
    ASSISTANT,
    SYSTEM
}

@Serializable
data class Attachment(
    val id: String,
    val type: AttachmentType,
    val url: String,
    val mimeType: String
)

enum class AttachmentType {
    IMAGE,
    AUDIO,
    DOCUMENT
}

@Serializable
data class GatewayConfig(
    val host: String,
    val port: Int = 18789,
    val useSsl: Boolean = false,
    val authToken: String? = null,
    val tailscaleMode: Boolean = false
)

@Serializable
data class WakeWordConfig(
    val enabled: Boolean = false,
    val sensitivity: Float = 0.7f,
    val autoListen: Boolean = true
)

@Serializable
data class AppSettings(
    val darkMode: DarkMode = DarkMode.SYSTEM,
    val biometricEnabled: Boolean = false,
    val pushNotifications: Boolean = true,
    val soundEffects: Boolean = true,
    val hapticFeedback: Boolean = true
)

enum class DarkMode {
    LIGHT,
    DARK,
    SYSTEM
}

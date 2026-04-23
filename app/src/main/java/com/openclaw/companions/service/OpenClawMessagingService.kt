package com.openclaw.companions.service

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class OpenClawMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        // TODO: Show notification and/or send to WebSocket
        message.notification?.let {
            showNotification(it.title ?: "OpenClaw", it.body ?: "")
        }

        message.data.let { data ->
            // Handle data payload
            if (data.containsKey("type")) {
                when (data["type"]) {
                    "chat_message" -> {
                        // Forward to chat UI
                    }
                    "alert" -> {
                        // Show high priority notification
                    }
                }
            }
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // TODO: Send token to OpenClaw gateway for push notifications
    }

    private fun showNotification(title: String, body: String) {
        // TODO: Implement notification display
    }
}

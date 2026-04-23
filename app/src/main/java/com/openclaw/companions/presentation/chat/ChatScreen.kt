package com.openclaw.companions.presentation.chat

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.openclaw.companions.domain.model.SenderType
import com.openclaw.companions.presentation.components.VoiceInputButton
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    viewModel: ChatViewModel = hiltViewModel(),
    onNavigateToSettings: () -> Unit = {},
    onNavigateToPairing: () -> Unit = {}
) {
    val messages by viewModel.messages.collectAsState()
    val connectionState by viewModel.connectionState.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    var messageText by remember { mutableStateOf("") }
    var showConnectionMenu by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("OpenClaw")
                        Text(
                            when (connectionState) {
                                is com.openclaw.companions.data.remote.WebSocketService.ConnectionState.Connected -> "Connected"
                                is com.openclaw.companions.data.remote.WebSocketService.ConnectionState.Connecting -> "Connecting..."
                                is com.openclaw.companions.data.remote.WebSocketService.ConnectionState.Error -> "Error"
                                else -> "Disconnected"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = when (connectionState) {
                                is com.openclaw.companions.data.remote.WebSocketService.ConnectionState.Connected ->
                                    MaterialTheme.colorScheme.tertiary
                                is com.openclaw.companions.data.remote.WebSocketService.ConnectionState.Connecting ->
                                    MaterialTheme.colorScheme.secondary
                                else -> MaterialTheme.colorScheme.error
                            }
                        )
                    }
                },
                actions = {
                    ConnectionStatusIndicator(connectionState)
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, "Settings")
                    }
                    IconButton(onClick = { showConnectionMenu = true }) {
                        Icon(Icons.Default.MoreVert, "Menu")
                    }
                    DropdownMenu(
                        expanded = showConnectionMenu,
                        onDismissRequest = { showConnectionMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Pair with Gateway") },
                            onClick = {
                                onNavigateToPairing()
                                showConnectionMenu = false
                            },
                            leadingIcon = { Icon(Icons.Default.QrCodeScanner, null) }
                        )
                        DropdownMenuItem(
                            text = { Text("Clear Chat") },
                            onClick = {
                                viewModel.clearChat()
                                showConnectionMenu = false
                            },
                            leadingIcon = { Icon(Icons.Default.Delete, null) }
                        )
                    }
                }
            )
        },
        bottomBar = {
            ChatInputBar(
                value = messageText,
                onValueChange = { messageText = it },
                onSend = {
                    if (messageText.isNotBlank()) {
                        viewModel.sendMessage(messageText)
                        messageText = ""
                    }
                },
                isRecording = uiState.isRecording,
                onStartRecording = { viewModel.startVoiceRecording() },
                onStopRecording = { viewModel.stopVoiceRecording() }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (messages.isEmpty()) {
                EmptyChatPlaceholder(
                    isConnected = connectionState is
                        com.openclaw.companions.data.remote.WebSocketService.ConnectionState.Connected
                )
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(messages, key = { it.id }) { message ->
                        MessageBubble(
                            message = message,
                            isUser = message.sender == SenderType.USER
                        )
                    }
                }

                LaunchedEffect(messages.size) {
                    scope.launch {
                        listState.animateScrollToItem(messages.size - 1)
                    }
                }
            }
        }
    }

    // Error snackbar
    uiState.error?.let { error ->
        LaunchedEffect(error) {
            // Show error and clear it
            viewModel.clearError()
        }
    }
}

@Composable
fun MessageBubble(
    message: com.openclaw.companions.domain.model.Message,
    isUser: Boolean
) {
    val backgroundColor = if (isUser) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.secondaryContainer
    }

    val alignment = if (isUser) Alignment.CenterEnd else Alignment.CenterStart

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        Surface(
            color = backgroundColor,
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.widthIn(max = 300.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = message.content,
                    style = MaterialTheme.typography.bodyMedium
                )
                if (message.isPending) {
                    Text(
                        text = "Sending...",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }
    }
}

@Composable
fun ChatInputBar(
    value: String,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit,
    isRecording: Boolean,
    onStartRecording: () -> Unit,
    onStopRecording: () -> Unit
) {
    Surface(
        tonalElevation = 3.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.weight(1f),
                placeholder = { Text("Message OpenClaw...") },
                singleLine = true,
                trailingIcon = {
                    VoiceInputButton(
                        isRecording = isRecording,
                        onStartRecording = onStartRecording,
                        onStopRecording = { data ->
                            onStopRecording()
                            // TODO: Send voice data
                        },
                        modifier = Modifier.padding(end = 4.dp)
                    )
                }
            )

            FilledIconButton(
                onClick = onSend,
                enabled = value.isNotBlank(),
                modifier = Modifier.size(48.dp)
            ) {
                Icon(Icons.Default.Send, "Send")
            }
        }
    }
}

@Composable
fun ConnectionStatusIndicator(
    state: com.openclaw.companions.data.remote.WebSocketService.ConnectionState
) {
    val color = when (state) {
        is com.openclaw.companions.data.remote.WebSocketService.ConnectionState.Connected ->
            MaterialTheme.colorScheme.tertiary
        is com.openclaw.companions.data.remote.WebSocketService.ConnectionState.Connecting ->
            MaterialTheme.colorScheme.secondary
        else -> MaterialTheme.colorScheme.error
    }

    Box(
        modifier = Modifier.padding(horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            color = color,
            shape = MaterialTheme.shapes.small,
            modifier = Modifier.size(12.dp)
        ) {}
    }
}

@Composable
fun EmptyChatPlaceholder(isConnected: Boolean) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                Icons.Default.Chat,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = if (isConnected) {
                    "Say 'Hey OpenClaw' or type a message"
                } else {
                    "Not connected to OpenClaw\nTap the menu to pair"
                },
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

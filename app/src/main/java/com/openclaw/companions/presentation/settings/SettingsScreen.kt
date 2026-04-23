package com.openclaw.companions.presentation.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit
) {
    var darkMode by remember { mutableStateOf(false) }
    var wakeWord by remember { mutableStateOf(false) }
    var notifications by remember { mutableStateOf(true) }
    var biometric by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Connection Settings
            SettingsSection(title = "Connection") {
                SettingsItem(
                    title = "Gateway",
                    subtitle = "192.168.1.126:18789",
                    onClick = { /* TODO */ }
                )
                SettingsItem(
                    title = "Pairing",
                    subtitle = "Scan QR code",
                    onClick = { /* TODO */ }
                )
            }

            // Features
            SettingsSection(title = "Features") {
                SettingsSwitch(
                    title = "Wake Word",
                    subtitle = "'Hey OpenClaw' to activate",
                    checked = wakeWord,
                    onCheckedChange = { wakeWord = it }
                )
                SettingsSwitch(
                    title = "Push Notifications",
                    subtitle = "Receive alerts from OpenClaw",
                    checked = notifications,
                    onCheckedChange = { notifications = it }
                )
            }

            // Appearance
            SettingsSection(title = "Appearance") {
                SettingsSwitch(
                    title = "Dark Mode",
                    subtitle = "Use dark theme",
                    checked = darkMode,
                    onCheckedChange = { darkMode = it }
                )
            }

            // Security
            SettingsSection(title = "Security") {
                SettingsSwitch(
                    title = "Biometric Lock",
                    subtitle = "Require fingerprint/face",
                    checked = biometric,
                    onCheckedChange = { biometric = it }
                )
            }

            // About
            SettingsSection(title = "About") {
                SettingsItem(
                    title = "Version",
                    subtitle = "1.0.0",
                    onClick = { }
                )
            }
        }
    }
}

@Composable
fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(vertical = 8.dp)
        )
        Surface(
            shape = MaterialTheme.shapes.medium,
            tonalElevation = 1.dp
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                content()
            }
        }
    }
}

@Composable
fun SettingsItem(
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = { Text(subtitle) },
        modifier = Modifier.clickable(onClick = onClick)
    )
}

@Composable
fun SettingsSwitch(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = { Text(subtitle) },
        trailingContent = {
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange
            )
        }
    )
}

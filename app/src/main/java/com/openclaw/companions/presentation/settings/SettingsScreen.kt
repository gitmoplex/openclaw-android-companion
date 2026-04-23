package com.openclaw.companions.presentation.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.openclaw.companions.domain.model.DarkMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val settings by viewModel.settings.collectAsState()
    val gatewayConfig by viewModel.gatewayConfig.collectAsState()
    var showGatewayDialog by remember { mutableStateOf(false) }

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
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Connection Settings
            SettingsSection(title = "Connection") {
                SettingsItem(
                    title = "Gateway",
                    subtitle = gatewayConfig?.let {
                        "${it.host}:${it.port}"
                    } ?: "Not configured",
                    icon = Icons.Default.Router,
                    onClick = { showGatewayDialog = true }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Appearance
            SettingsSection(title = "Appearance") {
                SettingsItemWithDropdown(
                    title = "Theme",
                    subtitle = settings.darkMode.name.lowercase().replaceFirstChar { it.uppercase() },
                    icon = Icons.Default.DarkMode,
                    options = DarkMode.entries.map { it.name.lowercase().replaceFirstChar { c -> c.uppercase() } },
                    selectedIndex = DarkMode.entries.indexOf(settings.darkMode),
                    onSelected = { index ->
                        viewModel.updateDarkMode(DarkMode.entries[index])
                    }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Features
            SettingsSection(title = "Features") {
                SettingsSwitch(
                    title = "Push Notifications",
                    subtitle = "Receive alerts from OpenClaw",
                    icon = Icons.Default.Notifications,
                    checked = settings.pushNotifications,
                    onCheckedChange = { viewModel.updateNotifications(it) }
                )
                SettingsSwitch(
                    title = "Sound Effects",
                    subtitle = "Play sounds for messages",
                    icon = Icons.Default.VolumeUp,
                    checked = settings.soundEffects,
                    onCheckedChange = { viewModel.updateSoundEffects(it) }
                )
                SettingsSwitch(
                    title = "Haptic Feedback",
                    subtitle = "Vibrate on interactions",
                    icon = Icons.Default.Vibration,
                    checked = settings.hapticFeedback,
                    onCheckedChange = { viewModel.updateHaptic(it) }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Security
            SettingsSection(title = "Security") {
                SettingsSwitch(
                    title = "Biometric Lock",
                    subtitle = "Require fingerprint or face",
                    icon = Icons.Default.Fingerprint,
                    checked = settings.biometricEnabled,
                    onCheckedChange = { viewModel.updateBiometric(it) }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // About
            SettingsSection(title = "About") {
                SettingsItem(
                    title = "Version",
                    subtitle = "1.0.0",
                    icon = Icons.Default.Info,
                    onClick = { }
                )
                SettingsItem(
                    title = "OpenClaw Gateway",
                    subtitle = "v2.4.1",
                    icon = Icons.Default.Computer,
                    onClick = { }
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Reset section
            OutlinedButton(
                onClick = { /* TODO: Clear all data */ },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Icon(Icons.Default.DeleteForever, "Reset", modifier = Modifier.padding(end = 8.dp))
                Text("Reset All Data")
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    if (showGatewayDialog) {
        GatewayConfigDialog(
            currentConfig = gatewayConfig,
            onDismiss = { showGatewayDialog = false },
            onSave = { config ->
                viewModel.saveGatewayConfig(config)
                showGatewayDialog = false
            }
        )
    }
}

@Composable
fun GatewayConfigDialog(
    currentConfig: com.openclaw.companions.domain.model.GatewayConfig?,
    onDismiss: () -> Unit,
    onSave: (com.openclaw.companions.domain.model.GatewayConfig) -> Unit
) {
    var host by remember { mutableStateOf(currentConfig?.host ?: "192.168.1.126") }
    var port by remember { mutableStateOf(currentConfig?.port?.toString() ?: "18789") }
    var useSsl by remember { mutableStateOf(currentConfig?.useSsl ?: false) }
    var authToken by remember { mutableStateOf(currentConfig?.authToken ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Gateway Configuration") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = host,
                    onValueChange = { host = it },
                    label = { Text("Host") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = port,
                    onValueChange = { port = it },
                    label = { Text("Port") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = authToken,
                    onValueChange = { authToken = it },
                    label = { Text("Auth Token (optional)") },
                    singleLine = true
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Use SSL (WSS)")
                    Switch(checked = useSsl, onCheckedChange = { useSsl = it })
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        com.openclaw.companions.domain.model.GatewayConfig(
                            host = host,
                            port = port.toIntOrNull() ?: 18789,
                            useSsl = useSsl,
                            authToken = authToken.takeIf { it.isNotBlank() }
                        )
                    )
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
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
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    onClick: () -> Unit
) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = { Text(subtitle) },
        leadingContent = icon?.let {
            { Icon(it, null, tint = MaterialTheme.colorScheme.onSurfaceVariant) }
        },
        modifier = Modifier.clickable(onClick = onClick)
    )
}

@Composable
fun SettingsSwitch(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = { Text(subtitle) },
        leadingContent = icon?.let {
            { Icon(it, null, tint = MaterialTheme.colorScheme.onSurfaceVariant) }
        },
        trailingContent = {
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange
            )
        }
    )
}

@Composable
fun SettingsItemWithDropdown(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    options: List<String>,
    selectedIndex: Int,
    onSelected: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ListItem(
        headlineContent = { Text(title) },
        supportingContent = { Text(subtitle) },
        leadingContent = icon?.let {
            { Icon(it, null, tint = MaterialTheme.colorScheme.onSurfaceVariant) }
        },
        modifier = Modifier.clickable { expanded = true },
        trailingContent = {
            Icon(Icons.Default.ArrowDropDown, null)
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                options.forEachIndexed { index, option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            onSelected(index)
                            expanded = false
                        }
                    )
                }
            }
        }
    )
}

package com.openclaw.companions.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.openclaw.companions.domain.model.AppSettings
import com.openclaw.companions.domain.model.DarkMode
import com.openclaw.companions.domain.model.GatewayConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class SettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore = context.dataStore

    val settings: Flow<AppSettings> = dataStore.data.map { prefs ->
        AppSettings(
            darkMode = DarkMode.valueOf(
                prefs[DARK_MODE] ?: DarkMode.SYSTEM.name
            ),
            biometricEnabled = prefs[BIOMETRIC_ENABLED] ?: false,
            pushNotifications = prefs[PUSH_NOTIFICATIONS] ?: true,
            soundEffects = prefs[SOUND_EFFECTS] ?: true,
            hapticFeedback = prefs[HAPTIC_FEEDBACK] ?: true
        )
    }

    val gatewayConfig: Flow<GatewayConfig?> = dataStore.data.map { prefs ->
        prefs[GATEWAY_CONFIG]?.let {
            try {
                Json.decodeFromString(GatewayConfig.serializer(), it)
            } catch (_: Exception) {
                null
            }
        }
    }

    suspend fun updateDarkMode(mode: DarkMode) {
        dataStore.edit { it[DARK_MODE] = mode.name }
    }

    suspend fun updateWakeWord(enabled: Boolean) {
        dataStore.edit { it[WAKE_WORD_ENABLED] = enabled }
    }

    suspend fun updateNotifications(enabled: Boolean) {
        dataStore.edit { it[PUSH_NOTIFICATIONS] = enabled }
    }

    suspend fun updateBiometric(enabled: Boolean) {
        dataStore.edit { it[BIOMETRIC_ENABLED] = enabled }
    }

    suspend fun updateSoundEffects(enabled: Boolean) {
        dataStore.edit { it[SOUND_EFFECTS] = enabled }
    }

    suspend fun updateHaptic(enabled: Boolean) {
        dataStore.edit { it[HAPTIC_FEEDBACK] = enabled }
    }

    suspend fun saveGatewayConfig(config: GatewayConfig) {
        dataStore.edit {
            it[GATEWAY_CONFIG] = Json.encodeToString(config)
        }
    }

    companion object {
        private val DARK_MODE = stringPreferencesKey("dark_mode")
        private val WAKE_WORD_ENABLED = booleanPreferencesKey("wake_word_enabled")
        private val PUSH_NOTIFICATIONS = booleanPreferencesKey("push_notifications")
        private val BIOMETRIC_ENABLED = booleanPreferencesKey("biometric_enabled")
        private val SOUND_EFFECTS = booleanPreferencesKey("sound_effects")
        private val HAPTIC_FEEDBACK = booleanPreferencesKey("haptic_feedback")
        private val GATEWAY_CONFIG = stringPreferencesKey("gateway_config")
    }
}

package com.rasick.shared.data

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "user_settings")

class SettingsRepository(private val context: Context) {

    companion object {
        val KEY_THEME = stringPreferencesKey("app_theme")
        val KEY_AUTO_RESUME = booleanPreferencesKey("auto_resume")
        val KEY_AUTO_DOWNLOAD_WIFI = booleanPreferencesKey("auto_download_wifi")
        val KEY_STREAMING_QUALITY = stringPreferencesKey("streaming_quality")
    }

    val themeFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[KEY_THEME] ?: "System"
    }

    val autoResumeFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_AUTO_RESUME] ?: false
    }

    val autoDownloadWifiFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_AUTO_DOWNLOAD_WIFI] ?: false
    }

    val streamingQualityFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[KEY_STREAMING_QUALITY] ?: "High"
    }

    suspend fun setTheme(theme: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_THEME] = theme
        }
    }

    suspend fun setAutoResume(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_AUTO_RESUME] = enabled
        }
    }

    suspend fun setAutoDownloadWifi(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_AUTO_DOWNLOAD_WIFI] = enabled
        }
    }

    suspend fun setStreamingQuality(quality: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_STREAMING_QUALITY] = quality
        }
    }
}

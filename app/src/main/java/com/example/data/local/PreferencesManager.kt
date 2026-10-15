package com.example.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "searx_preferences")

class PreferencesManager(private val context: Context) {
    companion object {
        val KEY_THEME_MODE = stringPreferencesKey("theme_mode") // "SYSTEM", "DARK", "LIGHT"
        val KEY_LANGUAGE = stringPreferencesKey("language") // "en", "ur"
        val KEY_WIFI_ONLY = booleanPreferencesKey("wifi_only")
        val KEY_AUTO_CLIPBOARD = booleanPreferencesKey("auto_clipboard")
        val KEY_ONBOARDING_DONE = booleanPreferencesKey("onboarding_done")
    }

    val themeModeFlow: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_THEME_MODE] ?: "DARK"
    }

    val languageFlow: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_LANGUAGE] ?: "en"
    }

    val wifiOnlyFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_WIFI_ONLY] ?: false
    }

    val autoClipboardFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_AUTO_CLIPBOARD] ?: true
    }

    val onboardingDoneFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_ONBOARDING_DONE] ?: false
    }

    suspend fun setThemeMode(mode: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_THEME_MODE] = mode
        }
    }

    suspend fun setLanguage(lang: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_LANGUAGE] = lang
        }
    }

    suspend fun setWifiOnly(wifiOnly: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_WIFI_ONLY] = wifiOnly
        }
    }

    suspend fun setAutoClipboard(auto: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_AUTO_CLIPBOARD] = auto
        }
    }

    suspend fun setOnboardingDone(done: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_ONBOARDING_DONE] = done
        }
    }
}

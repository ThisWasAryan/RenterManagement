package com.rms.app.core.ui.theme

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

enum class ThemeMode { LIGHT, DARK, SYSTEM }

@Singleton
class ThemeManager @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    companion object {
        val THEME_MODE_KEY = stringPreferencesKey("theme_mode")
    }

    val themeMode: Flow<ThemeMode> = dataStore.data.map { prefs ->
        val stored = prefs[THEME_MODE_KEY]
        when (stored) {
            "LIGHT" -> ThemeMode.LIGHT
            "DARK" -> ThemeMode.DARK
            else -> ThemeMode.DARK // Default to dark
        }
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        dataStore.edit { prefs ->
            prefs[THEME_MODE_KEY] = mode.name
        }
    }
}

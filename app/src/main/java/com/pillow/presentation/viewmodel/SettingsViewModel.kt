package com.pillow.presentation.viewmodel

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pillow.ui.theme.AccentPalettes
import com.pillow.ui.theme.NoteThemes
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val SETTINGS_DATASTORE_NAME = "pillow_settings"
private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(
    name = SETTINGS_DATASTORE_NAME
)

/** Theme mode options persisted as strings. */
object ThemeMode {
    const val LIGHT = "light"
    const val DARK = "dark"
    const val SYSTEM = "system"
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val dataStore = context.settingsDataStore

    private val _themeModeState = MutableStateFlow(ThemeMode.SYSTEM)
    val themeModeState: StateFlow<String> = _themeModeState.asStateFlow()

    private val _biometricEnabledState = MutableStateFlow(false)
    val biometricEnabledState: StateFlow<Boolean> = _biometricEnabledState.asStateFlow()

    private val _accentColorState = MutableStateFlow(AccentPalettes.Dynamic.key)
    val accentColorState: StateFlow<String> = _accentColorState.asStateFlow()

    private val _defaultNoteColorState = MutableStateFlow(NoteThemes.Default.backgroundHex)
    val defaultNoteColorState: StateFlow<String> = _defaultNoteColorState.asStateFlow()

    private val _defaultTileViewState = MutableStateFlow(false)
    val defaultTileViewState: StateFlow<Boolean> = _defaultTileViewState.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            dataStore.data.collectLatest { preferences ->
                // Theme mode: prefer the new key, fall back to the legacy dark_mode boolean.
                _themeModeState.value = preferences[THEME_MODE_KEY]
                    ?: if (preferences[LEGACY_DARK_MODE_KEY] == true) ThemeMode.DARK else ThemeMode.SYSTEM
                _biometricEnabledState.value = preferences[BIOMETRIC_ENABLED_KEY] ?: false
                _accentColorState.value = preferences[ACCENT_COLOR_KEY] ?: AccentPalettes.Dynamic.key
                _defaultNoteColorState.value =
                    preferences[DEFAULT_NOTE_COLOR_KEY] ?: NoteThemes.Default.backgroundHex
                _defaultTileViewState.value = preferences[DEFAULT_TILE_VIEW_KEY] ?: false
            }
        }
    }

    fun setThemeMode(mode: String) {
        viewModelScope.launch {
            dataStore.edit { it[THEME_MODE_KEY] = mode }
        }
    }

    fun setBiometricEnabled(enabled: Boolean) {
        viewModelScope.launch {
            dataStore.edit { it[BIOMETRIC_ENABLED_KEY] = enabled }
        }
    }

    fun setAccentColor(key: String) {
        viewModelScope.launch {
            dataStore.edit { it[ACCENT_COLOR_KEY] = key }
        }
    }

    fun setDefaultNoteColor(hex: String) {
        viewModelScope.launch {
            dataStore.edit { it[DEFAULT_NOTE_COLOR_KEY] = hex }
        }
    }

    fun setDefaultTileView(tile: Boolean) {
        viewModelScope.launch {
            dataStore.edit { it[DEFAULT_TILE_VIEW_KEY] = tile }
        }
    }

    companion object {
        private val THEME_MODE_KEY = stringPreferencesKey("theme_mode")
        private val LEGACY_DARK_MODE_KEY = booleanPreferencesKey("dark_mode")
        private val BIOMETRIC_ENABLED_KEY = booleanPreferencesKey("biometric_enabled")
        private val ACCENT_COLOR_KEY = stringPreferencesKey("accent_color")
        private val DEFAULT_NOTE_COLOR_KEY = stringPreferencesKey("default_note_color")
        private val DEFAULT_TILE_VIEW_KEY = booleanPreferencesKey("default_tile_view")
    }
}

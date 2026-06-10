package com.pillow.presentation.viewmodel

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val dataStore = context.settingsDataStore

    private val _darkModeState = MutableStateFlow(false)
    val darkModeState: StateFlow<Boolean> = _darkModeState.asStateFlow()

    private val _biometricEnabledState = MutableStateFlow(false)
    val biometricEnabledState: StateFlow<Boolean> = _biometricEnabledState.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            dataStore.data.collectLatest { preferences ->
                _darkModeState.value = preferences[DARK_MODE_KEY] ?: false
                _biometricEnabledState.value = preferences[BIOMETRIC_ENABLED_KEY] ?: false
            }
        }
    }

    fun setDarkMode(enabled: Boolean) {
        viewModelScope.launch {
            dataStore.edit { preferences ->
                preferences[DARK_MODE_KEY] = enabled
            }
        }
    }

    fun setBiometricEnabled(enabled: Boolean) {
        viewModelScope.launch {
            dataStore.edit { preferences ->
                preferences[BIOMETRIC_ENABLED_KEY] = enabled
            }
        }
    }

    companion object {
        private val DARK_MODE_KEY = booleanPreferencesKey("dark_mode")
        private val BIOMETRIC_ENABLED_KEY = booleanPreferencesKey("biometric_enabled")
    }
}

package com.pillow.ui

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import androidx.compose.foundation.isSystemInDarkTheme
import com.pillow.biometric.BiometricAuthManager
import com.pillow.presentation.viewmodel.SettingsViewModel
import com.pillow.presentation.viewmodel.ThemeMode
import com.pillow.ui.navigation.PillowNavGraph
import com.pillow.ui.theme.PillowTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : FragmentActivity() {

    @Inject
    lateinit var biometricAuthManager: BiometricAuthManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val settingsViewModel: SettingsViewModel = hiltViewModel()
            val themeMode = settingsViewModel.themeModeState.collectAsState()
            val accentColor = settingsViewModel.accentColorState.collectAsState()
            val biometricEnabled = settingsViewModel.biometricEnabledState.collectAsState()

            val darkTheme = when (themeMode.value) {
                ThemeMode.DARK -> true
                ThemeMode.LIGHT -> false
                else -> isSystemInDarkTheme()
            }

            // Whether the app content is currently locked behind biometric auth.
            var isLocked by remember { mutableStateOf(false) }

            // Lock the app whenever the app-lock setting is enabled and a biometric
            // (or device credential) is actually available on this device.
            LaunchedEffect(biometricEnabled.value) {
                isLocked = biometricEnabled.value && biometricAuthManager.isBiometricAvailable()
            }

            PillowTheme(darkTheme = darkTheme, accentKey = accentColor.value) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (isLocked) {
                        // Automatically present the biometric prompt when locked.
                        LaunchedEffect(Unit) { promptUnlock { isLocked = false } }
                        LockScreen(onUnlockClick = { promptUnlock { isLocked = false } })
                    } else {
                        val navController = rememberNavController()
                        PillowNavGraph(navController = navController)
                    }
                }
            }
        }
    }

    private fun promptUnlock(onUnlocked: () -> Unit) {
        biometricAuthManager.authenticate(
            activity = this,
            onSuccess = onUnlocked,
            onError = { /* User cancelled or error — remain locked. */ },
            onFailed = { /* Not recognized — remain locked, prompt allows retry. */ }
        )
    }
}

@Composable
private fun LockScreen(onUnlockClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Filled.Lock,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        Text(
            text = "Pillow is locked",
            style = MaterialTheme.typography.headlineSmall
        )
        Text(
            text = "Authenticate to access your notes",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
        )
        Button(onClick = onUnlockClick) {
            Text("Unlock")
        }
    }
}

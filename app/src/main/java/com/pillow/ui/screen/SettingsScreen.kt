package com.pillow.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.pillow.presentation.viewmodel.SettingsViewModel
import com.pillow.presentation.viewmodel.ThemeMode
import com.pillow.ui.theme.AccentPalettes
import com.pillow.ui.theme.NoteThemes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    onBackClick: () -> Unit = {}
) {
    val themeMode = viewModel.themeModeState.collectAsState()
    val accentColor = viewModel.accentColorState.collectAsState()
    val defaultNoteColor = viewModel.defaultNoteColorState.collectAsState()
    val defaultTileView = viewModel.defaultTileViewState.collectAsState()
    val biometricEnabled = viewModel.biometricEnabledState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(vertical = 16.dp)
        ) {
            // Theme mode
            SectionHeader("Theme")
            val modes = listOf(
                ThemeMode.LIGHT to "Light",
                ThemeMode.DARK to "Dark",
                ThemeMode.SYSTEM to "System"
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                modes.forEach { (value, label) ->
                    FilterChip(
                        selected = themeMode.value == value,
                        onClick = { viewModel.setThemeMode(value) },
                        label = { Text(label) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Accent color
            SectionHeader("Accent color")
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(AccentPalettes.all) { accent ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .background(accent.swatch, CircleShape)
                                .then(
                                    if (accentColor.value == accent.key)
                                        Modifier.border(3.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
                                    else Modifier
                                )
                                .clickable { viewModel.setAccentColor(accent.key) },
                            contentAlignment = Alignment.Center
                        ) {
                            if (accentColor.value == accent.key) {
                                Icon(
                                    Icons.Filled.Check,
                                    contentDescription = null,
                                    tint = androidx.compose.ui.graphics.Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(accent.label, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Default note color
            SectionHeader("Default note color")
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(NoteThemes.all) { theme ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .background(theme.background, CircleShape)
                                .then(
                                    if (defaultNoteColor.value.equals(theme.backgroundHex, ignoreCase = true))
                                        Modifier.border(3.dp, MaterialTheme.colorScheme.primary, CircleShape)
                                    else Modifier
                                )
                                .clickable { viewModel.setDefaultNoteColor(theme.backgroundHex) },
                            contentAlignment = Alignment.Center
                        ) {
                            if (defaultNoteColor.value.equals(theme.backgroundHex, ignoreCase = true)) {
                                Icon(
                                    Icons.Filled.Check,
                                    contentDescription = null,
                                    tint = theme.onBackground,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(theme.label, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Default view
            SectionHeader("Default view")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = !defaultTileView.value,
                    onClick = { viewModel.setDefaultTileView(false) },
                    label = { Text("List") }
                )
                FilterChip(
                    selected = defaultTileView.value,
                    onClick = { viewModel.setDefaultTileView(true) },
                    label = { Text("Tile") }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // App Lock
            SectionHeader("Security")
            SettingItem(
                title = "App Lock",
                description = "Secure your notes with fingerprint authentication",
                isEnabled = biometricEnabled.value,
                onToggle = { viewModel.setBiometricEnabled(it) }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // About
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text(
                    "About",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text("Pillow Notes v1.0.0", style = MaterialTheme.typography.bodyMedium)
                Text(
                    "A beautiful notes app to capture your thoughts",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

@Composable
fun SettingItem(
    title: String,
    description: String = "",
    isEnabled: Boolean = false,
    onToggle: (Boolean) -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge)
            if (description.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Switch(checked = isEnabled, onCheckedChange = onToggle)
    }
}

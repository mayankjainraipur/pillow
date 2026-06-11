package com.pillow.ui.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Side-menu contents shown by the [androidx.compose.material3.ModalNavigationDrawer]
 * on the home screen. "Notes" is the current destination, so selecting it simply
 * closes the drawer; the others navigate away.
 */
@Composable
fun PillowDrawerContent(
    onNotesClick: () -> Unit,
    onFavoritesClick: () -> Unit,
    onPinnedClick: () -> Unit,
    onArchiveClick: () -> Unit,
    onBucketsClick: () -> Unit,
    onTrashClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onBackupClick: () -> Unit
) {
    ModalDrawerSheet {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = "Pillow Notes",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(16.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))

            NavigationDrawerItem(
                icon = { Icon(Icons.AutoMirrored.Filled.ViewList, contentDescription = null) },
                label = { Text("Notes") },
                selected = true,
                onClick = onNotesClick,
                modifier = Modifier.fillMaxWidth()
            )
            NavigationDrawerItem(
                icon = { Icon(Icons.Filled.Star, contentDescription = null) },
                label = { Text("Favorites") },
                selected = false,
                onClick = onFavoritesClick,
                modifier = Modifier.fillMaxWidth()
            )
            NavigationDrawerItem(
                icon = { Icon(Icons.Filled.PushPin, contentDescription = null) },
                label = { Text("Pinned") },
                selected = false,
                onClick = onPinnedClick,
                modifier = Modifier.fillMaxWidth()
            )
            NavigationDrawerItem(
                icon = { Icon(Icons.Filled.Archive, contentDescription = null) },
                label = { Text("Archive") },
                selected = false,
                onClick = onArchiveClick,
                modifier = Modifier.fillMaxWidth()
            )
            NavigationDrawerItem(
                icon = { Icon(Icons.Filled.Folder, contentDescription = null) },
                label = { Text("Buckets") },
                selected = false,
                onClick = onBucketsClick,
                modifier = Modifier.fillMaxWidth()
            )
            NavigationDrawerItem(
                icon = { Icon(Icons.Filled.Delete, contentDescription = null) },
                label = { Text("Trash") },
                selected = false,
                onClick = onTrashClick,
                modifier = Modifier.fillMaxWidth()
            )
            NavigationDrawerItem(
                icon = { Icon(Icons.Filled.Settings, contentDescription = null) },
                label = { Text("Settings") },
                selected = false,
                onClick = onSettingsClick,
                modifier = Modifier.fillMaxWidth()
            )
            NavigationDrawerItem(
                icon = { Icon(Icons.Filled.Backup, contentDescription = null) },
                label = { Text("Backup & Restore") },
                selected = false,
                onClick = onBackupClick,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

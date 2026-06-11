package com.pillow.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.pillow.presentation.viewmodel.CategoryViewModel
import com.pillow.presentation.viewmodel.NoteViewModel
import com.pillow.presentation.viewmodel.SettingsViewModel

/** The three sidebar destinations that reuse the standard notes list. */
enum class NoteSection(val title: String, val emptyTitle: String) {
    FAVORITES("Favorites", "No favorite notes"),
    PINNED("Pinned", "No pinned notes"),
    ARCHIVE("Archive", "Archive is empty")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteSectionScreen(
    section: NoteSection,
    viewModel: NoteViewModel = hiltViewModel(),
    settingsViewModel: SettingsViewModel = hiltViewModel(),
    categoryViewModel: CategoryViewModel = hiltViewModel(),
    onBackClick: () -> Unit = {},
    onNoteClick: (Long) -> Unit = {}
) {
    val notes = viewModel.notesState.collectAsState()
    val buckets = categoryViewModel.categoriesState.collectAsState()
    val defaultTileView = settingsViewModel.defaultTileViewState.collectAsState()

    var userToggledTile by remember { mutableStateOf<Boolean?>(null) }
    val isTileView = userToggledTile ?: defaultTileView.value

    // Point this screen's ViewModel at the right data source.
    LaunchedEffect(section) {
        when (section) {
            NoteSection.FAVORITES -> viewModel.loadFavoriteNotes()
            NoteSection.PINNED -> viewModel.loadPinnedNotes()
            NoteSection.ARCHIVE -> viewModel.loadArchivedNotes()
        }
    }

    val actions = rememberNoteActions(viewModel, onNoteClick)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(section.title) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { userToggledTile = !isTileView }) {
                        Icon(
                            imageVector = if (isTileView) Icons.AutoMirrored.Filled.ViewList
                            else Icons.Filled.GridView,
                            contentDescription = if (isTileView) "List view" else "Tile view"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            NoteListContent(
                notes = notes.value,
                isTileView = isTileView,
                buckets = buckets.value,
                actions = actions,
                modifier = Modifier.fillMaxSize(),
                archiveMode = section == NoteSection.ARCHIVE,
                emptyTitle = section.emptyTitle,
                emptySubtitle = ""
            )
        }
    }
}

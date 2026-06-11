package com.pillow.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.pillow.presentation.viewmodel.CategoryViewModel
import com.pillow.presentation.viewmodel.NoteViewModel
import com.pillow.presentation.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BucketDetailScreen(
    bucketId: Long,
    noteViewModel: NoteViewModel = hiltViewModel(),
    categoryViewModel: CategoryViewModel = hiltViewModel(),
    settingsViewModel: SettingsViewModel = hiltViewModel(),
    onBackClick: () -> Unit = {},
    onNoteClick: (Long) -> Unit = {}
) {
    val notes = noteViewModel.categoryNotesState.collectAsState()
    val categories = categoryViewModel.categoriesState.collectAsState()
    val isTileView by settingsViewModel.defaultTileViewState.collectAsState()
    val bucketName = categories.value.firstOrNull { it.id == bucketId }?.name ?: "Bucket"

    LaunchedEffect(bucketId) {
        noteViewModel.loadNotesByCategory(bucketId)
    }

    val actions = rememberNoteActions(noteViewModel, onNoteClick)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(bucketName.ifEmpty { "Bucket" }) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
        ) {
            NoteListContent(
                notes = notes.value,
                isTileView = isTileView,
                buckets = categories.value,
                actions = actions,
                modifier = Modifier.fillMaxSize(),
                emptyTitle = "No notes in this bucket",
                emptySubtitle = "Assign notes to this bucket from the note editor"
            )
        }
    }
}

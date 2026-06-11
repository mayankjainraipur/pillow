package com.pillow.ui.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.pillow.presentation.viewmodel.CategoryViewModel
import com.pillow.presentation.viewmodel.NoteFilter
import com.pillow.presentation.viewmodel.NoteViewModel
import com.pillow.presentation.viewmodel.SettingsViewModel
import com.pillow.presentation.viewmodel.SortOption
import com.pillow.ui.navigation.PillowDrawerContent
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: NoteViewModel = hiltViewModel(),
    settingsViewModel: SettingsViewModel = hiltViewModel(),
    categoryViewModel: CategoryViewModel = hiltViewModel(),
    onNoteClick: (Long) -> Unit = {},
    onCreateNoteClick: () -> Unit = {},
    onFavoritesClick: () -> Unit = {},
    onPinnedClick: () -> Unit = {},
    onArchiveClick: () -> Unit = {},
    onBucketsClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    onTrashClick: () -> Unit = {},
    onBackupClick: () -> Unit = {}
) {
    val notes = viewModel.notesState.collectAsState()
    val buckets = categoryViewModel.categoriesState.collectAsState()
    val sortOption = viewModel.sortOption.collectAsState()
    val filter = viewModel.filter.collectAsState()
    val searchQuery = remember { mutableStateOf("") }
    val defaultTileView = settingsViewModel.defaultTileViewState.collectAsState()

    // Null until the user toggles this session; falls back to the saved default.
    var userToggledTile by remember { mutableStateOf<Boolean?>(null) }
    val isTileView = userToggledTile ?: defaultTileView.value

    var showSortMenu by remember { mutableStateOf(false) }
    var showFilterMenu by remember { mutableStateOf(false) }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val actions = rememberNoteActions(viewModel, onNoteClick)

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            PillowDrawerContent(
                onNotesClick = { scope.launch { drawerState.close() } },
                onFavoritesClick = { scope.launch { drawerState.close() }; onFavoritesClick() },
                onPinnedClick = { scope.launch { drawerState.close() }; onPinnedClick() },
                onArchiveClick = { scope.launch { drawerState.close() }; onArchiveClick() },
                onBucketsClick = { scope.launch { drawerState.close() }; onBucketsClick() },
                onTrashClick = { scope.launch { drawerState.close() }; onTrashClick() },
                onSettingsClick = { scope.launch { drawerState.close() }; onSettingsClick() },
                onBackupClick = { scope.launch { drawerState.close() }; onBackupClick() }
            )
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Pillow Notes") },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Filled.Menu, contentDescription = "Menu")
                        }
                    },
                    actions = {
                        Box {
                            IconButton(onClick = { showSortMenu = true }) {
                                Icon(Icons.AutoMirrored.Filled.Sort, contentDescription = "Sort")
                            }
                            DropdownMenu(
                                expanded = showSortMenu,
                                onDismissRequest = { showSortMenu = false }
                            ) {
                                SortOption.values().forEach { option ->
                                    DropdownMenuItem(
                                        text = { Text(option.label) },
                                        trailingIcon = {
                                            if (option == sortOption.value) {
                                                Icon(Icons.Filled.Check, contentDescription = null)
                                            }
                                        },
                                        onClick = { viewModel.setSort(option); showSortMenu = false }
                                    )
                                }
                            }
                        }
                        Box {
                            IconButton(onClick = { showFilterMenu = true }) {
                                Icon(Icons.Filled.FilterList, contentDescription = "Filter")
                            }
                            DropdownMenu(
                                expanded = showFilterMenu,
                                onDismissRequest = { showFilterMenu = false }
                            ) {
                                NoteFilter.values().forEach { option ->
                                    DropdownMenuItem(
                                        text = { Text(option.label) },
                                        trailingIcon = {
                                            if (option == filter.value) {
                                                Icon(Icons.Filled.Check, contentDescription = null)
                                            }
                                        },
                                        onClick = { viewModel.setFilter(option); showFilterMenu = false }
                                    )
                                }
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary,
                        navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                        actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = onCreateNoteClick,
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Add Note")
                }
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Fixed, rounded search bar with the list/tile toggle tucked inside.
                OutlinedTextField(
                    value = searchQuery.value,
                    onValueChange = {
                        searchQuery.value = it
                        viewModel.searchNotes(it)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    placeholder = { Text("Search notes...") },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                    trailingIcon = {
                        IconButton(onClick = { userToggledTile = !isTileView }) {
                            Icon(
                                imageVector = if (isTileView) Icons.AutoMirrored.Filled.ViewList
                                else Icons.Filled.GridView,
                                contentDescription = if (isTileView) "List view" else "Tile view"
                            )
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(28.dp)
                )

                NoteListContent(
                    notes = notes.value,
                    isTileView = isTileView,
                    buckets = buckets.value,
                    actions = actions,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

/** Builds the standard set of [NoteActions] backed by [NoteViewModel]. Shared by all list screens. */
@Composable
fun rememberNoteActions(
    viewModel: NoteViewModel,
    onNoteClick: (Long) -> Unit
): NoteActions = remember(viewModel, onNoteClick) {
    NoteActions(
        onClick = onNoteClick,
        onTogglePin = { viewModel.togglePinnedNote(it.id, it.isPinned) },
        onToggleFavorite = { viewModel.toggleFavoriteNote(it.id, it.isFavorite) },
        onDuplicate = { viewModel.duplicateNote(it.id) },
        onMoveToBucket = { note, bucketId -> viewModel.moveNoteToBucket(note.id, bucketId) },
        onMarkShared = { viewModel.markShared(it.id) },
        onArchive = { viewModel.archiveNote(it.id) },
        onUnarchive = { viewModel.unarchiveNote(it.id) },
        onDelete = { viewModel.trashNote(it.id) }
    )
}

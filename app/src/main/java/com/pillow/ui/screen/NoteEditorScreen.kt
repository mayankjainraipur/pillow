package com.pillow.ui.screen

import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.pillow.domain.model.Note
import com.pillow.presentation.viewmodel.NoteViewModel
import com.pillow.ui.theme.NoteTheme
import com.pillow.ui.theme.NoteThemes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteEditorScreen(
    noteId: Long = 0,
    viewModel: NoteViewModel = hiltViewModel(),
    onBackClick: () -> Unit = {}
) {
    val currentNote = viewModel.currentNoteState.collectAsState()

    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var selectedTheme by remember { mutableStateOf(NoteThemes.Default) }
    // New notes need no loading; existing notes initialize once their data arrives.
    var initialized by remember { mutableStateOf(noteId <= 0) }

    // Trigger the load exactly once per noteId (not on every recomposition).
    LaunchedEffect(noteId) {
        if (noteId > 0) viewModel.loadNoteById(noteId)
    }

    // Populate the fields the first time the matching note is emitted, then leave
    // them under the user's control so typing is never overwritten.
    LaunchedEffect(currentNote.value) {
        val note = currentNote.value
        if (!initialized && note != null && note.id == noteId) {
            title = note.title
            content = note.content
            selectedTheme = NoteThemes.fromHex(note.color)
            initialized = true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (noteId > 0) "Edit Note" else "New Note") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Filled.Check, contentDescription = "Back")
                    }
                },
                actions = {
                    if (noteId > 0) {
                        IconButton(onClick = { viewModel.trashNote(noteId); onBackClick() }) {
                            Icon(Icons.Filled.Delete, contentDescription = "Move to Trash")
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
        containerColor = selectedTheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            val fieldColors = TextFieldDefaults.colors(
                unfocusedContainerColor = Color.Transparent,
                focusedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                focusedTextColor = selectedTheme.onBackground,
                unfocusedTextColor = selectedTheme.onBackground,
                cursorColor = selectedTheme.onBackground
            )

            TextField(
                value = title,
                onValueChange = { title = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Title") },
                textStyle = MaterialTheme.typography.headlineSmall,
                colors = fieldColors,
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            TextField(
                value = content,
                onValueChange = { content = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                placeholder = { Text("Start typing...") },
                textStyle = MaterialTheme.typography.bodyMedium,
                colors = fieldColors
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text("Theme", style = MaterialTheme.typography.labelLarge, color = selectedTheme.onBackground)
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(NoteThemes.all) { theme ->
                    ThemeSwatch(
                        theme = theme,
                        selected = theme.key == selectedTheme.key,
                        onClick = { selectedTheme = theme }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            VoiceMemoSection(noteId = noteId, labelColor = selectedTheme.onBackground)

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(onClick = onBackClick, modifier = Modifier.weight(1f)) {
                    Text("Cancel")
                }

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = {
                        val note = Note(
                            id = if (noteId > 0) noteId else 0,
                            title = title,
                            content = content,
                            color = selectedTheme.backgroundHex,
                            updatedAt = System.currentTimeMillis()
                        )
                        if (noteId > 0) viewModel.updateNote(note) else viewModel.createNote(note)
                        onBackClick()
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Save")
                }
            }
        }
    }
}

@Composable
private fun ThemeSwatch(theme: NoteTheme, selected: Boolean, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Card(
            modifier = Modifier
                .size(48.dp)
                .then(
                    if (selected) Modifier.border(3.dp, MaterialTheme.colorScheme.primary, CircleShape)
                    else Modifier
                ),
            onClick = onClick,
            shape = CircleShape,
            colors = CardDefaults.cardColors(containerColor = theme.background)
        ) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                if (selected) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = null,
                        tint = theme.onBackground,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = theme.label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

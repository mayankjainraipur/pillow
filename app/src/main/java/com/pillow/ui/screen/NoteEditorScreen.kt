package com.pillow.ui.screen

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mohamedrejeb.richeditor.model.rememberRichTextState
import com.mohamedrejeb.richeditor.ui.BasicRichTextEditor
import com.pillow.domain.model.Category
import com.pillow.domain.model.Note
import com.pillow.presentation.viewmodel.AttachmentViewModel
import com.pillow.presentation.viewmodel.CategoryViewModel
import com.pillow.presentation.viewmodel.NoteViewModel
import com.pillow.presentation.viewmodel.SettingsViewModel
import com.pillow.presentation.viewmodel.VoiceMemoViewModel
import com.pillow.ui.theme.NoteTheme
import com.pillow.ui.theme.NoteThemes
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteEditorScreen(
    noteId: Long = 0,
    viewModel: NoteViewModel = hiltViewModel(),
    categoryViewModel: CategoryViewModel = hiltViewModel(),
    settingsViewModel: SettingsViewModel = hiltViewModel(),
    voiceMemoViewModel: VoiceMemoViewModel = hiltViewModel(),
    attachmentViewModel: AttachmentViewModel = hiltViewModel(),
    onBackClick: () -> Unit = {}
) {
    val currentNote = viewModel.currentNoteState.collectAsState()
    val buckets = categoryViewModel.categoriesState.collectAsState()
    val defaultBucketId = categoryViewModel.defaultBucketIdState.collectAsState()
    val defaultNoteColor = settingsViewModel.defaultNoteColorState.collectAsState()
    val savedNoteId = viewModel.savedNoteIdState.collectAsState()

    var title by remember { mutableStateOf("") }
    val richTextState = rememberRichTextState()
    var selectedTheme by remember { mutableStateOf(NoteThemes.fromHex(defaultNoteColor.value)) }
    var selectedBucketId by remember { mutableStateOf<Long?>(null) }
    var showThemeDialog by remember { mutableStateOf(false) }
    var showLinkDialog by remember { mutableStateOf(false) }
    var showVoiceSheet by remember { mutableStateOf(false) }
    // New notes need no loading; existing notes initialize once their data arrives.
    var initialized by remember { mutableStateOf(noteId <= 0) }

    val charCount = richTextState.annotatedString.text.length

    val imagePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri -> uri?.let { attachmentViewModel.addImage(it) } }

    val filePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri -> uri?.let { attachmentViewModel.addFile(it) } }

    // For a brand-new note, apply the user's default note color once it loads.
    LaunchedEffect(defaultNoteColor.value) {
        if (noteId <= 0 && richTextState.annotatedString.text.isEmpty() && title.isEmpty()) {
            selectedTheme = NoteThemes.fromHex(defaultNoteColor.value)
        }
    }

    // A new note always starts in the default bucket (notes never live bucket-less).
    LaunchedEffect(defaultBucketId.value) {
        if (noteId <= 0 && selectedBucketId == null) {
            selectedBucketId = defaultBucketId.value
        }
    }

    // Trigger the load exactly once per noteId, and point the attachment VM at it.
    LaunchedEffect(noteId) {
        if (noteId > 0) viewModel.loadNoteById(noteId)
        attachmentViewModel.setNote(noteId)
    }

    // After a new note is created, commit pending voice memos + attachments then navigate back.
    LaunchedEffect(savedNoteId.value) {
        val newId = savedNoteId.value ?: return@LaunchedEffect
        voiceMemoViewModel.commitPendingMemos(newId)
        attachmentViewModel.commitPending(newId)
        viewModel.consumeSavedNoteId()
        onBackClick()
    }

    // Populate the fields the first time the matching note is emitted, then leave
    // them under the user's control so typing is never overwritten.
    LaunchedEffect(currentNote.value) {
        val note = currentNote.value
        if (!initialized && note != null && note.id == noteId) {
            title = note.title
            richTextState.setMarkdown(note.content)
            selectedTheme = NoteThemes.fromHex(note.color)
            // Fall back to the default bucket if the note somehow had none.
            selectedBucketId = note.categoryId ?: defaultBucketId.value
            initialized = true
        }
    }

    fun discardIfNew() {
        if (noteId <= 0) {
            voiceMemoViewModel.discardPendingMemos()
            attachmentViewModel.discardPending()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (noteId > 0) "Edit Note" else "New Note") },
                navigationIcon = {
                    IconButton(onClick = {
                        discardIfNew()
                        onBackClick()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showThemeDialog = true }) {
                        Icon(Icons.Filled.Palette, contentDescription = "Note color")
                    }
                    if (noteId > 0) {
                        IconButton(onClick = { viewModel.trashNote(noteId); onBackClick() }) {
                            Icon(Icons.Filled.Delete, contentDescription = "Move to Trash")
                        }
                    }
                    FilledIconButton(
                        onClick = {
                            val markdown = richTextState.toMarkdown()
                            // Don't persist an empty note — discard any pending media and leave.
                            if (title.isBlank() && richTextState.annotatedString.text.isBlank()) {
                                discardIfNew()
                                onBackClick()
                                return@FilledIconButton
                            }
                            val note = Note(
                                id = if (noteId > 0) noteId else 0,
                                title = title,
                                content = markdown,
                                color = selectedTheme.backgroundHex,
                                categoryId = selectedBucketId ?: defaultBucketId.value,
                                updatedAt = System.currentTimeMillis()
                            )
                            if (noteId > 0) {
                                viewModel.updateNote(note)
                                onBackClick()
                            } else {
                                // createNoteForEditor emits the ID → LaunchedEffect commits media + navigates
                                viewModel.createNoteForEditor(note)
                            }
                        },
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.onPrimary,
                            contentColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(Icons.Filled.Check, contentDescription = "Save")
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
        bottomBar = {
            EditorToolbar(
                state = richTextState,
                onPickImage = {
                    imagePicker.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                onPickFile = { filePicker.launch(arrayOf("*/*")) },
                onAddLink = { showLinkDialog = true },
                onInsertTable = {
                    richTextState.replaceSelectedText("\n| Col 1 | Col 2 |\n| --- | --- |\n|  |  |\n")
                },
                onVoice = { showVoiceSheet = true }
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

            val dateFormatter = remember { SimpleDateFormat("MMM d, yyyy", Locale.getDefault()) }
            val displayDate = remember(currentNote.value?.updatedAt, noteId) {
                val ts = if (noteId > 0) currentNote.value?.updatedAt ?: System.currentTimeMillis()
                         else System.currentTimeMillis()
                dateFormatter.format(Date(ts))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    "$displayDate  ·  $charCount chars",
                    style = MaterialTheme.typography.labelMedium,
                    color = selectedTheme.onBackground
                )
                Spacer(modifier = Modifier.weight(1f))
                BucketDropdown(
                    buckets = buckets.value,
                    selectedBucketId = selectedBucketId,
                    onSelect = { selectedBucketId = it }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            TextField(
                value = title,
                onValueChange = { title = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Title") },
                textStyle = MaterialTheme.typography.headlineSmall,
                colors = fieldColors,
                singleLine = true
            )

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = selectedTheme.onBackground.copy(alpha = 0.2f))
            Spacer(modifier = Modifier.height(8.dp))

            BasicRichTextEditor(
                state = richTextState,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 280.dp),
                textStyle = MaterialTheme.typography.bodyLarge.copy(color = selectedTheme.onBackground),
                cursorBrush = SolidColor(selectedTheme.onBackground)
            )

            Spacer(modifier = Modifier.height(24.dp))

            AttachmentSection(noteId = noteId, labelColor = selectedTheme.onBackground)
        }
    }

    if (showThemeDialog) {
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            title = { Text("Note color") },
            text = {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(NoteThemes.all) { theme ->
                        ThemeSwatch(
                            theme = theme,
                            selected = theme.key == selectedTheme.key,
                            onClick = {
                                selectedTheme = theme
                                showThemeDialog = false
                            }
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showThemeDialog = false }) { Text("Done") }
            }
        )
    }

    if (showLinkDialog) {
        LinkDialog(
            onDismiss = { showLinkDialog = false },
            onConfirm = { text, url ->
                if (url.isNotBlank()) {
                    richTextState.addLink(text.ifBlank { url }, url)
                }
                showLinkDialog = false
            }
        )
    }

    if (showVoiceSheet) {
        ModalBottomSheet(
            onDismissRequest = { showVoiceSheet = false },
            sheetState = rememberModalBottomSheetState()
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                VoiceMemoSection(noteId = noteId)
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun LinkDialog(
    onDismiss: () -> Unit,
    onConfirm: (text: String, url: String) -> Unit
) {
    var text by remember { mutableStateOf("") }
    var url by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add link") },
        text = {
            Column {
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    label = { Text("Text") },
                    singleLine = true
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it },
                    label = { Text("URL (https://…)") },
                    singleLine = true
                )
            }
        },
        confirmButton = { TextButton(onClick = { onConfirm(text, url) }) { Text("Add") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun BucketDropdown(
    buckets: List<Category>,
    selectedBucketId: Long?,
    onSelect: (Long) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val label = buckets.firstOrNull { it.id == selectedBucketId }
        ?.name?.ifEmpty { "Untitled" } ?: "Bucket"
    Box {
        AssistChip(
            onClick = { expanded = true },
            label = { Text(label) },
            trailingIcon = { Icon(Icons.Filled.ArrowDropDown, contentDescription = null) }
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            buckets.forEach { bucket ->
                DropdownMenuItem(
                    text = { Text(bucket.name.ifEmpty { "Untitled" }) },
                    onClick = { onSelect(bucket.id); expanded = false }
                )
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

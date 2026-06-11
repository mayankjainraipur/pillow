package com.pillow.ui.screen

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DriveFileMove
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.Unarchive
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.pillow.domain.model.Category
import com.pillow.domain.model.Note
import com.pillow.ui.theme.NoteThemes
import com.pillow.util.shareNoteText
import com.pillow.util.stripMarkdown
import com.pillow.util.suggestedFileName
import com.pillow.util.writeNoteToUri

/**
 * The per-note actions surfaced by the 3-dot overflow menu. Screens supply
 * ViewModel-backed lambdas; Share/Export are handled inside [NoteListContent]
 * (they need an Android context / document launcher) and call back via
 * [onMarkShared] so the Shared filter stays accurate.
 */
data class NoteActions(
    val onClick: (Long) -> Unit = {},
    val onTogglePin: (Note) -> Unit = {},
    val onToggleFavorite: (Note) -> Unit = {},
    val onDuplicate: (Note) -> Unit = {},
    val onMoveToBucket: (Note, Long) -> Unit = { _, _ -> },
    val onMarkShared: (Note) -> Unit = {},
    val onArchive: (Note) -> Unit = {},
    val onUnarchive: (Note) -> Unit = {},
    val onDelete: (Note) -> Unit = {}
)

/**
 * Shared notes list used by Home, Favorites, Pinned and Archive. Renders either a
 * 2-column grid or a single column, with an empty state and a per-note overflow
 * menu. In [archiveMode] the overflow offers Restore + Delete instead of Archive.
 */
@Composable
fun NoteListContent(
    notes: List<Note>,
    isTileView: Boolean,
    buckets: List<Category>,
    actions: NoteActions,
    modifier: Modifier = Modifier,
    archiveMode: Boolean = false,
    emptyTitle: String = "No notes yet",
    emptySubtitle: String = "Create your first note to get started"
) {
    val context = LocalContext.current
    var pendingExport by remember { mutableStateOf<Note?>(null) }
    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("text/markdown")
    ) { uri ->
        val note = pendingExport
        if (uri != null && note != null) writeNoteToUri(context, note, uri)
        pendingExport = null
    }

    val onShare: (Note) -> Unit = { note ->
        shareNoteText(context, note)
        actions.onMarkShared(note)
    }
    val onExport: (Note) -> Unit = { note ->
        pendingExport = note
        exportLauncher.launch(suggestedFileName(note))
    }

    if (notes.isEmpty()) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(emptyTitle, style = MaterialTheme.typography.headlineMedium)
            Text(
                emptySubtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    } else if (isTileView) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = modifier,
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(notes) { note ->
                NoteItemCard(note, buckets, actions, onShare, onExport, archiveMode)
            }
        }
    } else {
        LazyColumn(
            modifier = modifier,
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(notes) { note ->
                NoteItemCard(note, buckets, actions, onShare, onExport, archiveMode)
            }
        }
    }
}

@Composable
fun NoteItemCard(
    note: Note,
    buckets: List<Category>,
    actions: NoteActions,
    onShare: (Note) -> Unit,
    onExport: (Note) -> Unit,
    archiveMode: Boolean
) {
    val theme = NoteThemes.fromHex(note.color)
    var menuExpanded by remember { mutableStateOf(false) }
    var showBucketPicker by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { actions.onClick(note.id) }
            .padding(4.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = theme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, top = 12.dp, end = 4.dp, bottom = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = note.title.ifEmpty { "Untitled" },
                    style = MaterialTheme.typography.titleMedium,
                    color = theme.onBackground,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (note.isFavorite) {
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = "Favorite",
                        tint = theme.onBackground,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.size(4.dp))
                }
                if (note.isPinned) {
                    Icon(
                        imageVector = Icons.Filled.PushPin,
                        contentDescription = "Pinned",
                        tint = theme.onBackground,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Box {
                    IconButton(onClick = { menuExpanded = true }, modifier = Modifier.size(28.dp)) {
                        Icon(
                            imageVector = Icons.Filled.MoreVert,
                            contentDescription = "More actions",
                            tint = theme.onBackground
                        )
                    }
                    NoteOverflowMenu(
                        note = note,
                        expanded = menuExpanded,
                        archiveMode = archiveMode,
                        onDismiss = { menuExpanded = false },
                        actions = actions,
                        onShare = onShare,
                        onExport = onExport,
                        onPickBucket = { showBucketPicker = true }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stripMarkdown(note.content).take(100),
                style = MaterialTheme.typography.bodySmall,
                color = theme.onBackground.copy(alpha = 0.75f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }

    if (showBucketPicker) {
        BucketPickerDialog(
            buckets = buckets,
            currentBucketId = note.categoryId,
            onDismiss = { showBucketPicker = false },
            onSelect = { bucketId ->
                actions.onMoveToBucket(note, bucketId)
                showBucketPicker = false
            }
        )
    }
}

@Composable
private fun NoteOverflowMenu(
    note: Note,
    expanded: Boolean,
    archiveMode: Boolean,
    onDismiss: () -> Unit,
    actions: NoteActions,
    onShare: (Note) -> Unit,
    onExport: (Note) -> Unit,
    onPickBucket: () -> Unit
) {
    DropdownMenu(expanded = expanded, onDismissRequest = onDismiss) {
        DropdownMenuItem(
            text = { Text("Duplicate") },
            leadingIcon = { Icon(Icons.Filled.ContentCopy, contentDescription = null) },
            onClick = { actions.onDuplicate(note); onDismiss() }
        )
        DropdownMenuItem(
            text = { Text(if (note.isPinned) "Unpin" else "Pin") },
            leadingIcon = {
                Icon(
                    if (note.isPinned) Icons.Outlined.PushPin else Icons.Filled.PushPin,
                    contentDescription = null
                )
            },
            onClick = { actions.onTogglePin(note); onDismiss() }
        )
        DropdownMenuItem(
            text = { Text(if (note.isFavorite) "Unfavorite" else "Favorite") },
            leadingIcon = {
                Icon(
                    if (note.isFavorite) Icons.Filled.StarBorder else Icons.Filled.Star,
                    contentDescription = null
                )
            },
            onClick = { actions.onToggleFavorite(note); onDismiss() }
        )
        DropdownMenuItem(
            text = { Text("Move to bucket") },
            leadingIcon = { Icon(Icons.AutoMirrored.Filled.DriveFileMove, contentDescription = null) },
            onClick = { onDismiss(); onPickBucket() }
        )
        DropdownMenuItem(
            text = { Text("Share") },
            leadingIcon = { Icon(Icons.Filled.Share, contentDescription = null) },
            onClick = { onDismiss(); onShare(note) }
        )
        DropdownMenuItem(
            text = { Text("Export") },
            leadingIcon = { Icon(Icons.Filled.FileDownload, contentDescription = null) },
            onClick = { onDismiss(); onExport(note) }
        )
        if (archiveMode) {
            DropdownMenuItem(
                text = { Text("Restore") },
                leadingIcon = { Icon(Icons.Filled.Unarchive, contentDescription = null) },
                onClick = { actions.onUnarchive(note); onDismiss() }
            )
        } else {
            DropdownMenuItem(
                text = { Text("Archive") },
                leadingIcon = { Icon(Icons.Filled.Archive, contentDescription = null) },
                onClick = { actions.onArchive(note); onDismiss() }
            )
        }
        DropdownMenuItem(
            text = { Text("Delete") },
            leadingIcon = { Icon(Icons.Filled.Delete, contentDescription = null) },
            onClick = { actions.onDelete(note); onDismiss() }
        )
    }
}

@Composable
private fun BucketPickerDialog(
    buckets: List<Category>,
    currentBucketId: Long?,
    onDismiss: () -> Unit,
    onSelect: (Long) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Move to bucket") },
        text = {
            LazyColumn {
                items(buckets) { bucket ->
                    DropdownMenuItem(
                        text = { Text(bucket.name.ifEmpty { "Untitled" }) },
                        trailingIcon = {
                            if (bucket.id == currentBucketId) {
                                Icon(Icons.Filled.Star, contentDescription = "Current")
                            }
                        },
                        onClick = { onSelect(bucket.id) }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

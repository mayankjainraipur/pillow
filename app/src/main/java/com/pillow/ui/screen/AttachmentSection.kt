package com.pillow.ui.screen

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.pillow.domain.model.AttachmentType
import com.pillow.presentation.viewmodel.AttachmentViewModel
import java.io.File

/**
 * Horizontal strip of a note's attachments (images as thumbnails, files as chips),
 * including not-yet-saved (pending) ones for a brand-new note. Each item has a
 * remove affordance. Surfaced inside the editor below the body.
 */
@Composable
fun AttachmentSection(
    labelColor: Color = MaterialTheme.colorScheme.onSurface,
    viewModel: AttachmentViewModel = hiltViewModel()
) {
    val attachments by viewModel.attachments.collectAsState()
    val pending by viewModel.pendingAttachments.collectAsState()

    if (attachments.isEmpty() && pending.isEmpty()) return

    Text("Attachments", style = MaterialTheme.typography.labelLarge, color = labelColor)
    Spacer(Modifier.height(8.dp))

    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(attachments) { att ->
            AttachmentTile(
                filePath = att.filePath,
                isImage = att.type == AttachmentType.IMAGE,
                displayName = att.displayName,
                unsaved = false,
                onRemove = { viewModel.deleteAttachment(att.id) }
            )
        }
        items(pending) { p ->
            AttachmentTile(
                filePath = p.filePath,
                isImage = p.type == AttachmentType.IMAGE,
                displayName = p.displayName,
                unsaved = true,
                onRemove = { viewModel.deletePending(p.filePath) }
            )
        }
    }
}

@Composable
private fun AttachmentTile(
    filePath: String,
    isImage: Boolean,
    displayName: String,
    unsaved: Boolean,
    onRemove: () -> Unit
) {
    Box {
        if (isImage) {
            val bitmap = remember(filePath) {
                runCatching { BitmapFactory.decodeFile(filePath)?.asImageBitmap() }.getOrNull()
            }
            Card(
                modifier = Modifier.size(96.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                if (bitmap != null) {
                    Image(
                        bitmap = bitmap,
                        contentDescription = displayName,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxWidth().height(96.dp)
                    )
                } else {
                    Box(Modifier.size(96.dp), contentAlignment = Alignment.Center) {
                        Icon(Icons.Filled.AttachFile, contentDescription = displayName)
                    }
                }
            }
        } else {
            Card(
                modifier = Modifier.size(width = 120.dp, height = 96.dp),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.size(width = 120.dp, height = 96.dp).padding(8.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Filled.AttachFile, contentDescription = null)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = displayName.ifEmpty { "File" },
                        style = MaterialTheme.typography.labelSmall,
                        maxLines = 2
                    )
                }
            }
        }

        IconButton(
            onClick = onRemove,
            modifier = Modifier.align(Alignment.TopEnd).size(24.dp)
        ) {
            Icon(
                Icons.Filled.Cancel,
                contentDescription = "Remove",
                tint = MaterialTheme.colorScheme.error
            )
        }
    }

    if (unsaved) {
        Text(
            "unsaved",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

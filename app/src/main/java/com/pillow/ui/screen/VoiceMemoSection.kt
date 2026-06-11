package com.pillow.ui.screen

import android.Manifest
import android.media.MediaPlayer
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.pillow.presentation.viewmodel.VoiceMemoViewModel

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun VoiceMemoSection(
    noteId: Long,
    viewModel: VoiceMemoViewModel = hiltViewModel(),
    labelColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface
) {
    Text("Voice memos", style = MaterialTheme.typography.labelLarge, color = labelColor)
    Spacer(modifier = Modifier.height(8.dp))

    if (noteId > 0) {
        LaunchedEffect(noteId) { viewModel.setNote(noteId) }
    }

    val memos by viewModel.memos.collectAsState()
    val pendingRecordings by viewModel.pendingRecordings.collectAsState()
    val isRecording by viewModel.isRecording.collectAsState()
    val micPermission = rememberPermissionState(Manifest.permission.RECORD_AUDIO)

    Button(
        onClick = {
            if (micPermission.status.isGranted) {
                if (isRecording) viewModel.stopRecording() else viewModel.startRecording()
            } else {
                micPermission.launchPermissionRequest()
            }
        },
        colors = if (isRecording) {
            ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
        } else {
            ButtonDefaults.buttonColors()
        }
    ) {
        Icon(
            imageVector = if (isRecording) Icons.Filled.Stop else Icons.Filled.Mic,
            contentDescription = null
        )
        Spacer(modifier = Modifier.padding(horizontal = 4.dp))
        Text(if (isRecording) "Stop recording" else "Record")
    }

    if (memos.isNotEmpty()) {
        Spacer(modifier = Modifier.height(8.dp))
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            memos.forEachIndexed { index, memo ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AudioPlayer(filePath = memo.filePath)
                        Text(
                            text = "Memo ${index + 1} · ${formatDuration(memo.durationMs)}",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { viewModel.deleteMemo(memo.id) }) {
                            Icon(Icons.Filled.Delete, contentDescription = "Delete memo")
                        }
                    }
                }
            }
        }
    }

    if (pendingRecordings.isNotEmpty()) {
        Spacer(modifier = Modifier.height(8.dp))
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            pendingRecordings.forEachIndexed { index, recording ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AudioPlayer(filePath = recording.filePath)
                        Text(
                            text = "Memo ${memos.size + index + 1} · ${formatDuration(recording.durationMs)} (unsaved)",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { viewModel.deletePendingMemo(recording.filePath) }) {
                            Icon(Icons.Filled.Delete, contentDescription = "Delete memo")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AudioPlayer(filePath: String) {
    var isPlaying by remember { mutableStateOf(false) }
    val player = remember(filePath) { MediaPlayer() }

    DisposableEffect(filePath) {
        runCatching {
            player.setDataSource(filePath)
            player.prepare()
        }
        player.setOnCompletionListener { isPlaying = false }
        onDispose { runCatching { player.release() } }
    }

    IconButton(onClick = {
        runCatching {
            if (isPlaying) {
                player.pause()
                isPlaying = false
            } else {
                player.start()
                isPlaying = true
            }
        }
    }) {
        Icon(
            imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
            contentDescription = if (isPlaying) "Pause" else "Play"
        )
    }
}

private fun formatDuration(ms: Long): String {
    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%d:%02d".format(minutes, seconds)
}

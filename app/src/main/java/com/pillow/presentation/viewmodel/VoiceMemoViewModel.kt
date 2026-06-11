package com.pillow.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pillow.audio.AudioRecorder
import com.pillow.data.repository.VoiceMemoRepository
import com.pillow.domain.model.VoiceMemo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class VoiceMemoViewModel @Inject constructor(
    private val repository: VoiceMemoRepository,
    private val recorder: AudioRecorder
) : ViewModel() {

    private val noteId = MutableStateFlow(0L)

    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

    data class PendingRecording(val filePath: String, val durationMs: Long)

    private val _pendingRecordings = MutableStateFlow<List<PendingRecording>>(emptyList())
    val pendingRecordings: StateFlow<List<PendingRecording>> = _pendingRecordings.asStateFlow()

    val memos: StateFlow<List<VoiceMemo>> = noteId
        .flatMapLatest { id ->
            if (id > 0) repository.getMemosForNoteFlow(id) else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** Point the view model at the note whose memos should be shown/attached. */
    fun setNote(id: Long) {
        noteId.value = id
    }

    fun startRecording() {
        try {
            recorder.start()
            _isRecording.value = true
        } catch (e: Exception) {
            e.printStackTrace()
            _isRecording.value = false
        }
    }

    fun stopRecording() {
        val result = recorder.stop() ?: run { _isRecording.value = false; return }
        _isRecording.value = false
        val id = noteId.value
        if (id > 0) {
            viewModelScope.launch {
                repository.addMemo(id, result.file.absolutePath, result.durationMs)
            }
        } else {
            _pendingRecordings.value = _pendingRecordings.value +
                PendingRecording(result.file.absolutePath, result.durationMs)
        }
    }

    /** Saves all pending recordings to the DB under the given note ID. Called after a new note is created. */
    fun commitPendingMemos(noteId: Long) {
        val pending = _pendingRecordings.value
        _pendingRecordings.value = emptyList()
        viewModelScope.launch {
            pending.forEach { recording ->
                repository.addMemo(noteId, recording.filePath, recording.durationMs)
            }
        }
    }

    /** Deletes temp files for all pending recordings. Called when leaving a new note without saving. */
    fun discardPendingMemos() {
        val pending = _pendingRecordings.value
        _pendingRecordings.value = emptyList()
        pending.forEach { File(it.filePath).delete() }
    }

    fun deletePendingMemo(filePath: String) {
        _pendingRecordings.value = _pendingRecordings.value.filter { it.filePath != filePath }
        File(filePath).delete()
    }

    fun deleteMemo(memoId: Long) {
        viewModelScope.launch { repository.deleteMemo(memoId) }
    }

    override fun onCleared() {
        super.onCleared()
        if (recorder.isRecording) recorder.stop()
    }
}

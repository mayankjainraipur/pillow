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
        val result = recorder.stop()
        _isRecording.value = false
        val id = noteId.value
        if (result != null && id > 0) {
            viewModelScope.launch {
                repository.addMemo(id, result.file.absolutePath, result.durationMs)
            }
        }
    }

    fun deleteMemo(memoId: Long) {
        viewModelScope.launch { repository.deleteMemo(memoId) }
    }

    override fun onCleared() {
        super.onCleared()
        if (recorder.isRecording) recorder.stop()
    }
}

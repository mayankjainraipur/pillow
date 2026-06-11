package com.pillow.presentation.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pillow.data.repository.AttachmentRepository
import com.pillow.domain.model.Attachment
import com.pillow.domain.model.AttachmentType
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

/**
 * Owns image/file attachments for the editor. Like [VoiceMemoViewModel], it
 * supports attaching to an unsaved note: staged files are queued and committed
 * to the DB once the note is created.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class AttachmentViewModel @Inject constructor(
    private val repository: AttachmentRepository
) : ViewModel() {

    private val noteId = MutableStateFlow(0L)

    data class PendingAttachment(
        val filePath: String,
        val type: String,
        val mimeType: String,
        val displayName: String
    )

    private val _pendingAttachments = MutableStateFlow<List<PendingAttachment>>(emptyList())
    val pendingAttachments: StateFlow<List<PendingAttachment>> = _pendingAttachments.asStateFlow()

    val attachments: StateFlow<List<Attachment>> = noteId
        .flatMapLatest { id ->
            if (id > 0) repository.getAttachmentsForNoteFlow(id) else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun setNote(id: Long) {
        noteId.value = id
    }

    fun addImage(uri: Uri) = add(uri, AttachmentType.IMAGE)
    fun addFile(uri: Uri) = add(uri, AttachmentType.FILE)

    private fun add(uri: Uri, type: String) {
        viewModelScope.launch {
            val staged = repository.stageAttachment(uri, type) ?: return@launch
            val id = noteId.value
            if (id > 0) {
                repository.addAttachment(id, staged)
            } else {
                _pendingAttachments.value = _pendingAttachments.value +
                    PendingAttachment(staged.filePath, staged.type, staged.mimeType, staged.displayName)
            }
        }
    }

    /** Persist queued attachments under a freshly created note. */
    fun commitPending(noteId: Long) {
        val pending = _pendingAttachments.value
        _pendingAttachments.value = emptyList()
        viewModelScope.launch {
            pending.forEach {
                repository.addAttachment(
                    noteId,
                    AttachmentRepository.StagedAttachment(it.filePath, it.type, it.mimeType, it.displayName)
                )
            }
        }
    }

    /** Drop queued attachments (and their files) when leaving a new note without saving. */
    fun discardPending() {
        val pending = _pendingAttachments.value
        _pendingAttachments.value = emptyList()
        pending.forEach { File(it.filePath).delete() }
    }

    fun deletePending(filePath: String) {
        _pendingAttachments.value = _pendingAttachments.value.filter { it.filePath != filePath }
        File(filePath).delete()
    }

    fun deleteAttachment(attachmentId: Long) {
        viewModelScope.launch { repository.deleteAttachment(attachmentId) }
    }
}

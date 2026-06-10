package com.pillow.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pillow.data.repository.CategoryRepository
import com.pillow.data.repository.NoteRepository
import com.pillow.data.repository.TagRepository
import com.pillow.domain.model.Note
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NoteViewModel @Inject constructor(
    private val noteRepository: NoteRepository,
    private val categoryRepository: CategoryRepository,
    private val tagRepository: TagRepository
) : ViewModel() {

    private val _notesState = MutableStateFlow<List<Note>>(emptyList())
    val notesState: StateFlow<List<Note>> = _notesState.asStateFlow()

    private val _currentNoteState = MutableStateFlow<Note?>(null)
    val currentNoteState: StateFlow<Note?> = _currentNoteState.asStateFlow()

    private val _isLoadingState = MutableStateFlow(false)
    val isLoadingState: StateFlow<Boolean> = _isLoadingState.asStateFlow()

    private val _searchQueryState = MutableStateFlow("")
    val searchQueryState: StateFlow<String> = _searchQueryState.asStateFlow()

    init {
        loadAllNotes()
    }

    fun loadAllNotes() {
        viewModelScope.launch {
            _isLoadingState.value = true
            noteRepository.getAllNotesFlow().collectLatest { notes ->
                _notesState.value = notes
                _isLoadingState.value = false
            }
        }
    }

    fun loadPinnedNotes() {
        viewModelScope.launch {
            _isLoadingState.value = true
            noteRepository.getPinnedNotesFlow().collectLatest { notes ->
                _notesState.value = notes
                _isLoadingState.value = false
            }
        }
    }

    fun loadArchivedNotes() {
        viewModelScope.launch {
            _isLoadingState.value = true
            noteRepository.getArchivedNotesFlow().collectLatest { notes ->
                _notesState.value = notes
                _isLoadingState.value = false
            }
        }
    }

    fun searchNotes(query: String) {
        _searchQueryState.value = query
        viewModelScope.launch {
            if (query.isEmpty()) {
                loadAllNotes()
            } else {
                _isLoadingState.value = true
                noteRepository.searchNotesFlow(query).collectLatest { notes ->
                    _notesState.value = notes
                    _isLoadingState.value = false
                }
            }
        }
    }

    private val _categoryNotesState = MutableStateFlow<List<Note>>(emptyList())
    val categoryNotesState: StateFlow<List<Note>> = _categoryNotesState.asStateFlow()

    /** Load the notes that belong to a single bucket (category). */
    fun loadNotesByCategory(categoryId: Long) {
        viewModelScope.launch {
            noteRepository.getNotesByCategoryFlow(categoryId).collectLatest { notes ->
                _categoryNotesState.value = notes
            }
        }
    }

    fun loadNoteById(noteId: Long) {
        viewModelScope.launch {
            _isLoadingState.value = true
            noteRepository.getNoteByIdFlow(noteId).collectLatest { note ->
                _currentNoteState.value = note
                _isLoadingState.value = false
            }
        }
    }

    fun createNote(note: Note) {
        viewModelScope.launch {
            try {
                noteRepository.createNote(note)
                loadAllNotes()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun updateNote(note: Note) {
        viewModelScope.launch {
            try {
                noteRepository.updateNote(note)
                loadAllNotes()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun deleteNote(noteId: Long) {
        viewModelScope.launch {
            try {
                noteRepository.deleteNote(noteId)
                loadAllNotes()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun togglePinnedNote(noteId: Long, currentState: Boolean) {
        viewModelScope.launch {
            try {
                noteRepository.updateNotePinnedStatus(noteId, !currentState)
                loadAllNotes()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun archiveNote(noteId: Long) {
        viewModelScope.launch {
            try {
                noteRepository.updateNoteArchivedStatus(noteId, true)
                loadAllNotes()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun unarchiveNote(noteId: Long) {
        viewModelScope.launch {
            try {
                noteRepository.updateNoteArchivedStatus(noteId, false)
                loadAllNotes()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun deleteAllArchivedNotes() {
        viewModelScope.launch {
            try {
                noteRepository.deleteAllArchivedNotes()
                loadAllNotes()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private val _trashedNotesState = MutableStateFlow<List<Note>>(emptyList())
    val trashedNotesState: StateFlow<List<Note>> = _trashedNotesState.asStateFlow()

    fun loadTrashedNotes() {
        viewModelScope.launch {
            noteRepository.getTrashedNotesFlow().collectLatest { notes ->
                _trashedNotesState.value = notes
            }
        }
    }

    /** Soft-delete: move a note to the Trash instead of deleting it outright. */
    fun trashNote(noteId: Long) {
        viewModelScope.launch {
            try {
                noteRepository.moveNoteToTrash(noteId)
                loadAllNotes()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun restoreNote(noteId: Long) {
        viewModelScope.launch {
            try {
                noteRepository.restoreNoteFromTrash(noteId)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun emptyTrash() {
        viewModelScope.launch {
            try {
                noteRepository.emptyTrash()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}

package com.pillow.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pillow.data.repository.CategoryRepository
import com.pillow.data.repository.NoteRepository
import com.pillow.data.repository.TagRepository
import com.pillow.domain.model.Note
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NoteViewModel @Inject constructor(
    private val noteRepository: NoteRepository,
    private val categoryRepository: CategoryRepository,
    private val tagRepository: TagRepository
) : ViewModel() {

    // The list as loaded from the active data source (all / favorites / pinned /
    // archived / search). Sort and filter are layered on top to produce [notesState].
    private val _baseNotes = MutableStateFlow<List<Note>>(emptyList())

    private val _sortOption = MutableStateFlow(SortOption.LAST_MODIFIED)
    val sortOption: StateFlow<SortOption> = _sortOption.asStateFlow()

    private val _filter = MutableStateFlow(NoteFilter.ALL)
    val filter: StateFlow<NoteFilter> = _filter.asStateFlow()

    // Note ids that have image / any attachments — populated in Phase 2 (attachments).
    // Empty until then, so the image/attachment filters simply show nothing.
    private val _imageNoteIds = MutableStateFlow<Set<Long>>(emptySet())
    private val _attachmentNoteIds = MutableStateFlow<Set<Long>>(emptySet())

    /** The notes actually shown, after applying the current filter and sort. */
    val notesState: StateFlow<List<Note>> = combine(
        _baseNotes, _sortOption, _filter, _imageNoteIds, _attachmentNoteIds
    ) { base, sort, filter, imageIds, attachmentIds ->
        applySort(applyFilter(base, filter, imageIds, attachmentIds), sort)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

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
                _baseNotes.value = notes
                _isLoadingState.value = false
            }
        }
    }

    fun loadFavoriteNotes() {
        viewModelScope.launch {
            _isLoadingState.value = true
            noteRepository.getFavoriteNotesFlow().collectLatest { notes ->
                _baseNotes.value = notes
                _isLoadingState.value = false
            }
        }
    }

    fun loadPinnedNotes() {
        viewModelScope.launch {
            _isLoadingState.value = true
            noteRepository.getPinnedNotesFlow().collectLatest { notes ->
                _baseNotes.value = notes
                _isLoadingState.value = false
            }
        }
    }

    fun loadArchivedNotes() {
        viewModelScope.launch {
            _isLoadingState.value = true
            noteRepository.getArchivedNotesFlow().collectLatest { notes ->
                _baseNotes.value = notes
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
                    _baseNotes.value = notes
                    _isLoadingState.value = false
                }
            }
        }
    }

    /** Change the list ordering. */
    fun setSort(option: SortOption) {
        _sortOption.value = option
    }

    /**
     * Change which notes are shown. [NoteFilter.ARCHIVED] swaps the data source to
     * the archived list; switching back returns to all active notes. The other
     * filters are predicates applied over the active list.
     */
    fun setFilter(newFilter: NoteFilter) {
        val wasArchived = _filter.value == NoteFilter.ARCHIVED
        val isArchived = newFilter == NoteFilter.ARCHIVED
        _filter.value = newFilter
        if (isArchived && !wasArchived) loadArchivedNotes()
        else if (!isArchived && wasArchived) loadAllNotes()
    }

    private fun applyFilter(
        notes: List<Note>,
        filter: NoteFilter,
        imageIds: Set<Long>,
        attachmentIds: Set<Long>
    ): List<Note> = when (filter) {
        // Base source is already correct for these two.
        NoteFilter.ALL, NoteFilter.ARCHIVED -> notes
        NoteFilter.WITH_IMAGES -> notes.filter { it.id in imageIds }
        NoteFilter.WITH_ATTACHMENTS -> notes.filter { it.id in attachmentIds }
        NoteFilter.CHECKLIST -> notes.filter { hasChecklist(it.content) }
        NoteFilter.SHARED -> notes.filter { it.isShared }
    }

    private fun applySort(notes: List<Note>, sort: SortOption): List<Note> = when (sort) {
        SortOption.LAST_MODIFIED -> notes.sortedByDescending { it.updatedAt }
        SortOption.DATE_CREATED -> notes.sortedByDescending { it.createdAt }
        SortOption.ALPHA_AZ -> notes.sortedBy { it.title.lowercase() }
        SortOption.ALPHA_ZA -> notes.sortedByDescending { it.title.lowercase() }
        SortOption.PINNED_FIRST -> notes.sortedWith(
            compareByDescending<Note> { it.isPinned }.thenByDescending { it.updatedAt }
        )
    }

    /** Markdown task-list markers, e.g. "- [ ] item" or "* [x] done". */
    private fun hasChecklist(content: String): Boolean =
        CHECKLIST_REGEX.containsMatchIn(content)

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

    private val _savedNoteIdState = MutableStateFlow<Long?>(null)
    val savedNoteIdState: StateFlow<Long?> = _savedNoteIdState.asStateFlow()

    /** Creates a note and emits the new DB ID via [savedNoteIdState] for callers that need it. */
    fun createNoteForEditor(note: Note) {
        viewModelScope.launch {
            try {
                val id = noteRepository.createNote(note)
                loadAllNotes()
                _savedNoteIdState.value = id
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun consumeSavedNoteId() {
        _savedNoteIdState.value = null
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
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun toggleFavoriteNote(noteId: Long, currentState: Boolean) {
        viewModelScope.launch {
            try {
                noteRepository.updateNoteFavoriteStatus(noteId, !currentState)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /** Flag a note as shared (called after the share sheet is launched) so the Shared filter works. */
    fun markShared(noteId: Long) {
        viewModelScope.launch {
            try {
                noteRepository.updateNoteSharedStatus(noteId, true)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun moveNoteToBucket(noteId: Long, categoryId: Long?) {
        viewModelScope.launch {
            try {
                noteRepository.moveNoteToBucket(noteId, categoryId)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun duplicateNote(noteId: Long) {
        viewModelScope.launch {
            try {
                noteRepository.duplicateNote(noteId)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun archiveNote(noteId: Long) {
        viewModelScope.launch {
            try {
                noteRepository.updateNoteArchivedStatus(noteId, true)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun unarchiveNote(noteId: Long) {
        viewModelScope.launch {
            try {
                noteRepository.updateNoteArchivedStatus(noteId, false)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun deleteAllArchivedNotes() {
        viewModelScope.launch {
            try {
                noteRepository.deleteAllArchivedNotes()
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

    companion object {
        private val CHECKLIST_REGEX = Regex("""(?m)^\s*[-*]\s+\[[ xX]]""")
    }
}

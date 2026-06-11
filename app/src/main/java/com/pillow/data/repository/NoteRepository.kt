package com.pillow.data.repository

import com.pillow.data.db.dao.NoteDao
import com.pillow.data.db.dao.TagDao
import com.pillow.data.db.entity.NoteEntity
import com.pillow.domain.model.Note
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class NoteRepository @Inject constructor(
    private val noteDao: NoteDao,
    private val tagDao: TagDao
) {
    suspend fun createNote(note: Note): Long {
        val entity = noteToEntity(note)
        val noteId = noteDao.insertNote(entity)
        if (note.tags.isNotEmpty()) {
            tagDao.updateNoteTags(noteId, note.tags.map { it.id })
        }
        return noteId
    }

    suspend fun updateNote(note: Note) {
        val entity = noteToEntity(note)
        noteDao.updateNote(entity)
        tagDao.updateNoteTags(note.id, note.tags.map { it.id })
    }

    suspend fun deleteNote(noteId: Long) {
        val note = noteDao.getNoteById(noteId)
        if (note != null) {
            noteDao.deleteNote(note)
        }
    }

    suspend fun getNoteById(noteId: Long): Note? {
        val entity = noteDao.getNoteById(noteId) ?: return null
        val tags = tagDao.getTagsForNote(noteId)
        return entityToNote(entity, tags.map { com.pillow.domain.model.Tag(it.id, it.name) })
    }

    fun getNoteByIdFlow(noteId: Long): Flow<Note?> {
        return noteDao.getNoteByIdFlow(noteId).map { entity ->
            entity?.let {
                val tags = tagDao.getTagsForNote(noteId)
                entityToNote(it, tags.map { tag -> com.pillow.domain.model.Tag(tag.id, tag.name) })
            }
        }
    }

    fun getAllNotesFlow(): Flow<List<Note>> {
        return noteDao.getAllNotesFlow().map { entities ->
            entities.map { entity ->
                val tags = tagDao.getTagsForNote(entity.id)
                entityToNote(entity, tags.map { tag -> com.pillow.domain.model.Tag(tag.id, tag.name) })
            }
        }
    }

    fun getTrashedNotesFlow(): Flow<List<Note>> {
        return noteDao.getTrashedNotesFlow().map { entities ->
            entities.map { entity ->
                val tags = tagDao.getTagsForNote(entity.id)
                entityToNote(entity, tags.map { tag -> com.pillow.domain.model.Tag(tag.id, tag.name) })
            }
        }
    }

    fun getArchivedNotesFlow(): Flow<List<Note>> {
        return noteDao.getArchivedNotesFlow().map { entities ->
            entities.map { entity ->
                val tags = tagDao.getTagsForNote(entity.id)
                entityToNote(entity, tags.map { tag -> com.pillow.domain.model.Tag(tag.id, tag.name) })
            }
        }
    }

    fun getFavoriteNotesFlow(): Flow<List<Note>> {
        return noteDao.getFavoriteNotesFlow().map { entities ->
            entities.map { entity ->
                val tags = tagDao.getTagsForNote(entity.id)
                entityToNote(entity, tags.map { tag -> com.pillow.domain.model.Tag(tag.id, tag.name) })
            }
        }
    }

    fun getPinnedNotesFlow(): Flow<List<Note>> {
        return noteDao.getPinnedNotesFlow().map { entities ->
            entities.map { entity ->
                val tags = tagDao.getTagsForNote(entity.id)
                entityToNote(entity, tags.map { tag -> com.pillow.domain.model.Tag(tag.id, tag.name) })
            }
        }
    }

    fun searchNotesFlow(query: String): Flow<List<Note>> {
        return noteDao.searchNotesFlow(query).map { entities ->
            entities.map { entity ->
                val tags = tagDao.getTagsForNote(entity.id)
                entityToNote(entity, tags.map { tag -> com.pillow.domain.model.Tag(tag.id, tag.name) })
            }
        }
    }

    fun getNotesByCategoryFlow(categoryId: Long): Flow<List<Note>> {
        return noteDao.getNotesByCategoryFlow(categoryId).map { entities ->
            entities.map { entity ->
                val tags = tagDao.getTagsForNote(entity.id)
                entityToNote(entity, tags.map { tag -> com.pillow.domain.model.Tag(tag.id, tag.name) })
            }
        }
    }

    suspend fun updateNotePinnedStatus(noteId: Long, pinned: Boolean) {
        noteDao.updateNotePinnedStatus(noteId, pinned)
    }

    suspend fun updateNoteArchivedStatus(noteId: Long, archived: Boolean) {
        noteDao.updateNoteArchivedStatus(noteId, archived)
    }

    suspend fun updateNoteFavoriteStatus(noteId: Long, favorite: Boolean) {
        noteDao.updateNoteFavoriteStatus(noteId, favorite)
    }

    suspend fun updateNoteSharedStatus(noteId: Long, shared: Boolean) {
        noteDao.updateNoteSharedStatus(noteId, shared)
    }

    suspend fun moveNoteToBucket(noteId: Long, categoryId: Long?) {
        noteDao.updateNoteCategory(noteId, categoryId)
    }

    /** Insert a copy of an existing note (new id, fresh timestamps, "(copy)" suffix). */
    suspend fun duplicateNote(noteId: Long): Long {
        val original = noteDao.getNoteById(noteId) ?: return -1
        val now = System.currentTimeMillis()
        val copy = original.copy(
            id = 0,
            title = (original.title.ifEmpty { "Untitled" }) + " (copy)",
            createdAt = now,
            updatedAt = now,
            isShared = false
        )
        return noteDao.insertNote(copy)
    }

    /** Soft-delete: move a note to the Trash, stamping the time it was trashed. */
    suspend fun moveNoteToTrash(noteId: Long) {
        noteDao.updateNoteDeletedStatus(noteId, deleted = true, deletedAt = System.currentTimeMillis())
    }

    /** Restore a trashed note back to the active list. */
    suspend fun restoreNoteFromTrash(noteId: Long) {
        noteDao.updateNoteDeletedStatus(noteId, deleted = false, deletedAt = null)
    }

    suspend fun deleteAllArchivedNotes() {
        noteDao.deleteAllArchivedNotes()
    }

    suspend fun emptyTrash() {
        noteDao.deleteAllTrashedNotes()
    }

    private fun noteToEntity(note: Note): NoteEntity {
        return NoteEntity(
            id = note.id,
            title = note.title,
            content = note.content,
            createdAt = note.createdAt,
            updatedAt = note.updatedAt,
            isPinned = note.isPinned,
            categoryId = note.categoryId,
            color = note.color,
            isArchived = note.isArchived,
            isFavorite = note.isFavorite,
            isShared = note.isShared,
            isDeleted = note.isDeleted,
            deletedAt = note.deletedAt
        )
    }

    private fun entityToNote(entity: NoteEntity, tags: List<com.pillow.domain.model.Tag>): Note {
        return Note(
            id = entity.id,
            title = entity.title,
            content = entity.content,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt,
            isPinned = entity.isPinned,
            categoryId = entity.categoryId,
            color = entity.color,
            isArchived = entity.isArchived,
            isFavorite = entity.isFavorite,
            isShared = entity.isShared,
            isDeleted = entity.isDeleted,
            deletedAt = entity.deletedAt,
            tags = tags
        )
    }
}

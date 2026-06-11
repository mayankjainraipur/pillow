package com.pillow.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.pillow.data.db.entity.NoteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: NoteEntity): Long

    @Update
    suspend fun updateNote(note: NoteEntity)

    @Delete
    suspend fun deleteNote(note: NoteEntity)

    @Query("SELECT * FROM notes WHERE id = :noteId")
    suspend fun getNoteById(noteId: Long): NoteEntity?

    @Query("SELECT * FROM notes WHERE id = :noteId")
    fun getNoteByIdFlow(noteId: Long): Flow<NoteEntity?>

    @Query("SELECT * FROM notes WHERE is_archived = 0 AND is_deleted = 0 ORDER BY is_pinned DESC, updated_at DESC")
    fun getAllNotesFlow(): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE is_archived = 1 AND is_deleted = 0 ORDER BY updated_at DESC")
    fun getArchivedNotesFlow(): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE is_pinned = 1 AND is_archived = 0 AND is_deleted = 0 ORDER BY updated_at DESC")
    fun getPinnedNotesFlow(): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE is_favorite = 1 AND is_archived = 0 AND is_deleted = 0 ORDER BY is_pinned DESC, updated_at DESC")
    fun getFavoriteNotesFlow(): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE is_deleted = 1 ORDER BY deleted_at DESC")
    fun getTrashedNotesFlow(): Flow<List<NoteEntity>>

    @Query("""
        SELECT * FROM notes
        WHERE is_archived = 0 AND is_deleted = 0
        AND (title LIKE '%' || :query || '%' OR content LIKE '%' || :query || '%')
        ORDER BY is_pinned DESC, updated_at DESC
    """)
    fun searchNotesFlow(query: String): Flow<List<NoteEntity>>

    @Query("""
        SELECT * FROM notes
        WHERE is_archived = 0 AND is_deleted = 0
        AND category_id = :categoryId
        ORDER BY is_pinned DESC, updated_at DESC
    """)
    fun getNotesByCategoryFlow(categoryId: Long): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE is_archived = 0 AND is_deleted = 0")
    suspend fun getAllNotes(): List<NoteEntity>

    /** Every note regardless of archived/trashed state — used for full backups. */
    @Query("SELECT * FROM notes")
    suspend fun getAllNotesRaw(): List<NoteEntity>

    @Query("UPDATE notes SET is_pinned = :pinned WHERE id = :noteId")
    suspend fun updateNotePinnedStatus(noteId: Long, pinned: Boolean)

    @Query("UPDATE notes SET is_archived = :archived WHERE id = :noteId")
    suspend fun updateNoteArchivedStatus(noteId: Long, archived: Boolean)

    @Query("UPDATE notes SET is_favorite = :favorite WHERE id = :noteId")
    suspend fun updateNoteFavoriteStatus(noteId: Long, favorite: Boolean)

    @Query("UPDATE notes SET is_shared = :shared WHERE id = :noteId")
    suspend fun updateNoteSharedStatus(noteId: Long, shared: Boolean)

    @Query("UPDATE notes SET category_id = :categoryId WHERE id = :noteId")
    suspend fun updateNoteCategory(noteId: Long, categoryId: Long?)

    @Query("UPDATE notes SET is_deleted = :deleted, deleted_at = :deletedAt WHERE id = :noteId")
    suspend fun updateNoteDeletedStatus(noteId: Long, deleted: Boolean, deletedAt: Long?)

    @Query("DELETE FROM notes WHERE is_archived = 1")
    suspend fun deleteAllArchivedNotes()

    @Query("DELETE FROM notes WHERE is_deleted = 1")
    suspend fun deleteAllTrashedNotes()
}

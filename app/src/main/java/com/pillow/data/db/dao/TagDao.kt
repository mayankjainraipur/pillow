package com.pillow.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.pillow.data.db.entity.NoteTagCrossRef
import com.pillow.data.db.entity.TagEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TagDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTag(tag: TagEntity): Long

    @Delete
    suspend fun deleteTag(tag: TagEntity)

    @Query("SELECT * FROM tags ORDER BY name ASC")
    fun getAllTagsFlow(): Flow<List<TagEntity>>

    @Query("SELECT * FROM tags ORDER BY name ASC")
    suspend fun getAllTags(): List<TagEntity>

    @Query("SELECT * FROM tags WHERE id = :tagId")
    suspend fun getTagById(tagId: Long): TagEntity?

    @Query("SELECT DISTINCT t.* FROM tags t INNER JOIN note_tags nt ON t.id = nt.tag_id WHERE nt.note_id = :noteId")
    fun getTagsForNoteFlow(noteId: Long): Flow<List<TagEntity>>

    @Query("SELECT DISTINCT t.* FROM tags t INNER JOIN note_tags nt ON t.id = nt.tag_id WHERE nt.note_id = :noteId")
    suspend fun getTagsForNote(noteId: Long): List<TagEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNoteTagRelation(relation: NoteTagCrossRef)

    @Query("DELETE FROM note_tags WHERE note_id = :noteId AND tag_id = :tagId")
    suspend fun removeTagFromNote(noteId: Long, tagId: Long)

    @Query("DELETE FROM note_tags WHERE note_id = :noteId")
    suspend fun removeAllTagsFromNote(noteId: Long)

    @Transaction
    suspend fun updateNoteTags(noteId: Long, tagIds: List<Long>) {
        removeAllTagsFromNote(noteId)
        tagIds.forEach { tagId ->
            insertNoteTagRelation(NoteTagCrossRef(noteId, tagId))
        }
    }

    @Query("DELETE FROM tags WHERE id NOT IN (SELECT DISTINCT tag_id FROM note_tags)")
    suspend fun deleteUnusedTags()
}

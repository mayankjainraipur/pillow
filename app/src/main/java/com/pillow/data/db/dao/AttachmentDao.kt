package com.pillow.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.pillow.data.db.entity.AttachmentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AttachmentDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(attachment: AttachmentEntity): Long

    @Query("SELECT * FROM attachments WHERE note_id = :noteId ORDER BY created_at ASC")
    fun getAttachmentsForNoteFlow(noteId: Long): Flow<List<AttachmentEntity>>

    @Query("SELECT * FROM attachments WHERE id = :attachmentId")
    suspend fun getAttachmentById(attachmentId: Long): AttachmentEntity?

    @Query("DELETE FROM attachments WHERE id = :attachmentId")
    suspend fun deleteById(attachmentId: Long)

    /** Note ids that have at least one attachment of the given type — powers the list filters. */
    @Query("SELECT DISTINCT note_id FROM attachments WHERE type = :type")
    fun getNoteIdsWithAttachmentTypeFlow(type: String): Flow<List<Long>>

    /** Note ids that have any attachment at all. */
    @Query("SELECT DISTINCT note_id FROM attachments")
    fun getNoteIdsWithAttachmentsFlow(): Flow<List<Long>>
}

package com.pillow.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.pillow.data.db.entity.VoiceMemoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface VoiceMemoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(memo: VoiceMemoEntity): Long

    @Query("SELECT * FROM voice_memos WHERE note_id = :noteId ORDER BY created_at ASC")
    fun getMemosForNoteFlow(noteId: Long): Flow<List<VoiceMemoEntity>>

    @Query("SELECT * FROM voice_memos WHERE id = :memoId")
    suspend fun getMemoById(memoId: Long): VoiceMemoEntity?

    @Query("DELETE FROM voice_memos WHERE id = :memoId")
    suspend fun deleteById(memoId: Long)
}

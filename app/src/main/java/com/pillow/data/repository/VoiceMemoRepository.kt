package com.pillow.data.repository

import com.pillow.data.db.dao.VoiceMemoDao
import com.pillow.data.db.entity.VoiceMemoEntity
import com.pillow.domain.model.VoiceMemo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.io.File
import javax.inject.Inject

class VoiceMemoRepository @Inject constructor(
    private val voiceMemoDao: VoiceMemoDao
) {
    fun getMemosForNoteFlow(noteId: Long): Flow<List<VoiceMemo>> =
        voiceMemoDao.getMemosForNoteFlow(noteId).map { entities ->
            entities.map { it.toDomain() }
        }

    suspend fun addMemo(noteId: Long, filePath: String, durationMs: Long): Long =
        voiceMemoDao.insert(
            VoiceMemoEntity(noteId = noteId, filePath = filePath, durationMs = durationMs)
        )

    /** Deletes the memo's DB row and its backing audio file from disk. */
    suspend fun deleteMemo(memoId: Long) {
        val memo = voiceMemoDao.getMemoById(memoId) ?: return
        runCatching { File(memo.filePath).delete() }
        voiceMemoDao.deleteById(memoId)
    }

    private fun VoiceMemoEntity.toDomain() = VoiceMemo(
        id = id,
        noteId = noteId,
        filePath = filePath,
        durationMs = durationMs,
        createdAt = createdAt
    )
}

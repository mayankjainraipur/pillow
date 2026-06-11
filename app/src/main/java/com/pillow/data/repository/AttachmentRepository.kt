package com.pillow.data.repository

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.pillow.data.db.dao.AttachmentDao
import com.pillow.data.db.entity.AttachmentEntity
import com.pillow.domain.model.Attachment
import com.pillow.domain.model.AttachmentType
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

/**
 * Stores note attachments. Picked content (from the photo / document picker) is
 * copied into the app's private `attachments/` directory so the note keeps a
 * stable copy even if the original is moved or its permission is revoked.
 * Mirrors [VoiceMemoRepository].
 */
class AttachmentRepository @Inject constructor(
    private val attachmentDao: AttachmentDao,
    @ApplicationContext private val context: Context
) {
    /** Metadata for a file that has been copied into app storage but not yet linked to a note. */
    data class StagedAttachment(
        val filePath: String,
        val type: String,
        val mimeType: String,
        val displayName: String
    )

    fun getAttachmentsForNoteFlow(noteId: Long): Flow<List<Attachment>> =
        attachmentDao.getAttachmentsForNoteFlow(noteId).map { entities ->
            entities.map { it.toDomain() }
        }

    fun getImageNoteIdsFlow(): Flow<List<Long>> =
        attachmentDao.getNoteIdsWithAttachmentTypeFlow(AttachmentType.IMAGE)

    fun getAttachmentNoteIdsFlow(): Flow<List<Long>> =
        attachmentDao.getNoteIdsWithAttachmentsFlow()

    /** Copies the picked content into private storage and returns its metadata. */
    suspend fun stageAttachment(uri: Uri, type: String): StagedAttachment? = withContext(Dispatchers.IO) {
        val resolver = context.contentResolver
        val mimeType = resolver.getType(uri) ?: ""
        val displayName = queryDisplayName(uri) ?: "attachment"
        val dir = File(context.filesDir, "attachments").apply { mkdirs() }
        val dest = File(dir, "att_${System.currentTimeMillis()}_$displayName")
        val ok = runCatching {
            resolver.openInputStream(uri)?.use { input ->
                dest.outputStream().use { output -> input.copyTo(output) }
            } != null
        }.getOrDefault(false)
        if (!ok) return@withContext null
        StagedAttachment(dest.absolutePath, type, mimeType, displayName)
    }

    suspend fun addAttachment(noteId: Long, staged: StagedAttachment): Long =
        attachmentDao.insert(
            AttachmentEntity(
                noteId = noteId,
                filePath = staged.filePath,
                type = staged.type,
                mimeType = staged.mimeType,
                displayName = staged.displayName
            )
        )

    /** Deletes the attachment's DB row and its backing file from disk. */
    suspend fun deleteAttachment(attachmentId: Long) {
        val attachment = attachmentDao.getAttachmentById(attachmentId) ?: return
        runCatching { File(attachment.filePath).delete() }
        attachmentDao.deleteById(attachmentId)
    }

    private fun queryDisplayName(uri: Uri): String? = runCatching {
        context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
            ?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val idx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (idx >= 0) cursor.getString(idx)?.replace(Regex("[^A-Za-z0-9._-]"), "_") else null
                } else null
            }
    }.getOrNull()

    private fun AttachmentEntity.toDomain() = Attachment(
        id = id,
        noteId = noteId,
        filePath = filePath,
        type = type,
        mimeType = mimeType,
        displayName = displayName,
        createdAt = createdAt
    )
}

package com.pillow.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * A file attached to a note — either an image (rendered as a thumbnail in the
 * editor) or a generic file (shown as a chip). Mirrors [VoiceMemoEntity]: the
 * picked content is copied into the app's private storage and the row holds the
 * absolute path. Cascade-deletes with its owning note.
 */
@Entity(
    tableName = "attachments",
    foreignKeys = [
        ForeignKey(
            entity = NoteEntity::class,
            parentColumns = ["id"],
            childColumns = ["note_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("note_id")]
)
data class AttachmentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "note_id")
    val noteId: Long,

    /** Absolute path to the copied file in the app's private storage. */
    @ColumnInfo(name = "file_path")
    val filePath: String,

    /** Either "image" or "file". */
    @ColumnInfo(name = "type")
    val type: String,

    @ColumnInfo(name = "mime_type")
    val mimeType: String = "",

    @ColumnInfo(name = "display_name")
    val displayName: String = "",

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)

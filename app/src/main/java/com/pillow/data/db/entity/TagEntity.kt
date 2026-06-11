package com.pillow.data.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo

@Entity(tableName = "tags")
data class TagEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    @ColumnInfo(name = "name")
    val name: String
)

@Entity(
    tableName = "note_tags",
    primaryKeys = ["note_id", "tag_id"],
    indices = [Index("tag_id")],
    foreignKeys = [
        androidx.room.ForeignKey(
            entity = NoteEntity::class,
            parentColumns = ["id"],
            childColumns = ["note_id"],
            onDelete = androidx.room.ForeignKey.CASCADE
        ),
        androidx.room.ForeignKey(
            entity = TagEntity::class,
            parentColumns = ["id"],
            childColumns = ["tag_id"],
            onDelete = androidx.room.ForeignKey.CASCADE
        )
    ]
)
data class NoteTagCrossRef(
    @ColumnInfo(name = "note_id")
    val noteId: Long,
    
    @ColumnInfo(name = "tag_id")
    val tagId: Long
)

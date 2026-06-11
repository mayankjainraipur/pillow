package com.pillow.domain.model

data class Note(
    val id: Long = 0,
    val title: String = "",
    val content: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isPinned: Boolean = false,
    val categoryId: Long? = null,
    val color: String = "#FFFFFF",
    val isArchived: Boolean = false,
    val isFavorite: Boolean = false,
    val isShared: Boolean = false,
    val isDeleted: Boolean = false,
    val deletedAt: Long? = null,
    val tags: List<Tag> = emptyList()
)

data class Category(
    val id: Long = 0,
    val name: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val color: String = "#FF6B6B",
    val isDefault: Boolean = false
)

data class Tag(
    val id: Long = 0,
    val name: String = ""
)

data class VoiceMemo(
    val id: Long = 0,
    val noteId: Long,
    val filePath: String,
    val durationMs: Long = 0,
    val createdAt: Long = System.currentTimeMillis()
)

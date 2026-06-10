package com.pillow.data.repository

import com.pillow.data.db.dao.TagDao
import com.pillow.data.db.entity.TagEntity
import com.pillow.domain.model.Tag
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class TagRepository @Inject constructor(
    private val tagDao: TagDao
) {
    suspend fun createTag(tag: Tag): Long {
        val entity = tagToEntity(tag)
        return tagDao.insertTag(entity)
    }

    suspend fun deleteTag(tagId: Long) {
        val tag = tagDao.getTagById(tagId)
        if (tag != null) {
            tagDao.deleteTag(tag)
        }
    }

    suspend fun getTagById(tagId: Long): Tag? {
        val entity = tagDao.getTagById(tagId)
        return entity?.let { entityToTag(it) }
    }

    fun getAllTagsFlow(): Flow<List<Tag>> {
        return tagDao.getAllTagsFlow().map { entities ->
            entities.map { entityToTag(it) }
        }
    }

    suspend fun getAllTags(): List<Tag> {
        return tagDao.getAllTags().map { entityToTag(it) }
    }

    fun getTagsForNoteFlow(noteId: Long): Flow<List<Tag>> {
        return tagDao.getTagsForNoteFlow(noteId).map { entities ->
            entities.map { entityToTag(it) }
        }
    }

    suspend fun getTagsForNote(noteId: Long): List<Tag> {
        return tagDao.getTagsForNote(noteId).map { entityToTag(it) }
    }

    suspend fun deleteUnusedTags() {
        tagDao.deleteUnusedTags()
    }

    private fun tagToEntity(tag: Tag): TagEntity {
        return TagEntity(
            id = tag.id,
            name = tag.name
        )
    }

    private fun entityToTag(entity: TagEntity): Tag {
        return Tag(
            id = entity.id,
            name = entity.name
        )
    }
}

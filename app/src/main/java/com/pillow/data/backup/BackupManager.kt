package com.pillow.data.backup

import com.pillow.data.db.dao.CategoryDao
import com.pillow.data.db.dao.NoteDao
import com.pillow.data.db.entity.CategoryEntity
import com.pillow.data.db.entity.NoteEntity
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject

/**
 * Serializes all notes and buckets (categories) to/from a versioned JSON document.
 * Uses Android's bundled [org.json] so no extra dependency is required.
 *
 * Restore uses a MERGE strategy: rows are inserted with fresh auto-generated ids
 * (never overwriting existing data), and each note's bucket reference is remapped
 * from the old category id to the newly inserted one.
 */
class BackupManager @Inject constructor(
    private val noteDao: NoteDao,
    private val categoryDao: CategoryDao
) {
    companion object {
        const val BACKUP_VERSION = 1
    }

    suspend fun exportToJson(): String {
        val root = JSONObject()
        root.put("version", BACKUP_VERSION)

        val bucketsArray = JSONArray()
        for (c in categoryDao.getAllCategories()) {
            bucketsArray.put(
                JSONObject().apply {
                    put("id", c.id)
                    put("name", c.name)
                    put("createdAt", c.createdAt)
                    put("color", c.color)
                }
            )
        }
        root.put("buckets", bucketsArray)

        val notesArray = JSONArray()
        for (n in noteDao.getAllNotesRaw()) {
            notesArray.put(
                JSONObject().apply {
                    put("title", n.title)
                    put("content", n.content)
                    put("createdAt", n.createdAt)
                    put("updatedAt", n.updatedAt)
                    put("isPinned", n.isPinned)
                    put("categoryId", n.categoryId ?: JSONObject.NULL)
                    put("color", n.color)
                    put("isArchived", n.isArchived)
                    put("isDeleted", n.isDeleted)
                    put("deletedAt", n.deletedAt ?: JSONObject.NULL)
                }
            )
        }
        root.put("notes", notesArray)

        return root.toString(2)
    }

    /** Returns the number of notes imported. Throws on malformed JSON. */
    suspend fun importFromJson(json: String): Int {
        val root = JSONObject(json)

        // Insert buckets first, building an old-id -> new-id map.
        val idMap = HashMap<Long, Long>()
        val buckets = root.optJSONArray("buckets") ?: JSONArray()
        for (i in 0 until buckets.length()) {
            val b = buckets.getJSONObject(i)
            val oldId = b.optLong("id", 0L)
            val newId = categoryDao.insertCategory(
                CategoryEntity(
                    id = 0,
                    name = b.optString("name", ""),
                    createdAt = b.optLong("createdAt", System.currentTimeMillis()),
                    color = b.optString("color", "#FF6B6B")
                )
            )
            if (oldId != 0L) idMap[oldId] = newId
        }

        val notes = root.optJSONArray("notes") ?: JSONArray()
        for (i in 0 until notes.length()) {
            val n = notes.getJSONObject(i)
            val oldCategoryId = if (n.isNull("categoryId")) null else n.optLong("categoryId")
            val remappedCategoryId = oldCategoryId?.let { idMap[it] }
            noteDao.insertNote(
                NoteEntity(
                    id = 0,
                    title = n.optString("title", ""),
                    content = n.optString("content", ""),
                    createdAt = n.optLong("createdAt", System.currentTimeMillis()),
                    updatedAt = n.optLong("updatedAt", System.currentTimeMillis()),
                    isPinned = n.optBoolean("isPinned", false),
                    categoryId = remappedCategoryId,
                    color = n.optString("color", "#FFFFFF"),
                    isArchived = n.optBoolean("isArchived", false),
                    isDeleted = n.optBoolean("isDeleted", false),
                    deletedAt = if (n.isNull("deletedAt")) null else n.optLong("deletedAt")
                )
            )
        }
        return notes.length()
    }
}

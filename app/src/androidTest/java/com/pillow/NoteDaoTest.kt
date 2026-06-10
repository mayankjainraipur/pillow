package com.pillow

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.pillow.data.db.PillowDatabase
import com.pillow.data.db.dao.NoteDao
import com.pillow.data.db.entity.NoteEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NoteDaoTest {

    private lateinit var db: PillowDatabase
    private lateinit var noteDao: NoteDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, PillowDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        noteDao = db.noteDao()
    }

    @After
    fun teardown() {
        db.close()
    }

    @Test
    fun insertedNote_appearsInActiveList() = runBlocking {
        noteDao.insertNote(NoteEntity(title = "Hello", content = "World"))

        val active = noteDao.getAllNotesFlow().first()

        assertEquals(1, active.size)
        assertEquals("Hello", active[0].title)
    }

    @Test
    fun trashingNote_movesItOutOfActiveListAndIntoTrash() = runBlocking {
        val id = noteDao.insertNote(NoteEntity(title = "Trash me"))

        noteDao.updateNoteDeletedStatus(id, deleted = true, deletedAt = 123L)

        assertTrue(noteDao.getAllNotesFlow().first().isEmpty())
        assertEquals(1, noteDao.getTrashedNotesFlow().first().size)
    }

    @Test
    fun restoringNote_returnsItToActiveList() = runBlocking {
        val id = noteDao.insertNote(NoteEntity(title = "Back from trash"))
        noteDao.updateNoteDeletedStatus(id, deleted = true, deletedAt = 123L)

        noteDao.updateNoteDeletedStatus(id, deleted = false, deletedAt = null)

        assertEquals(1, noteDao.getAllNotesFlow().first().size)
        assertTrue(noteDao.getTrashedNotesFlow().first().isEmpty())
    }
}

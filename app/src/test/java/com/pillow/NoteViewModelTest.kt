package com.pillow

import com.pillow.data.repository.CategoryRepository
import com.pillow.data.repository.NoteRepository
import com.pillow.data.repository.TagRepository
import com.pillow.presentation.viewmodel.NoteViewModel
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class NoteViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private val noteRepository: NoteRepository = mockk(relaxed = true)
    private val categoryRepository: CategoryRepository = mockk(relaxed = true)
    private val tagRepository: TagRepository = mockk(relaxed = true)

    @Before
    fun setup() {
        Dispatchers.setMain(dispatcher)
        // The view model collects this flow in its init block.
        every { noteRepository.getAllNotesFlow() } returns flowOf(emptyList())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun trashNote_delegatesToRepository() = runTest(dispatcher) {
        coEvery { noteRepository.moveNoteToTrash(any()) } just Runs
        val viewModel = NoteViewModel(noteRepository, categoryRepository, tagRepository)

        viewModel.trashNote(42L)
        advanceUntilIdle()

        coVerify { noteRepository.moveNoteToTrash(42L) }
    }

    @Test
    fun restoreNote_delegatesToRepository() = runTest(dispatcher) {
        coEvery { noteRepository.restoreNoteFromTrash(any()) } just Runs
        val viewModel = NoteViewModel(noteRepository, categoryRepository, tagRepository)

        viewModel.restoreNote(7L)
        advanceUntilIdle()

        coVerify { noteRepository.restoreNoteFromTrash(7L) }
    }
}

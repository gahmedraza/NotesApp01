package com.raza.notesapp01.data.repository

import android.content.Context
import com.raza.notesapp01.FakeLogger
import com.raza.notesapp01.Logger
import com.raza.notesapp01.data.local.FakeTodoDao
import com.raza.notesapp01.data.local.entity.TodoEntity
import com.raza.notesapp01.data.remote.FakeRemoteDataSource
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock

@OptIn(ExperimentalCoroutinesApi::class)
class TodoRepositoryTest {
    private lateinit var fakeDao: FakeTodoDao
    private lateinit var fakeRemote: FakeRemoteDataSource
    private lateinit var repository: TodoRepository
    private lateinit var context: Context
    private lateinit var fakeLogger: Logger

    @Before
    fun setup() {
        fakeDao = FakeTodoDao()
        context = mock(Context::class.java)
        fakeLogger = FakeLogger()
    }

    @Test
    fun `remote todo is inserted locally`() = runTest {
        val remoteTodo = TodoEntity(id = 1, value = "Remote", lastModified = 100)
        fakeRemote = FakeRemoteDataSource(mutableListOf(remoteTodo))

        repository = TodoRepository(fakeDao, fakeRemote, context, fakeLogger)
        repository.syncFromFirebase()

        val result = fakeDao.getInternalList()
        assertEquals(1, result.size)
        assertEquals("Remote", result[0].value)
    }

    @Test
    fun `local todo is preserved`() = runTest {
        val localTodo = TodoEntity(id = 1, value = "Local", lastModified = 100)
        fakeDao.insertTodo(localTodo)

        fakeRemote = FakeRemoteDataSource(mutableListOf())
        repository = TodoRepository(fakeDao, fakeRemote, context, fakeLogger)

        repository.syncFromFirebase()

        val result = fakeDao.getInternalList()
        assertEquals(1, result.size)
        assertEquals("Local", result[0].value)
    }


}
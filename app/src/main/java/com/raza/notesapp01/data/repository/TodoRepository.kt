package com.raza.notesapp01.data.repository

import android.content.Context
import android.util.Log
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.raza.notesapp01.Logger
import com.raza.notesapp01.TodoSyncWorker
import com.raza.notesapp01.data.local.TodoDao
import com.raza.notesapp01.data.local.entity.TodoEntity
import com.raza.notesapp01.data.remote.FirestoreTodoDataSource
import com.raza.notesapp01.data.remote.TodoRemoteDataSource
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class TodoRepository @Inject constructor(
    private val dao: TodoDao,
    private val remote: TodoRemoteDataSource,
    @ApplicationContext private val context: Context,
    private val logger: Logger
) {

    val todos: Flow<List<TodoEntity>> = dao.getAllTodos()

    suspend fun insert(todo: TodoEntity) {
        todo.lastModified = System.currentTimeMillis()
        dao.insertTodo(todo)
        enqueueSyncWorker()
    }

    suspend fun delete(todo: TodoEntity) {
        todo.isDeleted = true
        todo.lastModified =
            System.currentTimeMillis()

        dao.updateTodo(todo)
        enqueueSyncWorker()
        //uploadAllToFirebase()
    }

    suspend fun update(todo: TodoEntity) {
        todo.lastModified = System.currentTimeMillis()
        dao.updateTodo(todo)
        enqueueSyncWorker()
    }

    suspend fun uploadAllToFirebase() {
        val list = dao.getAllTodos().first()

        remote.uploadTodos(list)
    }

    suspend fun syncFromFirebase() {
        // Fetch remote and local
        val remoteTodos = remote.fetchTodos()
        val localTodos = dao.getAllTodos().first()

        val merged = mutableListOf<TodoEntity>()

        // merge remote with local
        remoteTodos.forEach { remote ->
            val local = localTodos.find { it.id == remote.id }

            if (local == null) {
                //exists only in firebase
                merged.add(remote)
            } else {
                //conflict case
                if (remote.lastModified > local.lastModified) {
                    merged.add(remote)
                    //firebase wins
                } else {
                    merged.add(local)
                    //local wins
                }
            }
        }

        // add local only todos
        localTodos
            .filter { local ->
                remoteTodos.none { remote ->
                    remote.id == local.id
                }
            }
            .forEach { localOnly ->
                merged.add(localOnly)
            }

        // replace local db with merged result
        logger.d("TAG", "todos = ${merged.size}")
        dao.clearAll()
        dao.insertAll(merged)
    }

    suspend fun syncLocalFromRemote() {
        val remoteTodos = remote.fetchTodos()

        dao.clearAll()
        dao.insertAll(remoteTodos)
    }

    private fun enqueueSyncWorker() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val request = OneTimeWorkRequestBuilder<TodoSyncWorker>()
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context)
            .enqueueUniqueWork(
                "todo_sync_work",
                ExistingWorkPolicy.KEEP,
                request
            )
    }

    suspend fun cleanupDeletedTodos(days: Int = 7) {
        val thresholdTime = System.currentTimeMillis() -
                (days * 24 * 60 * 60 * 1000L)
        dao.hardDeleteTodos(thresholdTime)
    }
}
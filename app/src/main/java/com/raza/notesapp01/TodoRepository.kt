package com.raza.notesapp01

import android.content.Context
import android.util.Log
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.forEach
import kotlinx.coroutines.tasks.await

class TodoRepository(
    private val dao: TodoDao,
    private val firestore: FirebaseFirestore,
    private val context: Context
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

        val collection = firestore.collection("todos")

        list.forEach { todo ->
            collection.document(todo.id.toString())
                .set(todo)
        }
    }

    suspend fun syncFromFirebase() {
        val snapshot = firestore.collection("todos")
            .get().await()
        val remoteTodos = snapshot.toObjects(TodoEntity::class.java)

        val localTodos = dao.getAllTodos().first()

        val merged = mutableListOf<TodoEntity>()

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

        Log.d("TAG", "todos = ${merged.size}")

        dao.clearAll()
        dao.insertAll(merged)
    }

    suspend fun syncFromDatabase() {
        val snapshot = firestore.collection("todos")
            .get().await()

        val remoteTodos = snapshot.toObjects(TodoEntity::class.java)

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
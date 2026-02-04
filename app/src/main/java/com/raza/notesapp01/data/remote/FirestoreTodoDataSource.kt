package com.raza.notesapp01.data.remote

import com.google.firebase.firestore.FirebaseFirestore
import com.raza.notesapp01.NoUnitTest
import com.raza.notesapp01.data.local.entity.TodoEntity
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@NoUnitTest
class FirestoreTodoDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) : TodoRemoteDataSource {
    override suspend fun fetchTodos(): List<TodoEntity> {
        val snapshot = firestore.collection("todos")
            .get().await()
        return snapshot.toObjects(TodoEntity::class.java)
    }

    override suspend fun uploadTodos(todos: List<TodoEntity>) {
        val collection = firestore.collection("todos")
        todos.forEach {
            collection.document(it.id.toString()).set(it)
        }
    }
}
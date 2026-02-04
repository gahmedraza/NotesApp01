package com.raza.notesapp01.data.remote

import com.raza.notesapp01.data.local.entity.TodoEntity

interface TodoRemoteDataSource {
    suspend fun fetchTodos(): List<TodoEntity>
    suspend fun uploadTodos(todos: List<TodoEntity>)
}
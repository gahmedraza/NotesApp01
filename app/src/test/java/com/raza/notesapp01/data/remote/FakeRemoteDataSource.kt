package com.raza.notesapp01.data.remote

import com.raza.notesapp01.data.local.entity.TodoEntity

class FakeRemoteDataSource(
    private val fakeTodos: MutableList<TodoEntity> = mutableListOf()
) : TodoRemoteDataSource {

    override suspend fun fetchTodos(): List<TodoEntity> {
        return fakeTodos
    }

    override suspend fun uploadTodos(todos: List<TodoEntity>) {
        fakeTodos.clear()
        fakeTodos.addAll(todos)
    }
}
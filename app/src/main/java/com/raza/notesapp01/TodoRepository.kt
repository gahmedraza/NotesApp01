package com.raza.notesapp01

import kotlinx.coroutines.flow.Flow

class TodoRepository(private val dao: TodoDao) {

     val todos: Flow<List<TodoEntity>> = dao.getAllTodos()

    suspend fun insert(todo: TodoEntity) {
        dao.insertTodo(todo)
    }

    suspend fun delete(todo: TodoEntity) {
        dao.deleteTodo(todo)
    }

    suspend fun update(todo: TodoEntity) {
        dao.updateTodo(todo)
    }
}
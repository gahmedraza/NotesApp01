package com.raza.notesapp01.data.local

import com.raza.notesapp01.data.local.entity.TodoEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class FakeTodoDao : TodoDao {

    private val todos = mutableListOf<TodoEntity>()

    //copy pasted the entire method
    override fun getAllTodos(): Flow<List<TodoEntity>>
    = flow {
        emit(todos.filter { !it.isDeleted })
    }

    override suspend fun insertTodo(todo: TodoEntity) {
        todos.add(todo)
    }

    override suspend fun updateTodo(todo: TodoEntity) {
        //write
    }

    override suspend fun deleteTodo(todo: TodoEntity) {
        todos.removeIf { it.id == todo.id }
    }

    override suspend fun hardDeleteTodos(thresholdTime: Long) {
        // Write
    }

    override suspend fun insertAll(todos: List<TodoEntity>) {
        this.todos.clear()
        this.todos.addAll(todos)
    }

    override suspend fun clearAll() {
        todos.clear()
    }

    fun getInternalList(): List<TodoEntity> = todos

}
package com.raza.notesapp01

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class DashboardViewModel(
    private val repository: TodoRepository
) : ViewModel() {
    var text by mutableStateOf("")

    val todos = repository.todos

    /*mutableStateListOf(
        TodoEntity(1, "Buy milk"),
        TodoEntity(2, "Learn compose"),
        TodoEntity(3, "Walk in the morning")
    )*/

    init {
        viewModelScope.launch {
            repository.syncFromDatabase()
        }
    }

    var editedTodo by mutableStateOf<TodoEntity?>(null)

    fun addOrUpdateTodo() {
        viewModelScope.launch {
            editedTodo?.let { todo ->
                //update
                repository.update(TodoEntity(todo.id, text))
            } ?: run {
                //add
                repository.insert(TodoEntity(value = text))
            }
        }
    }

    fun deleteTodo(todo: TodoEntity) {
        viewModelScope.launch {
            repository.delete(todo)
        }
    }

    fun startEditing(todo: TodoEntity) {
        text = todo.value
        editedTodo = todo
    }

    fun onTextChange(newText: String) {
        text = newText
        Log.d("TAG", " = $newText")
    }
}
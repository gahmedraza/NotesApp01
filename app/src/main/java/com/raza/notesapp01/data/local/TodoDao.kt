package com.raza.notesapp01.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.raza.notesapp01.data.local.entity.TodoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TodoDao {

    @Query(
        "SELECT * FROM todos " +
                "WHERE isDeleted = 0"
    )
    fun getAllTodos(): Flow<List<TodoEntity>>

    @Insert
    suspend fun insertTodo(todo: TodoEntity)

    @Update
    suspend fun updateTodo(todo: TodoEntity)

    @Delete
    suspend fun deleteTodo(todo: TodoEntity)

    @Query(
        "DELETE FROM todos " +
                "WHERE isDeleted=1 AND lastModified < :thresholdTime"
    )
    suspend fun hardDeleteTodos(thresholdTime: Long)

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insertAll(todos: List<TodoEntity>)

    @Query("DELETE FROM todos")
    suspend fun clearAll()
}
package com.raza.notesapp01

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "todos")
data class TodoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    var value: String = "",
    var lastModified: Long = System.currentTimeMillis()
)
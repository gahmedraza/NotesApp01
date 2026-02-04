package com.raza.notesapp01.di

import android.content.Context
import com.google.firebase.firestore.FirebaseFirestore
import com.raza.notesapp01.data.local.TodoDao
import com.raza.notesapp01.data.local.TodoDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    //Firestore
    @Provides
    @Singleton
    fun provideFirestore(): FirebaseFirestore {
        return FirebaseFirestore.getInstance()
    }

    //Database
    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): TodoDatabase {
        return TodoDatabase.getDatabase(context)
    }

    //Dao
    @Provides
    fun provideTodoDao(
        database: TodoDatabase
    ): TodoDao {
        return database.todoDao()
    }
}
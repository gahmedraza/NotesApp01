package com.raza.notesapp01.di

import com.raza.notesapp01.data.remote.FirestoreTodoDataSource
import com.raza.notesapp01.data.remote.TodoRemoteDataSource
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class RemoteDataSourceModule {

    @Binds
    abstract fun bindTodoRemoteDataSource(
        impl: FirestoreTodoDataSource
    ): TodoRemoteDataSource
}
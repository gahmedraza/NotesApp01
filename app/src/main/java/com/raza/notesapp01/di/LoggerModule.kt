package com.raza.notesapp01.di

import com.raza.notesapp01.AndroidLogger
import com.raza.notesapp01.Logger
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class LoggerModule {

    @Binds
    abstract fun bindLogger(
        impl: AndroidLogger
    ): Logger
}
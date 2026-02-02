package com.raza.notesapp01

import android.app.Application
import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequest
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

class TodoApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        val request = OneTimeWorkRequestBuilder<TodoSyncWorker>()
                .build()
        WorkManager.getInstance(this)
            .enqueue(request)

        schedulePeriodicTodoSync(this)
    }

    fun schedulePeriodicTodoSync(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val request = PeriodicWorkRequestBuilder<TodoSyncWorker>(
            6, TimeUnit.HOURS
        )
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(
                "todo_periodic_sync",
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
    }
}
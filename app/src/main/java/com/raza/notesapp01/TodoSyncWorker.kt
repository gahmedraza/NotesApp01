package com.raza.notesapp01

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.firebase.firestore.FirebaseFirestore
import com.raza.notesapp01.data.repository.TodoRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import javax.inject.Inject

@HiltWorker
class TodoSyncWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val repository: TodoRepository
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            repository.uploadAllToFirebase()
            repository.syncFromFirebase()
            repository.cleanupDeletedTodos(7)

            Log.d("TAG", "worker success")
            Result.success()
        } catch (e: Exception) {

            e.printStackTrace()

            Log.d("TAG", "worker exception")
            Result.retry()
        }
    }
}
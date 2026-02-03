package com.raza.notesapp01

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.firebase.firestore.FirebaseFirestore

class TodoSyncWorker(
    private val context: Context,
    workerParams: WorkerParameters,
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val db = TodoDatabase.getDatabase(context)
            val dao = db.todoDao()

            val firestore = FirebaseFirestore.getInstance()

            val repository = TodoRepository(dao, firestore, context)

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
package com.raza.notesapp01

import android.util.Log
import javax.inject.Inject

interface Logger {
    fun d(tag: String, msg: String)
}

class AndroidLogger @Inject constructor() : Logger {
    override fun d(tag: String, msg: String) {
        Log.d(tag, msg)
    }
}
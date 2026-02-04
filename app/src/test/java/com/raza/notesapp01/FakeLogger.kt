package com.raza.notesapp01

class FakeLogger : Logger {
    override fun d(tag: String, msg: String) {
        println("$tag: $msg")
    }
}
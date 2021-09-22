package com.kotlincode

object Unit1 {
    const val radiusInKM = 59000
    fun numberOfProcess() = Runtime.getRuntime().availableProcessors()
}
fun createRunnable3(): Runnable {
    return object : Runnable, AutoCloseable {
        override fun run() {
        }

        override fun close() {
        }
    }
}

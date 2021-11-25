package com.kotlincode.myCoroutine

import kotlinx.coroutines.*

/**
 * 协程自我练习
 */
fun main() {

    runBlocking {

        launch {
            delay(100)
            println("launch!")
        }
        async {
            delay(100)
            println("async!")
        }

        println("Hello") // mai
    }

}

suspend fun test() {
    delay(100)
    println("world!")
}


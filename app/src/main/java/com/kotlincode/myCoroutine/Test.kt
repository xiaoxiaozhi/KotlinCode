package com.kotlincode.myCoroutine

import android.os.SystemClock
import kotlinx.coroutines.*
import kotlin.system.measureTimeMillis

private fun log(message: String) = println("[${Thread.currentThread().name}] $message")
fun main() {
//    runBlocking {
//
//        val async1 = async { // root coroutine with launch
//            repeat(2) { repeat(Int.MAX_VALUE) {} }
//            println("async1")
//        }
//        val async2 = async { // root coroutine with launch
//            println("async2")
//        }
//        println("runBlocking")
//        async2.await()
//        async1.await()
//    }
    runBlocking {
        val launch1 = launch {
            repeat(Int.MAX_VALUE) { }
            println("launch1")
        }
        val launch2 = launch {
            println("launch2")
        }
        println("runBlocking")
        launch2.join()
        println("---------")
        launch1.join()
    }
//    val time = measureTimeMillis {
//        repeat(10) { repeat(Int.MAX_VALUE) {} }
//
//    }
//    println("time----$time")
//    runBlocking {
//        measureTimeMillis {
//            val one = async {
//                repeat(10) { repeat(Int.MAX_VALUE) {} }
//                13
//            }
//            val two = async {
//                repeat(10) { repeat(Int.MAX_VALUE) {} }
//                20
//            }
//            one.await() + two.await()
//        }.also {
//            println("花费时间----$it")
//       }
//
//    }
}




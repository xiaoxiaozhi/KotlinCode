package com.kotlincode.myCoroutine

import kotlinx.coroutines.*

/**
 * 扔物线协程 https://ke.qq.com/course/3452535?taid=11410005827038839
 * 协程 遇见 suspend关键字后会被挂起
 * withContext 线程切走再切回来
 *
 */
fun main() = runBlocking {
    println("1")
//    test1()
    launch {
        withContext(Dispatchers.IO) {
            delay(2000)
            println("2")
        }
    }
    coroutineScope {
        println("4")
    }
    println("3")


}

suspend fun test1() {
    for (i in 1..Int.MAX_VALUE) {
    }
    delay(2000)// 执行到test1的时候协程并未真正挂起，只要执行到协程自带的挂起函数才会真正挂起
    println("2")
}



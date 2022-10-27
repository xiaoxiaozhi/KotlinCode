package com.kotlincode.myCoroutine

import android.os.SystemClock
import com.beust.klaxon.Klaxon
import kotlinx.coroutines.*
import kotlin.system.measureTimeMillis

private fun log(message: String) = println("[${Thread.currentThread().name}] $message")
fun main() {

    runBlocking {
        val launch1 = launch(Dispatchers.Default) {
            repeat(Int.MAX_VALUE) { }
            println("launch1")
        }
        val launch2 = launch() {
            println("launch2")
        }
        val launch3 = launch() {
            println("launch3")
        }
        val launch4 = launch() {
            println("launch4")
        }
        println("runBlocking")
        // ①join挂起当前协程，(但并不阻塞底层线程)也就是runBlocking开启的那个协程。然后线程会按照协程生成顺序执行launch1和launch2。打印结果也可以看到launch1先执行然后launch2
        //   如果launch1 设置了 其它线程 上下文元素 协程会先执行 其他线程上运行的协程
        // ②yield挂起 ，设置了 Dispatch会并行执行，没设置的会根据生成顺序执行。
        launch3.join()// 任意协程的join都可以
//        yield()//
//        launch2.join()
        println("---------")

    }
//    newSingleThreadContext("自定义单线程").use {
//        runBlocking(it) {
//            Util.log("Xiancheng")
//            val async1 = launch (Dispatchers.Default) { // root coroutine with launch
//                repeat(Int.MAX_VALUE) { }
//                Util.log("async1")
//            }
//            val async2 = launch(Dispatchers.Default) { // root coroutine with launch
//                Util.log("async2")
//            }
//            Util.log("runBlocking")
//
//            async2.join()
////        yield()
//
//            Util.log("---------")
//        }
//    }
//    val  s = CoroutineScope(Job())
//    s.launch {
//        Util.log("Xiancheng")
//        val async1 = launch (Dispatchers.Default) { // root coroutine with launch
//            repeat(Int.MAX_VALUE) { }
//            Util.log("async1")
//        }
//        val async2 = launch(Dispatchers.Default) { // root coroutine with launch
//            Util.log("async2")
//        }
//        Util.log("runBlocking")
//
//        async2.join()
////        yield()
//
//        Util.log("---------")
//    }

}

suspend fun massiveRun(action: suspend () -> Unit) {
    val n = 5  // 启动的协程数量
    val k = 1000 // 每个协程重复执行同一动作的次数
    val time = measureTimeMillis {
        coroutineScope { // 协程的作用域
            repeat(n) {
                launch(Dispatchers.Default) {
                    repeat(k) { action() }
                }
            }
        }
    }
    println("Completed ${n * k} actions in $time ms")
}




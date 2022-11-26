package com.kotlincode.asynchronous

import com.kotlincode.myCoroutine.Util
import kotlinx.coroutines.*
import java.util.Date
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * [异步编程技术](https://kotlinlang.org/docs/async-programming.html)
 * 官网介绍了几种异步编程技术，我觉得用kotlin的挂起函数就能很好解决这个问题，现在就挂起函数代替回调函数展开自己总结。(前置技能协程)
 * 1.回调解决异步问题
 *   假设这样一种情况。操作1的结果是操作2的值，操作2的结果又是操作3的值， act3(act2(act1()))如果这不是耗时操作还好说，可是一旦这是耗时操作为了不堵塞主线程就需要回调解决异步问题(例如读取文件，读取网络返回)
 * 2.挂起函数解决异步问题
 *   观察1可以发现当需要很多这样的回调操作时嵌套会让代码难以阅读。协程挂起函数很好的解决了这个问题
 * 3.把回调转成挂起函数
 *   [用挂起函数处理回调，Android官方教程](https://medium.com/androiddevelopers/simplifying-apis-with-coroutines-and-flow-a6fb65338765)
 *   [对官方文档的中文翻译，代码稍微做了更改](https://xuyisheng.top/callback-flow/)
 *   我们可以使用suspendCancellableCoroutine函数，他是一个挂起函数，执行给定的代码块，当Continuation对象中的resume或resumeWithException方法被调用时，外部协程将恢复执行。
 *
 * TODO [在协程中调用网络请求教程](https://kotlinlang.org/docs/coroutines-and-channels.html)
 * TODO 学习了okhttp再回来看suspendCoroutine和suspendCancellableCoroutine和startCoroutine
 *
 */
fun main() {
    //1 回调解决异步问题
    println("1。回调解决异步问题-------")
    val startTime = System.currentTimeMillis()
    act1("任意输入作为act1的参数") { it1 ->
        Util.log("回调----$it1 at ${System.currentTimeMillis() - startTime}")
        act2(it1) { it2 ->
            Util.log("回调----$it2 at ${System.currentTimeMillis() - startTime}")
            act3(it2) { it3 ->
                Util.log("回调----$it3 at ${System.currentTimeMillis() - startTime}")
            }
        }
    }//可以发现嵌套层数很多，导致代码难以阅读
    //2 挂起函数解决异步问题
    println("2。挂起函数解决异步问题-------")
    runBlocking {
        val startTime = System.currentTimeMillis()
        val value1 = act1Coroutine("任意输入作为act1的参数")
        Util.log("value1----${value1}  at ${System.currentTimeMillis() - startTime}")
        val value2 = act2Coroutine(value1)
        Util.log("value2----${value2}  at ${System.currentTimeMillis() - startTime}")
        val value3 = act3Coroutine(value2)
        Util.log("value3----${value3}  at ${System.currentTimeMillis() - startTime}")
    }

}

val executor: ExecutorService = Executors.newFixedThreadPool(2)

//因为是异步操作结果不能再靠act1返回，利用回调函数callBack返回计算值
fun act1(id: String, callBack: (String) -> Unit) {
    executor.execute {
        Thread.sleep(1000)
        callBack("act1")
    }
}

fun act2(id: String, callBack: (String) -> Unit) {
    executor.execute {
        Thread.sleep(1500)
        callBack("act2")
    }
}

fun act3(id: String, callBack: (String) -> Unit) {
    executor.execute {
        Thread.sleep(2000)
        callBack("act3")
    }
}

suspend fun act1Coroutine(id: String): String = withContext(Dispatchers.Default) {
    delay(1000)
    "act1"
}

suspend fun act2Coroutine(id: String): String = withContext(Dispatchers.Default) {
    delay(1500)
    "act2"
}

suspend fun act3Coroutine(id: String): String = withContext(Dispatchers.Default) {
    delay(2000)
    "act3"
}

suspend fun awaitGetData(): Date =

    // 创建一个可以cancelled  suspendCancellableCoroutine
    suspendCancellableCoroutine<Date> { continuation ->

        val callback = object : NetCallback {
            override fun success(date: Date) {
                // Resume coroutine 同时返回Data
                continuation.resume(date) {

                }
                continuation.resume(date)
            }

            override fun error(e: String) {
                // Resume the coroutine

                continuation.resumeWithException(RuntimeException(e))
            }
        }
//        addListener(callback)// 调用接口，里面触发回调
        // 结束suspendCancellableCoroutine块的执行，直到在任一回调中调用continuation参数
    }

interface NetCallback {
    fun success(date: Date)
    fun error(e: String)
}
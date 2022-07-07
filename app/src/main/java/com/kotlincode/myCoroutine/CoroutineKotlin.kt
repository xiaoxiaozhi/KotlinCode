package com.kotlincode.myCoroutine

import kotlin.coroutines.*

/**
 * 《深入理解kotlin协程》
 * 1. 使用suspend关键字修饰的函数叫作挂起函数，挂起函数只能在协程体内或其他挂起函数内调用。
 * 创建协程
 *
 * suspendCoroutine 和 suspendCancellableCoroutine将回调转换为协程
 * https://blog.csdn.net/zhaoyanjun6/article/details/122058124#suspendCancellableCoroutine_5
 *
 */
fun main() {

    val continuation = suspend {// suspend 修饰的挂起函数，也就是协程本体
        println("In Coroutine. Thread ${Thread.currentThread()}")
        5
    }.createCoroutine(object : Continuation<Int> { //object 是协程的回调

        override fun resumeWith(result: Result<Int>) {
            println("Coroutine End: $result ${Thread.currentThread()}")
        }

        override val context = EmptyCoroutineContext
    })
    continuation.resume(Unit)
    //fun <T> (suspend () -> T).startCoroutine(completion: Continuation<T>) 创建协程之后立即执行
}

fun <R, T> launchCoroutine(receiver: R, block: suspend R.() -> T) {
    block.startCoroutine(receiver, object : Continuation<T> {
        override fun resumeWith(result: Result<T>) {
            println("Coroutine End: $result")
        }

        override val context = EmptyCoroutineContext
    })
}

class ProducerScope<T> {
    suspend fun produce(value: T) {}
}

fun callLaunchCoroutine() {
    launchCoroutine(ProducerScope<Int>()) {
        println("In Coroutine.")
        produce(1024)
//        delay(1000) 错误，不能调用外部的挂起函数挂的
        produce(2048)
    }
}

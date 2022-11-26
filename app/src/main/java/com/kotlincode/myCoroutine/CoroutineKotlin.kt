package com.kotlincode.myCoroutine

import kotlinx.coroutines.CoroutineName
import kotlin.coroutines.*

/**
 * [这个人对深入理解协程这本书，协程是怎么来的做了一些加工，帮助理解](https://aisia.moe/2018/02/08/kotlin-coroutine-kepa/)
 * 这里的创建协程跟官网介绍的不太一样，讲的是协程源码里面协程怎么创建起来的。
 * 1.createCoroutine创建协程
 *   创建并启动一个协程十分简单，你只需要两件宝具：一个 suspend lambda，以及一个 Continuation：使用createCoroutine创建协程，这样你就得到了一个未启动的协程，然后调用 resume 扩展方法启动这个协程：
 * 2.startCoroutine创建协程
 *   与createCoroutine不同suspendLambda.startCoroutine(completion)创建并立即启动协程
 *
 */
fun main() {

    //1.createCoroutine创建协程
    val continuation = suspend {// suspend 修饰的挂起函数，也就是协程本体
        Util.log("In suspend lambda")
        5
    }.createCoroutine(object : Continuation<Int> {

        override fun resumeWith(result: Result<Int>) {
            result.onSuccess {
                Util.log("resumeWith---onSuccess--result=$it")
            }
        }

        override val context = EmptyCoroutineContext+CoroutineName("diy")
    })
    continuation.resume(Unit)
    //2.startCoroutine创建协程
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

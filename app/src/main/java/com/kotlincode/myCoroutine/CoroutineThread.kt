package com.kotlincode.myCoroutine

import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

/**
 * 协程执行与线程切换
 * 1. newSingleThreadContext("thread name") 创建一个协程分发器，执行在单线程池上
 * 2. .use{ } 接收者为Closeable的扩展函数,执行完毕后会执行close()关闭接收者
 * 3. withContext()挂起函数
 */
private fun log(message: String) = println("[${Thread.currentThread().name}] $message")
fun main() {

    newSingleThreadContext("context1").use { ctx1 ->
        newSingleThreadContext("context2").use { ctx2 ->
            runBlocking(ctx1) {
                log("start in ctx1")
                withContext(ctx2) {
                    log("work in ctx2")
                }
                log("back to ctx1")
            }
        }
    }

}
package com.kotlincode.myCoroutine

import kotlinx.coroutines.*
import java.lang.Exception
import kotlin.system.measureTimeMillis

/**
 * 挂起函数
 * 1. 并行与并发 并发是两个队列交替使用一台咖啡机，并行是两个队列同时使用两台咖啡机 https://www.zhihu.com/question/33515481/answer/199929767
 */
fun main() {
    //1. async和launch实现并发
    myConcurrent()
    myConcurrent1()
    //2.CoroutineStart 协程启动形式
    myCoroutineStart()
    //3.嵌套携程，异常处理
    try {
        myConcurrent2()//结果发现，其中一个子协程抛出异常，父协程下面其它协程都会被取消，有finally执行finally
    } finally {
        println("nesting coroutine end ")
    }


}

//1. async 和 launch 实现的并发 区别在前者有返回值
fun myConcurrent() {
    runBlocking {
        val timeConsuming = measureTimeMillis {
            val value1 = async {
                initValue1()
            }
            val value2 = async {
                initValue2()
            }
            val result1 = value1.await()//挂起协程
            val result2 = value2.await()
            println(" result1 +result2 = ${result1 + result2}")
        }
        println("consuming = $timeConsuming")
    }
}

fun myConcurrent1() {
    runBlocking {
        val timeConsuming = measureTimeMillis {
            val value1 = launch {
                initValue1()
            }
            val value2 = launch {
                initValue2()
            }
            value1.join()
            value2.join()
        }
        println("consuming = $timeConsuming")
    }
}

fun myConcurrent2() {
    runBlocking {
        val value3 = launch {
            initValue3()
        }
        val value4 = launch {
            initValue4()
        }
        value3.join()
        value4.join()
    }
}

suspend fun initValue1(): Int {
    delay(500)
    return 15
}

suspend fun initValue2(): Int {
    delay(1000)
    return 10
}

suspend fun initValue3(): Int {
    try {
        delay(2000)
        println("initValue3")
        return 15
    } finally {
        println("initValue3 finally")
    }
}

suspend fun initValue4(): Int {
    delay(1000)
    println("initValue4")
    throw Exception()
    return 10
}

fun myCoroutineStart() {
    runBlocking {
        val deferred = async(start = CoroutineStart.LAZY) { // 协程延迟执行
            delay(1000)
            println("execute coroutine ${System.currentTimeMillis()}")
        }
        println("trigger ${System.currentTimeMillis()}")
        deferred.await()//触发执行,并挂起协程
//        deferred.start()//触发执行,普通函数不挂起协程
    }
}


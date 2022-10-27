package com.kotlincode.myCoroutine

import kotlinx.coroutines.*

/**
 * [按照这篇文章的总结](https://blog.csdn.net/cpcpcp123/article/details/112425954)
 * 协程处理异常
 * 1. 异常传播
 *     子协程未捕获到的异常将不会被重新抛出，而是一级一级向父作用域传递，这种异常传播将导致父父作用域失败，进而导致其子作用域的所有请求被取消。
 *     launch 在协程代码块中用try catch 才能捕获异常，注意在协程构建器外面try catch不能捕获异常
 *     async在结果 Deferred 对象中捕获所有异常 try{deferred.await()}catch{}因此给他用 CoroutineExceptionHandler没有效果。
 * 2. CoroutineExceptionHandler
 *    如果 try-catch 不在协程代码块中，那么它不会重新抛出异常，而是传播到顶级协程/父协程的工作，导致应用程序崩溃。
 *    CoroutineExceptionHandler 上下文元素，未捕获异常的协程设置后，可在这里捕获。是由于协程结构化并发的特性的存在，子作用域的异常经过一级一级的传递，最后由CoroutineExceptionHandler进行处理
 *    也就是说在CoroutineExceptionHandler被调用时，协程已经被取消
 * 3. SupervisorJob
 *    使用SupervisorJob ，子协程的失败不会影响到其他子协程。SupervisorJob 不会取消自身或它的其他子协程，而且SupervisorJob 不会传播异常而是让它的协程处理。
 *    无论我们使用何种类型的Job，未捕获的异常最终都会被抛出。在Android中，不论它发生在哪个调度器中都会使App崩溃。常用做法是在根协程加入CoroutineExceptionHandler捕获异常
 *    SupervisorJob只有在以下两种作用域中才会起作用：使用supervisorScope{...}或CoroutineScope(SupervisorJob())创建的作用域
 */
fun main() {

    //1. 异常传播
//    uncaughtException()
    //3.SupervisorJob

        val scope = CoroutineScope(SupervisorJob())
        scope.launch {
            Util.log(" scope.launch1")
            throw IndexOutOfBoundsException()
        }
        scope.launch {
            Util.log(" scope.launch2")
        }
        Util.log("runBlocking")

}

/**
 * 未捕获异常有两种情况，①不加try catch ② 在协程构建器外面try catch
 */
fun uncaughtException() {
    runBlocking {
        try {//
            val job = launch {
                Util.log("Throwing exception from launch")
                throw IndexOutOfBoundsException() // 我们将在控制台打印 Thread.defaultUncaughtExceptionHandler
            }
            job.join()
        } catch (e: Exception) {
            Util.log("Joined failed job exception----$e")
        }
        Util.log("Joined failed job")
        val deferred = async {
            Util.log("async")
        }
        deferred.await()
        Util.log("done")
    }
}

/**
 * 捕获异常
 */
fun catchException() {
    runBlocking {
        val job = launch { // launch 根协程
            try {
                Util.log("Throwing exception from launch")
                throw IndexOutOfBoundsException() // 我们将在控制台打印 Thread.defaultUncaughtExceptionHandler
            } catch (e: Exception) {
                Util.log("job launch----exception ${e}")
            } finally {
                Util.log("job launch----finally")
            }

        }
        job.join()
        Util.log("Joined failed job")
        val deferred = async { // async 根协程
            Util.log("Throwing exception from async")
            throw ArithmeticException() // 没有打印任何东西，依赖用户去调用等待
        }
        try {
            deferred.await()
            Util.log("Unreached")
        } catch (e: ArithmeticException) {
            Util.log("Caught ArithmeticException")
        }
        Util.log("done")
    }
}

/**
 * CoroutineExceptionHandler 捕获异常
 */
fun exceptionHandler() {

    runBlocking() {
//        val exceptionHandler = CoroutineExceptionHandler { _, exception ->
//            Util.log("CoroutineExceptionHandler exception : ${exception.message}")
//        }
//        val job = GlobalScope.launch(exceptionHandler) {
//            Util.log("Throwing exception from launch")
//            throw Exception()
//        }
//        job.join()
//        Util.log("done")

        val job = launch { // root coroutine with launch
            println("Throwing exception from launch")
            throw IndexOutOfBoundsException() // Will be printed to the console by Thread.defaultUncaughtExceptionHandler
        }
        job.join()
        println("Joined failed job")
        val deferred = async { // root coroutine with async
            println("Throwing exception from async")
            throw ArithmeticException() // Nothing is printed, relying on user to call await
        }
        try {
            deferred.await()
            println("Unreached")
        } catch (e: ArithmeticException) {
            println("Caught ArithmeticException")
        }
    }
}



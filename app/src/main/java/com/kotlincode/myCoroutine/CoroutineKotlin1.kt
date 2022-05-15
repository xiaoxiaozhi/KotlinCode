package com.kotlincode.myCoroutine

import kotlinx.coroutines.*
import java.util.*

/**
 *  ------结构化并发-----------
 *  1. https://www.jianshu.com/p/dcf63586335d
 *     即父Job和子Job形成树的数据结构，处理父子协程的cancel和exception
 *     结构化并发在android中的应用，启动一个全局协程处理UI更新，如果遇到网络问题迟迟更新不了，用户返回重进就会造成协程重复执行的问题，
 *     为避免这个问题把协程放在UI的的协程作用域中处理
 *  2. https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines/-coroutine-scope/index.html
 *     CoroutineScope() 返回一个使用Dispatchers.Default的协程作用域
 *     MainScope()返回一个使用 Dispatchers.Main的协程作用域
 *     coroutineScope()方法接收一个参数 CoroutineScope
 *  ------非阻塞挂起-----------
 *  https://www.jianshu.com/p/efd5037eda48
 *
 *
 * 《https://www.bilibili.com/video/BV1Ph411C7dG?p=58&spm_id_from=pageDriver b站大牛老师
 *  取消协程
 *  1. 使用cancel 和 join 组合 或者 cancelAndJoin()取消协程
 *  2. 当协程正处在计算过程中，利用isActive取消协程
 *  3. delay()内部挂起的时候会检查isActive是否为取消状态，所以例3能取消协程
 *  4. 父协程总会等待子协程完成
 */
fun main() {

    val s: Int.() -> Unit = { println("123") }

    println(
        "${
            tes {
                println("111111111111111111$c")
            }
        }"
    )
    runBlocking {
        launch {
            println("launch ${Date().time}")
        }
        println("runBlocking ${Date().time}")
    }
    //1. job与协程作用域
    jobAndScope()
    //2. 作用域 launch 和 GlobalScope.launch
    launchAndGlobal()
    //3. 取消协程
    println("----cancelJob()----")
    runBlocking { cancelJob() }
    //3.1 取消运算中的协程
    println("----cancelFault1()----")
    cancelJob1()
    //3.2 在try{} finally{}中关闭资源
    println("----cancelFinally()----")
    cancelFinally()
    //3.3 协程超时取消,抛出异常
    println("----timeOut()----")
//    timeout()
    //3.4 协程超时取消，不抛出异常
    timeoutOrNull()
    //3.5 嵌套协程取消
    println("----nestingCoroutine()----")
    nestingCoroutine()
}

//1. job与协程作用域
fun jobAndScope() {
    runBlocking {
        val job = launch {
            delay(1000)
            println("job block")
        }
        println("hello")
        job.join()// join 是挂起函数，挂起外部协程直到job执行完成
        println("word")
    }
}

//2. 外部协程会等待内部协程执行完成后才会结束
fun launchAndGlobal() {
    runBlocking {
        val join = GlobalScope.launch {
            delay(1000)
            println("job2 block")
        }// 2.1 GlobalScope.launch 协程作用域和runBlocking 提供的协程没有关系，GlobalScope.launch 调用 join() 挂起runBlocking协程
//        launch {
//            delay(1000)
//            println("job2 block")
//        }//2.2 runBlocking协程会等待launch执行完 才会执行完，launch使用的作用域CoroutineScope 是runBlocking提供的
        println("hello2")
        println("word2")
    }
}

//3. 取消协程
suspend fun cancelJob() {
    coroutineScope {
        val myJob = GlobalScope.launch {
            repeat(200) {
                println("hello $it")
                delay(500)
            }
        }
        delay(1000)
        println("runBlocking is finished")
        println("job cancel ")
        myJob.cancel(CancellationException("just a try"))//
        myJob.join()// 取消后要调用join 等待myjob取消完成
//        myJob.cancelAndJoin()//由于cancel和join经常成对出现，kotlin 提供了cancelAndJoin方法
    }
}

//3.1. 取消运算中的协程
fun cancelJob1() {
    runBlocking {
        val myJob = launch(Dispatchers.Default) {
            var currentTime = System.currentTimeMillis()
            var i = 0
            println()
            while (i < 20 && isActive) {// 当协程执行完毕或者job.cancel()时候 isActive 返回true，
                if (System.currentTimeMillis() - currentTime >= 500) {
                    println("i = ${++i}")
                    currentTime = System.currentTimeMillis()
                }
            }
        }
        delay(1000)
        println("hello word")
        myJob.cancelAndJoin()
        println("welcome")
    }
}

//3.2 在取消的函数中调用挂起函数
fun cancelFinally() {
    runBlocking {
        val job = launch {
            try {
                repeat(1000) { i ->
                    println("job: I'm sleeping $i ...")
                    delay(500L)
                }
            } finally {// finally中的代码在协程取消的时候执行
//                delay(1000L)// 在finally中任何挂起函数都会报异常因为协程在此时已经取消了
                withContext(NonCancellable) {// 罕见情况下需要在取消的协程中使用挂起函数，使用 withContext(NonCancellable)
                    println("job: I'm running finally")
                    delay(1000L)
                    println("job: And I've just delayed for 1 sec because I'm non-cancellable")
                }
            }
        }
        delay(1300L) // delay a bit
        println("main: I'm tired of waiting!")
        job.cancelAndJoin() // cancels the job and waits for its completion
        println("main: Now I can quit.")
    }
}

//3.3 协程超时取消
fun timeout() {
    runBlocking {
        withTimeout(1900) {//引发超时取消协程会报 TimeoutCancellationException
            try {                   // 使用try catch来捕获异常 ，timeoutOrNull 不抛出异常也就不用捕获异常
                repeat(20) {
                    println("i = $it")
                    delay(400)
                }
            } catch (e: TimeoutCancellationException) {

            }

        }
    }
}

//3.4 协程超时取消，不抛出异常
fun timeoutOrNull() {
    runBlocking {
        val result = withTimeoutOrNull(1900) {//引发超时取消协程会报 TimeoutCancellationException
            repeat(20) {
                println("i = $it")
                delay(400)
            }
        }
        println("result is $result")// 超时前协程正常执行则返回协程返回的结果，否则返回null
    }
}

//3.5 协程嵌套被取消
fun nestingCoroutine() {
    runBlocking {
        val job = launch {
            GlobalScope.launch {
                println("GlobalScope hello")
                delay(1000)
                println("GlobalScope word")
            }
            launch {
                println("launch1 hello")
                delay(1000)
//                for (i in 0 until Long.MAX_VALUE) {
//                }
                println("launch word")
            }

        }
        delay(500)
        job.cancel()
        delay(1000)
    }
}// 父协程取消，子协程也会被取消，但是有个特例 GlobalScope.launch创建的协程，只要进程还在会继续执行

fun nestingCoroutine1() {
    runBlocking {
        GlobalScope.launch {
            println("GlobalScope hello")
            delay(1000)
            println("GlobalScope word")
        }
        launch {
            println("launch1 hello")
            delay(1000)
//                for (i in 0 until Long.MAX_VALUE) {
//                }
            println("launch word")
        }
        delay(200)
        coroutineContext[Job]?.cancel()
    }
    runBlocking {
        delay(10 * 1000)
    }

}// 父协程取消，子协程也会被取消，但是有个特例 GlobalScope.launch创建的协程，只要进程还在会继续执行

class ABC {
    val c: Int = 0
}

fun tes(a: ABC.() -> Unit) {
    ABC().a()
}

fun getResult(): Boolean {
    println("current ${System.currentTimeMillis()}")
    return true
}
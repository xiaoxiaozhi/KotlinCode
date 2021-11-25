package com.kotlincode.myCoroutine

import kotlinx.coroutines.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * 协程上下文：
 * 1. 协程分发器
 * 2. 增加 VM options -Dkotlinx.coroutines.debug 这样打印线程名字的时候也会打印协程名字
 * 3. 通过协程上下文获取job对象
 * 4. 协程状态isActive
 * 5. 设置协程名字
 * 6. ThreadLocal 在协程中的应用
 */
private fun log(message: String) = println("[${Thread.currentThread().name}] $message")
fun main() {
    //1. 协程分发器，设置协程运行线程
    dispatcher()
    //2.通过上下文获取job对象
    getJob()
    //3. 设置协程名称
    runBlocking(CoroutineName("name")) {
        log("runBlocking1")
        launch(CoroutineName("launch1")) {
            log("launch1")
        }
        log("runBlocking2")
    }//[main @name#7] runBlocking1 打印结果，#7是 main线程执行的地7个协程
    //4. 协程中使用ThreadLocal
    coroutineThreadLocal()
}

//1. 协程分发器，设置线程
fun dispatcher() {
    runBlocking {
        launch {
            println("no parameter,${Thread.currentThread().name}")
        }//不指定,继承父协程的上下文
        launch(Dispatchers.Unconfined) {
            println("before delay Unconfined ${Thread.currentThread().name}")
//            delay(100)
            val es = Executors.newSingleThreadExecutor().asCoroutineDispatcher()
            withContext(es) {
                println("es")
                es.close()
            }
            println("after delay Unconfined ${Thread.currentThread().name}")
        }//无指定线程，在当前线程立即执行，遇到第一个挂起点，后续协程将会在执行挂起点的线程执行，delay在kotlinx.coroutines.DefaultExecutor线程执行,es在自定义线程池执行
        launch(Dispatchers.Default) {
            println("default ${Thread.currentThread().name}")
        }//在共享线程池上执行,未指定运行线程的 GlobalScope.launch 也在共享线程池上运行(DefaultDispatcher-worker-X)
        //指定自定义线程池,这个方法会导致协程挂起
        val es = Executors.newSingleThreadExecutor().asCoroutineDispatcher()
        launch(es) {
            println("asCoroutineDispatcher ${Thread.currentThread().name}")
            es.close()// override fun close() { (executor as? ExecutorService)?.shutdown() } 实际执行的是线程池的shutdown方法
        }//协程执行完成后没有返回到当前线程，这时候需要手动关闭执行线程
    }

}

//2. 通过上下文获取job对象
fun getJob() {
    runBlocking {
        val job = coroutineContext[Job]
        println("$job")
    }
}

//4. ThreadLocal 与Coroutine
fun coroutineThreadLocal() {
    val threadLocal = ThreadLocal<String>()
    runBlocking {
        threadLocal.set("hello")
        log("threadLocal1 value is ${threadLocal.get()}")
        launch(Dispatchers.Default + threadLocal.asContextElement()) {
            log("threadLocal2 value is ${threadLocal.get()}")
        }//threadLocal.asContextElement(value = "word") 还可以在线程切换前提前赋值
    }
}//协程与线程绑定并不紧密，正相反ThreadLocal的存取值与线程紧密相关，协程执行的时候切换线程，导致ThreadLocal取值异常
// threadLocal.asContextElement()一直维护着ThreadLocal，在协程执行的时候local值与线程无关。
package com.kotlincode.myCoroutine

import kotlinx.coroutines.*
import java.util.concurrent.Executors

/**
 * 协程官网 https://kotlinlang.org/docs/coroutines-basics.html
 * https://blog.csdn.net/zou8944/article/details/106447727
 *
 * 协程创建器
 * 1. CoroutineScope.launch{} 创建一个新的协程，不阻塞当前线程，也可以指定协程调度器，如果不指定运行在当前线程
 * 2. runBlocking{} 创建一个协程，阻塞当前线程，直到协程以及子协程全部结束，不要在开发中使用，主要是为了测试
 * 3. withContext{} 不会创建新的协程，在指定协程上运行挂起代码块，并挂起该协程直到该代码块完成,可以指定代码块运行的线程，执行完后返回该线程
 * 4. CoroutineScope.async{} 与launch一样创建一个新的协程，唯一不同的是返回一个值 ，deferred
 * 5. coroutineScope{} 它是一个挂起函数，创建一个协程并挂起底层协程，直到所有的子协程都完成时才能结束,
 *
 * 协程构建器参数
 * 1. CoroutineStart 协程启动方式
 *
 *
 * 并行和并发：讲话和吃东西交织在一起——这就是并发。一般来说，我们吃和听可以并行，但吃和说可以并发。
 * 1. CoroutineScope  创建了一个协程作用域， 它定义了launch、async、withContext等协程启动方法
 * 并在这些方法内定义了启动子协程时上下文的继承方式。只有里面的子协程都执行完，作用域才会结束
 * 2. CoroutineContext 协程上下文一些元素的集合，包含协程的运行环境(在哪个线程)，包括协程调度器(Dispatchers)、代表协程本身的Job、协程名称、协程ID等
 * 3. CoroutineDispatcher 调度器，决定协程所在的线程或线程池 ，指定协程运行在哪一个线程或者线程池，不指定表示运行在当前线程
 * ①.Dispatchers.IO的值可用于在专用于运行IO密集型任务的池中执行协程
 * ②.Dispatchers.Default的值指示在Default-Dispatcher池的线程中开始执行的协程。这个池中的线程数为2或等于系统的核数，以较高者为准。
 * ③.Dispatchers.Main可用于Android设备和Swing UI，来运行只从main线程更新UI的任务
 *
 * launch()
 * 1. 如果不指定CoroutineDispatcher ，默认调度器就是Dispatchers.Default,Default 是一个协程调度器，其指定的线程为共有线程，线程数量至少为2，最大与CPU数量相同
 * 2. 作用是返回一个job对象，该对象用于等待协程的终止或取消，但是无法从使用launch()启动的协程返回结果。
 * 如果希望异步执行任务并获得响应，请使用async()而不是launch()。
 * job()和Deferred()
 * job完成时没有返回值，如果需要返回值的话应该使用deferred，它是job的子类
 *
 * 挂起点函数
 * 1.yield()方法不会导致任何显式延迟。这两种方法都为另一个挂起的任务提供了执行的机会。
 * 2.delay()函数：将当前执行的任务暂停指定的毫秒数。这两种方法都为另一个挂起的任务提供了执行的机会
 * 3.Kotlin将只允许在带有suspend关键字注释的函数中使用挂起点。但是，使用suspend标记函数并不会自动使函数在协程中运行或并发运行
 */
fun main() {
    GlobalScope.launch {
        println("Thread ${Thread.currentThread().name}")
        delay(1000)
        println("kotlin coroutine ${System.currentTimeMillis()} Thread ${Thread.currentThread().name}")
    }
    println("hello ${System.currentTimeMillis()} Thread ${Thread.currentThread().name}")
    Thread.sleep(2000)// 试验证明，线程睡眠途中协程执行了,GlobalScope.launch创建的协程不再主线程中。
    println("word ${System.currentTimeMillis()}")
    //1. 顺序执行
    println("----顺序执行----")
    sequential()
    //2. 协程 并发执行
    println("----并发执行----")
    concurrent()
    //3. 设置上下文
    println("----其他线程池中执行----")
    runBlocking {
        launch(Dispatchers.Default) { task5() }//Dispatchers.Default的值指示在Default-Dispatcher池的线程中开始执行的协程。这个池中的线程数为2或等于系统的核数，以较高者为准。
        launch { task6() }
        println("call task5 and task6 from ${Thread.currentThread()}")
    }
    //4. 在自定义线程池中运行协程
    println("----自定义线程池中执行----")
    customThreadPool()
    //5. 在协程挂起点后切换该协程的线程
    println("----在挂起点后切换线程----")
    switchThread()
    //6. 修改协程上下文
    println("----修改协程上下文----")
    switchContext()// 未达到试验目的，暂时挂起
    //7. async和await
    println("---- async和await----")
    coroutineAsync()// 调用async 得到deferred 对象，然后deferred.await()得到协程结果await方法会阻塞流程
    //8. 延续
    println("----延续----")
    coroutineContinue()
    //9. job和作用域
    println("----job和协程作用域----")
    jobAndScope()

}

//1. 顺序执行
fun sequential() {
    println("start")
    run {
        task1()
        task2()
        println("call task1 and task2 from ${Thread.currentThread()}")
    }
    println("done")
}

//2. 协程 并发执行
fun concurrent() {
    println("start")
    runBlocking {         //runBlocking 启动一个协程，调用它的线程被阻塞，直到它里面的代码运行完毕
        launch { task3() }//launch()函数启动一个新的协程来执行给定的lambda
        launch { task4() }
        println("call task3 and task4 from ${Thread.currentThread()}")
    }
    println("done")
}

//4. 在自定义线程池中运行协程
fun customThreadPool() {
    Executors.newSingleThreadExecutor().asCoroutineDispatcher()//单线程池的扩展函数asCoroutineDispatcher()
        .use {
            println("start")
            runBlocking {
                launch(it) { task1() }//launch接收自定义线程池传过来的上下文，lambda 执行完毕后，use()函数将关闭执行器，
                launch { task2() }
                println("call task1 and task2 from ${Thread.currentThread()}")
            }
            println("done")
        }
}

//5. 在挂起点后切换线程
fun switchThread() {
    Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()).asCoroutineDispatcher()
        .use {
            println("start")
            runBlocking {//使用launch的第二个参数CoroutineStart实现这点。
                //1.DEFAULT       默认实现
                //2.LAZY          延迟执行，直到调用显式的start()
                //3.ATOMIC        不可取消的模式运行
                //4.UNDISPATCHED  来最初在被调用上下文中运行，但在挂起点之后切换到launch接收到的上下文线程
                launch(it, start = CoroutineStart.UNDISPATCHED) { task3() }
                launch { task4() }
                println("call task3 and task4 from ${Thread.currentThread()}")
            }
            println("done")
        }
}

//6. 运行中切换协程上下文
fun switchContext() {
    Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()).asCoroutineDispatcher()
        .use {
            println("start")
            runBlocking(CoroutineName("Top")) {
                withContext(Dispatchers.Default) { task3() }
                launch() { task4() }
                println("call task3 and task4 from ${Thread.currentThread()}")
            }
            println("done")
        }
}

//7. async
fun coroutineAsync() {
    runBlocking {
        val count: Deferred<Int> = async(Dispatchers.Default) {
            println("feting in thread ${Thread.currentThread()}")
            Thread.sleep(1000)
            Runtime.getRuntime().availableProcessors()
        }
        println("1called the function in the ${Thread.currentThread()}")
        println("number of cores is ${count.await()}")// 阻塞执行流程
        println("2called the function in the ${Thread.currentThread()}")
    }
}

//8. 延续
fun coroutineContinue() {
    runBlocking {
        val compute = Compute()
        launch(Dispatchers.Unconfined) {
            compute.compute2(1)
        }
        launch {
            compute.compute2(2)
        }
    }
}

fun task1() {
    println("start task1 in thread ${Thread.currentThread()}")
    println("end task1 in thread ${Thread.currentThread()}")
}

fun task2() {
    println("start task2 in thread ${Thread.currentThread()}")
    println("end task2 in thread ${Thread.currentThread()}")
}

suspend fun task3() {
    println("start task3 in thread ${Thread.currentThread()}")
    yield()//yield()方法不会导致任何显式延迟。这两种方法都为另一个挂起的任务提供了执行的机会。
    println("end task3 in thread ${Thread.currentThread()}")
}

suspend fun task4() {
    println("start task4 in thread ${Thread.currentThread()}")
    delay(1000)//delay()函数：将当前执行的任务暂停指定的毫秒数。这两种方法都为另一个挂起的任务提供了执行的机会。
    println("end task4 in thread ${Thread.currentThread()}")
}

suspend fun task5() {
    Thread.sleep(100)
    println("start task5 in thread ${Thread.currentThread()}")
    yield()
    println("end task5 in thread ${Thread.currentThread()}")
}

suspend fun task6() {
    println("start task6 in thread ${Thread.currentThread()}")
    yield()
    println("end task6 in thread ${Thread.currentThread()}")
}

class Compute {
    fun compute1(n: Long) = n * 2
    suspend fun compute2(n: Long): Long {
        val factor = 2
        println("$n received thread ${Thread.currentThread()}")
        delay(n)
        val result = n * factor
        println("$n return $result :Thread ${Thread.currentThread()}")
        return result

    }
}


package com.kotlincode.myCoroutine

import kotlinx.coroutines.*
import org.jetbrains.annotations.TestOnly
import java.util.concurrent.Executors
import kotlin.system.measureTimeMillis

/**
 * 协程官网 https://kotlinlang.org/docs/coroutines-basics.html

 * 1. 协程基本概念
 *    协程是可挂起计算的实例。类似于一个线程，与其余代码并发工作。但是，协程不绑定到任何特定的线程。它可以在一个线程中暂停执行，在另一个线程中恢复执行。
 *    runBlocking{}运行一个新的协同程序并可中断地阻塞当前线程，直到其完成。这个函数不应该在协程中使用。它的设计目的是将常规阻塞代码与以挂起方式编写的库连接起来，以便在主函数和测试中使用
 *    launch{}:一种协程构建器，创建一个新的协程，也可以指定协程调度器，如果不指定运行在当前线程
 *    delay(n 毫秒)：一种挂起函数。暂停协程n毫秒但不会阻塞底层线程，允许其他协程运行并使用底层线程的代码。
 *    suspend function：使用suspend关键字修饰的函数叫作挂起函数，挂起函数只能在协程体内或其他挂起函数内调用
 *    async{} 与launch一样创建一个新的协程，唯一不同的是返回一个值 ，deferred
 *    withContext(必须指定上下文){} 不会创建新的协程，在指定上下文运行挂起代码块，并挂起该协程直到该代码块完成,执行完后返回该线程，由于不新创建协程代码块仍然处于调用协程那里，但是执行线程却不一样
 *    note:并发
 * 2. 结构化并发
 *    [谷歌搜索 a principle of structured concurrency 看到的一篇文章](https://proandroiddev.com/structured-concurrency-in-action-97c749a8f755)
 *    “结构化并发”是指一种构建异步/并发计算的方法，以便保证子操作在其父操作之前完成，即在父操作范围之外不执行任何子操作。
 *    协程遵循结构化并发原则，这意味着新的协程只能在一个特定的 CoroutineScope 中启动，这个 CoroutineScope 限定了协程的生命周期。
 *    所有子协程都完成外部CoroutineScope才能结束，外部CoroutineScope停止，子协程都将停止
 *    并行与并发 并发是两个队列交替使用一台咖啡机，并行是两个队列同时使用两台咖啡机. 协程并发并不是同时执行几个协程二是按照协程产生的顺序执行(目前观察到的情况就是这样)
 * 3. 构建协程作用域
 *    3.1 coroutineScope{} 除了协程构建器提供的作用域之外，用coroutineScope也能构建。它是一个挂起点函数，创建一个协程作用域并挂起底层协程，直到所有的子协程都完成时才能结束
 *    3.2 MainScope{} 查看代码 CoroutineActivity.kt  运行在主线程 记得在onDestroy()中调用cancel()取消协程 实验发现在kt文件下运行报错
 *    3.3 GlobalScope{} 运行在线程 [DefaultDispatcher-worker-2] 实验发现在kt文件中运行报错
 *    3.4 withTimeout(1000){}和withTimeoutOrNull(1000){} 挂起协程，超过一定时间就取消协程，执行完返回结果。前者最好用try catch捕获超时异常 后者不抛出超时异常用返回null代替
 *        withTimeout{}超时事件对于代码块运行是异步的，有可能在代码块完成之前就超时异常，所以我们要在finally中 把代码块的资源关闭
 *
 * 4. job
 *    launch{}协程构建器返回一个 Job 对象，该对象是已启动协程的句柄,Job.join()可以挂起外部协程直到 launch完成
 *    协程的Job是上下文的一部分也可以这样获得 launch{coroutineContext[Job]}
 *    4.1 取消协程
 *        协程中所有挂起函数都是可取消的。它们检查协同程序的取消情况，并在取消时产生异常(CancellationException会被协程忽略，try catch仍能捕获)。作用域内的循环不一定完全消失，这时候需要用isActivity字段判断。这叫协同取消。
 *        job.cancel()取消协程 job.join()等待协程完全取消。 这样做会有问题，就是cancel后 isActive实际是Job的字段 coroutineContext[Job]?.isActive
 *        4.1.1 有循环的协程不能立即取消掉
 *        4.1.2 取消这种协程有两种方式①在循环中加入挂起函数 yield()或者delay() ② 循环中加入isActive判断 例如while (isActive)
 *        4.1.3 用try catch 捕获 取消引起的挂起函数异常 finally{在这里关闭资源} 例如关闭 文件 或者网络，需要挂起协程等待关闭，但是finally不允许存在挂起函数。
 *              这时候要用到 withContext(NonCancellable){}执行挂起操作
 *        note:有循环又不检查isActive的协程无法取消
 * 5. CoroutineContext
 *    协程上下文：协程在CoroutineContext提供的上下文中执行，上下文是一系列元素的集合，主要有Job和Dispatchers(调度程序)，协程名称、协程ID。看源码可知Job和Dispatcher就是一种上下文。
 *    所有的构建器 例如launch{}、 async{}...都可以设置一个上下文(指定Job就不会随着父作用域取消而取消，指定Dispatcher子协程就会在指定线程上运行)，如果不指定就继承所在CoroutineScope的上下文
 *    多个上下文元素可以组合在一起使用launch(Dispatchers.Default + CoroutineName("test")){}
 *    CoroutineDispatcher：协程上下文包括一个CoroutineDispatcher ，它的功能是确定协程在哪个线程上执行.
 *                        Dispatchers.Unconfined 是一个特殊的调度器。在调用者线程中启动协程，但是仅仅会持续到第一个挂起点；当挂起函数结束后协程恢复运行时，但此时执行协程的线程由执行挂起函数的线程决定（挂起函数由哪个线程执行 后续协程就在这个线程运行）极少使用
 *                        Dispatchers.Default 如果CoroutineScope没有指定调度器，那么默认就是这个，该调度器使协程运行在一个共享后台线程池，这个池中的线程数为2或等于系统的核数，以较高者为准。
 *                        newSingleThreadContext 创建一个线程专门运行协程，线程资源非常昂贵所以运行完毕要一定要释放
 *                        Executors.newSingleThreadExecutor().asCoroutineDispatcher()//单线程池的扩展函数asCoroutineDispatcher() 用法同上
 *                        Dispatchers.IO的值可用于在专用于运行IO密集型任务的池中执行协程
 *                        Dispatchers.Default的值指示在Default-Dispatcher池的线程中开始执行的协程。这个池中的线程数为2或等于系统的核数，以较高者为准。
 *                        Dispatchers.Main可用于Android设备和Swing UI，来运行只从main线程更新UI的任务
 *                        runBlocking{}的调度器默认是它所在的线程。例如 Thread("线程1"){runBlocking{ 运行在线程1中 }}.start()
 *                        在线程之间跳转：同一个协程在不同线程之间执行  代码在下面
 *    CoroutineScope：launch{}和async{}是CoroutineScope的扩展函数。查看CoroutineScope代码发现CoroutineContext有事作用域的一个参数。所以launch和async开启的协程继承了所处作用域的上下文
 *                   当一个协程在一个 CoroutineScope 中启动时，它继承了 CoroutineScope的coroutineContext(包含job)，所以当作用域取消时，子协程也会被取消
 *                   有两种方式能避免这种情况，①给子协程显示指定一个作用域 ②给子协程设置Job
 *                   创建一个作用域 来管理整个协程 。例如 MainScope{} 代码在CoroutineActivity
 *    CoroutineName:给协程指定名称 launch(CoroutineName("自家协程")){}
 *    ThreadLocal：在协程中传递数据,ThreadLocal在线程中set()和get()的值在其他线程中无效。由于协程在线程之间自由切换，协程提供了一个扩展函数threadLocal.asContextElement(value = "launch")
 *    、           方便在切换协程的时候，也能像线程那样set()和get()。
 *
 * 6. CoroutineStart：
 *    协程构建器 launch 和 async 启动选项    例如： launch(start=)
 *    6.1.DEFAULT       默认实现，立即执行
 *    6.2.LAZY          调用deferred.start() 协程才开始执行,如果不调用start就直接wait 。两个deferred就会变成顺序执行也就是同步关系  代码在下面
 *    6.3.ATOMIC        不可取消的模式运行
 *    6.4.UNDISPATCHED  来最初在被调用上下文中运行，但在挂起点之后切换到launch接收到的上下文线程 ,类似 Dispatchers.Unconfined  代码在下面
 *
 * 7. 并发异步async
 *    如果用同步的话 r1 = {delay(10) 1} r2 = {delay(10) 2}  r1+r2   由于每个launch都要等待得到结果需要20毫秒的时间
 *    以下是并发异步代码，只需要10毫秒。async 返回一个 Deferred ，执行deferred.wait() 挂起协程而不阻塞线程，直到获取值,而launch 返回一个 Job，不含任何结果
 *    deferred，它是job的子类，所以deferred也可以取消。
 * 8. 超时
 *     withTimeout(1000){}和withTimeoutOrNull(1000){} 挂起协程，超过一定时间就取消协程，执行完返回结果。前者最好用try catch捕获超时异常 后者不抛出超时异常用返回null代替
 *     8.1 超时释放资源
 *         withTimeout{}超时事件对于代码块运行是异步的，有可能在代码块完成之前就超时异常，所以我们要在finally中 把代码块的资源关闭
 *
 * 挂起点函数
 * 1.yield()方法不会导致任何显式延迟。这两种方法都为另一个挂起的任务提供了执行的机会。
 * 2.delay()函数：将当前执行的任务暂停指定的毫秒数。这两种方法都为另一个挂起的任务提供了执行的机会
 * 3.coroutineScope{}
 * 4.job.join()
 * 5.withContext(指定上下文){}
 * 6.deferred.wait()
 * 7.withTimeoutOrNull(1000){}
 *
 * TODO [在协程中调用网络请求教程](https://kotlinlang.org/docs/coroutines-and-channels.html)
 * TODO  suspendCoroutine和suspendCancellableCoroutine
 */
fun main() {
    //1.协程基本概念
    println("1.协程基本概念-----")
    runBlocking {
        launch {
            doWorld()//挂起函数
        }
        println("Hello")
    }
    //2.结构化并发
    structuredConcurrent()
    //3. 构建协程作用域
    println("3.构建协程作用域----")
    runBlocking {
        doWorld1()//coroutineScope{}挂起底层协程，直到子协程执行完毕才会结束。
        Util.log("Done")
    }
    //4.job
    println("4.job----")
    runBlocking {
        val job = launch {//job是一个协程的句柄
            delay(1000L)
            println("World!")
        }
        println("Hello")
        job.join()
        println("Done")
    }

    //4.1 取消协程
    println("4.1.取消协程----")
    runBlocking {
        //4.1.1 有循环的协程不能立即取消掉
        println("cancelJob-------")
        cancelJob()//有循环又不检查isActive的协程无法取消
        //4.1.2 取消这种协程有两种方式
        println("cancelJob1-------")
        cancelJob1()//
        //4.1.3 用try catch 捕获异常并用finally{}关闭资源
        println("cancelFinally-------")
        cancelFinally()
        //4.1.4
    }


    //5.协程上下文
    println("5.协程上下文----")
    runBlocking {
        newSingleThreadContext("nst").use {
            launch(it) {//
                Util.log("运行在指定线程上面")
            }
        }
    }

    //unconfined
    println("unconfined----")
    unconfined()
    println("在线程之间跳转---")
    newSingleThreadContext("context1").use { ctx1 ->//.use{ } 接收者为Closeable的扩展函数,执行完毕后会执行close()关闭接收者
        newSingleThreadContext("context2").use { ctx2 ->
            runBlocking(ctx1) {
                Util.log("start in ctx1")
                withContext(ctx2) {// withContext()挂起点函数，
                    repeat(Int.MAX_VALUE) {}
                    Util.log("work in ctx2")
                }
                Util.log("back to ctx1")
            }
        }
    }
    println("避免作用域取消后 子协程跟着取消----")
    runBlocking {
        val request = launch {
            launch(Job()) {//指定 Job避免被取消
                println("job1: I run in my own Job and execute independently!")
                delay(1000)
                println("job1: I am not affected by cancellation of the request")
            }
            launch {
                delay(100)
                println("job2: I am a child of the request coroutine")
                delay(1000)
                println("job2: I will not execute this line if my parent request is cancelled")
            }
            GlobalScope.launch {//在另一个作用域中 启动线程避免被取消
                println("job3: I run in GlobalScope and execute independently!")
                delay(1000)
                println("job3: I am not affected by cancellation of the request")
            }
        }
        delay(500)
        request.cancel() // cancel processing of the request
        println("main: Who has survived request cancellation?")
        delay(1000) // delay the main thread for a second to see what happens
    }
    println("给协程命名----")
    runBlocking {
        launch(CoroutineName("自家协程")) {
            Util.log("给协程命名---")
        }
    }

    println("ThreadLocal协程之间传递数据-----")
    coroutineThreadLocal()

    //6. 协程启动选项
    println("6. 协程启动选项-----")
    println("6.2 协程启动选项-----LAZY")
    lazyStart()
    println("6.4协程启动选项-----UNDISPATCHED-----")
    undispatchedStart()

    //7.并发异步
    println("7.---- async----")
    coroutineAsync()// 调用async 得到deferred 对象，然后deferred.await()得到协程结果await方法会阻塞流程
    //8. 超时
    println("8.超时-------")
    timeoutOrNull()//演示异步
    println("8.1 超时取消finally中取消资源-------")
    asynchronousTimeout()

}

//2. 结构化并发
fun structuredConcurrent() {
    runBlocking {
        val startTime = System.currentTimeMillis()
        launch {
            var nextPrintTime = startTime
            var i = 0
            while (i < 5) { // computation loop, just wastes CPU
                // print a message twice a second
                if (System.currentTimeMillis() >= nextPrintTime) {
                    Util.log("job: I'm sleeping ${i++} ...")
                    nextPrintTime += 500L
                    yield()
                }
            }
        }
        launch {
            Util.log("launch2----1")
            yield()
            Util.log("launch2----2")
        }
        launch {
            Util.log("launch3----1")
            yield()
            Util.log("launch3----2")
        }
        Util.log("runBlocking done")
    }
}

suspend fun doWorld() {
    delay(1000L)
    println("World!")
}

suspend fun doWorld1() = coroutineScope {
    launch {
        delay(1000L)
        Util.log("World!")
    }
    Util.log("Hello")
}

/**
 * 协程中有循环，并且不检查isActive，那么它就不能被取消
 */
suspend fun cancelJob() {
    coroutineScope {
        val startTime = System.currentTimeMillis()
        val myJob = launch(Dispatchers.Default) {
            var nextPrintTime = startTime
            var i = 0
            try {
                while (i < 5) { // computation loop, just wastes CPU
                    // print a message twice a second
                    if (System.currentTimeMillis() >= nextPrintTime) {
                        Util.log("job: I'm sleeping ${i++} ...")
                        nextPrintTime += 500L
//                        yield()
                    }
                }
            } catch (e: Exception) {
                Util.log("e----$e")
            }
        }
        Util.log("job cancel1")
//        myJob.cancel(CancellationException("just a try"))//在上面的try catch中捕获
//        myJob.join()// 取消后要调用join 等待myjob取消完成
        myJob.cancelAndJoin()//由于cancel和join经常成对出现，kotlin 提供了cancelAndJoin方法
        Util.log("job cancel2")
    }
}

/**
 *协程取消的两种方式 ①循环判断时加入 isActive ② 循环中加入 delay()或者yield()
 */
suspend fun cancelJob1() {
    coroutineScope {
        val myJob = launch(Dispatchers.Default) {
            var currentTime = System.currentTimeMillis()
            var i = 0
            while (i < 5 && isActive) {// 当协程执行完毕或者job.cancel()时候 isActive 返回true，
                if (System.currentTimeMillis() - currentTime >= 500) {
                    Util.log("i = ${++i} ")
                    currentTime = System.currentTimeMillis()
//                    yield()
                }
            }
        }
        delay(600)
        Util.log("hello word")
        myJob.cancelAndJoin()
        Util.log("welcome")

    }
}

suspend fun cancelFinally() {
    coroutineScope {
        val job = launch {
            try {
                repeat(1000) { i ->
                    Util.log("cancelFinally: I'm sleeping $i ...")
                    delay(500L)
                }
            } finally {
                withContext(NonCancellable) {
                    Util.log("cancelFinally: I'm running finally")
                    delay(1000L)
                    Util.log("cancelFinally: And I've just delayed for 1 sec because I'm non-cancellable")
                }
            }
        }
        delay(1300L) // delay a bit
        println("cancelFinally: I'm tired of waiting!")
        job.cancelAndJoin() // cancels the job and waits for its completion
        println("cancelFinally: Now I can quit.")
    }
}

/**
 * DefaultExecutor它是一个object 并且是一个runnable 本身与时间循环相关
 * internal actual object DefaultExecutor : EventLoopImplBase(), Runnable
 */
fun unconfined() {
    runBlocking<Unit> {
        launch(Dispatchers.Unconfined) { // not confined -- will work with main thread
            Util.log("Unconfined      : I'm working in thread")
            delay(500)//delay函数运行在 kotlinx.coroutines.DefaultExecutor
            Util.log("Unconfined   :delay after thread in ")
            newSingleThreadContext("nst").use {
                withContext(it) {// withContext 挂起点函数运行在 nst线程上
                    Util.log("es")
                }
            }
            Util.log("Unconfined      : After delay in thread")//打印 After delay in thread kotlinx.coroutines.DefaultExecutor
        }
        launch { // context of the parent, main runBlocking coroutine
            Util.log("main runBlocking: I'm working in thread ")
            delay(1000)
            Util.log("main runBlocking: After delay in thread ")
        }
    }

}

fun coroutineThreadLocal() {
    val threadLocal = ThreadLocal<String>()
    runBlocking {
        threadLocal.set("hello")
        Util.log("threadLocal1 value is ${threadLocal.get()}")
        launch(Dispatchers.Default + threadLocal.asContextElement(value = "word")) {//在协程的上下文中赋值，好像只能用这种方式。看注释更清楚
            Util.log("threadLocal2 value is ${threadLocal.get()}")
        }
        println("threadLocal3 value is ${threadLocal.get()}")
    }
}//协程与线程绑定并不紧密，正相反ThreadLocal的存取值与线程紧密相关，协程执行的时候切换线程，导致ThreadLocal取值异常
// threadLocal.asContextElement()一直维护着ThreadLocal，在协程执行的时候local值与线程无关。

//6.2
fun lazyStart() {
    runBlocking<Unit> {
        val time = measureTimeMillis {
            val one = async(start = CoroutineStart.LAZY) { doSomethingUsefulOne() }
            val two = async(start = CoroutineStart.LAZY) { doSomethingUsefulTwo() }
            // some computation
            // one.start() // start the first one
            //two.start() // start the second one
            println("The answer is ${one.await() + two.await()}")//
        }
        println("Completed in $time ms")
    }
}

//6.4 UNDISPATCHED
fun undispatchedStart() {//该例是UNDISPATCHED 选项的演示
    Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()).asCoroutineDispatcher()
        .use {
            Util.log("start")
            runBlocking {//使用launch的第二个参数CoroutineStart实现这点。
                launch(it, start = CoroutineStart.UNDISPATCHED) {
                    Util.log("start task3 in thread")
                    delay(100)//yield()方法不会导致任何显式延迟。这两种方法都为另一个挂起的任务提供了执行的机会。
                    Util.log("end task3 in thread ")
                }
                launch {
                    Util.log("start task4 in thread ")
                    delay(500)//delay()函数：将当前执行的任务暂停指定的毫秒数。这两种方法都为另一个挂起的任务提供了执行的机会。
                    Util.log("end task4 in thread ")
                }
                Util.log("call task3 and task4")
            }
            Util.log("done")
        }
}

suspend fun doSomethingUsefulOne(): Int {
    delay(1000L) // pretend we are doing something useful here
    return 13
}

suspend fun doSomethingUsefulTwo(): Int {
    delay(1000L) // pretend we are doing something useful here, too
    return 29
}

//7. async
fun coroutineAsync() {
    runBlocking {
        val time = measureTimeMillis {
            println("The answer is ${concurrentSum()}")
        }
        println("Completed in $time ms")
    }
}

suspend fun concurrentSum(): Int = coroutineScope {
    val one = async {
        Util.log("one-----1")
        delay(1000L)
        Util.log("one-----2")
        13
    }
    val two = async {
        Util.log("two-----1")
        delay(1000L)
        Util.log("two-----2")
        20
    }
    one.await() + two.await()
}

//8 超时
fun timeoutOrNull() {
    runBlocking {
        Util.log("runBlocking----")
        val result = withTimeoutOrNull(1900) {//引发超时取消协程会报 TimeoutCancellationException
//            throw Exception()
            repeat(20) {
                delay(400)
                Util.log("i = $it")
            }
        }
        Util.log("result is $result")// 超时前协程正常执行则返回协程返回的结果，否则返回null
    }
}

//8.1 超时取消 资源释放
fun asynchronousTimeout() {
    runBlocking {
        repeat(3) { // Launch 100K coroutines
            launch {
                var resource: Resource? = null // Not acquired yet
                try {
                    withTimeout(60) { // Timeout of 60 ms
                        delay(50) // Delay for 50 ms
                        resource = Resource() // Store a resource to the variable if acquired
                    }
                    // We can do something else with the resource here
                } finally {
                    resource?.close() // Release the resource if it was acquired
                }
            }
        }
    }
    // Outside of runBlocking all coroutines have completed
    println(acquired) // Print the number of resources still acquired
}

var acquired = 0

class Resource {
    init {
        acquired++
    } // Acquire the resource

    fun close() {
        acquired--
    } // Release the resource
}
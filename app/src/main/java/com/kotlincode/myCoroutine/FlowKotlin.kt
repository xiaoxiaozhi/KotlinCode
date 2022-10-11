package com.kotlincode.myCoroutine

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.lang.Exception
import kotlin.system.measureTimeMillis

/**
 * https://zhuanlan.zhihu.com/p/114295411
 * [LiveData，StateFlow，SharedFlow对比](https://juejin.cn/post/7007602776502960165)
 * kotlin flow
 * 一个挂起函数异步返回一个值，但我们想要返回多个值？这时候就要用到Flow
 * Sequences yield(产出)一个值，forEach(遍历一个值)，Sequences不执行完，遍历就会一直等待是同步过程。与它相比
 * 1. 利用序列在不阻塞主线程情况下，一个一个返回元素
 * 2. Sequence 无法使用delay
 * 3. 背压问题在生产者的生产速率高于消费者的处理速率的情况下出现
 * note:5. 在flow生成元素的逻辑代码中 修改上下文是不允许的,直接运行报错
 * note:1. flow在不指定协程(flowOn)的情况下,逻辑代码和终端操作处于同意协程中，逻辑代码就算写在 Default 协程中，也不顶用
 * TODO callBackFlow{}构建器、
 *
 */
private fun log(message: String) = println("[${Thread.currentThread().name}] $message")

fun main() {
    //1. 为什么 在协程中使用Flow而不是Sequence
    runBlocking{
        log("runBlocking")
        val f = withContext(Dispatchers.Default) {
//            flow { // flow builder
//                for (i in 1..5) {
//                    log("flow $i")
//                    delay(1000)
//                    emit(i) // emit next value flow在不指定协程(flowOn)的情况下,逻辑代码和终端操作处于同一协程中，逻辑代码就算写在 Default 协程中，也不顶用
//                }
//            }

            sequence {
                log("sequence---1")
                yield(1)
                log("sequence---2")
                yield(2)

            }
        }
//        f.collect { log("$it") }//flow逻辑代码执行协程，只能通过flowOn()指定，如果不指定flow逻辑代码运行在 终端操作所在的协程。例如f.collect()
        f.forEach {
            log("试试能不能异步----$it")
        }
    }
    //2. Flow的几种创建
    println("-----createFlows------")
    createFlows()
    //3. Flow冷流
    println("-----coldFlow------")
    coldFlow()
    //4.取消流
    println("-----cancelFlow------")
//    cancelFlow()
    //5. 捕获异常
    println("-----flowException------")
    flowException()
    //6. Completion
    println("-----flowCompletion------")
    flowCompletion()
    //7. 中间操作
    println("-----middleFlow------")
    middleFlow()
    //8. flow上下文
    println("-----contextFlow------")
    contextFlow()
    //9. flow缓冲区，解决背压
    println("-----bufferFlow------")
    bufferFlow()
    //10. flow合并，解决背压
    println("-----conflateFlow------")
    conflateFlow()
    //11. zip组合flow
    println("-----zipFlow------")
    zipFlow()
    //12. 发射前执行操作
    println("-----eachFlow------")
    eachFlow()
    //13. combine组合flow
    println("-----combineFlow------")
    combineFlow()
    //14. 消除嵌套flow
    println("-----flatteningFlows------")
    flatteningFlows()
    //15. 并发消除嵌套flow
    println("-----flatteningFlows------")
    flatMapMergeFlow()
    //16 并发消除嵌套，收集端只处理最新值
    println("-----flatMapLatestFlow------")
    flatMapLatestFlow()
    //17. 终端操作符
    println("-----terminalFlow------")
    terminalFlow()

}


fun flowSimple(): Flow<Int> = flow { // flow builder
    for (i in 1..5) {
//        delay(100) // pretend we are doing something useful here
        log("flow $i")
        delay(1000)
        emit(i) // emit next value
    }
}

fun flowSimple1(): Flow<Int> = flow { // flow builder
    for (i in 1..3) {
//        delay(100) // pretend we are doing something useful here
        Thread.sleep(100)
        println("flowSimple1-----$i")
        emit(i) // emit next value
    }
}

/**
 * 受 RestrictsSuspension 注解的约束,序列生成器中不能调用其他挂起函数,不能指定线程，用Flow代替Sequence
 */
suspend fun sequenceSimple(): Sequence<Int> = sequence { // flow builder
    for (i in 1..10) {
//        delay(100)
        yield(i) // emit next value
    }
}

//1. Flow的几种创建
fun createFlows() {
    //1.1 元素创建Flow
    flowOf(1, 2)
    //1.2 代码块创建 Flow
    flow<Int> {
        emit(1)
        emitAll(flowOf(2, 3))
    }
    //1.3 列表转换
    listOf<Int>(1, 2).asFlow()
    arrayOf(1, 2).asFlow()
    (1..12).asFlow()
}

//2.1 Flow冷流
fun coldFlow() {//一个 Flow 创建出来之后，不消费则不生产，多次消费则多次生产，生产和消费总是相对应的。
    runBlocking {
        val flow = flowSimple1()
        flow.collect(::println)//Flow 可以被重复消费
        flow.collect(::println)
    }
}

// flow的取消和协程一样
fun cancelFlow() {// 协程取消，协程下的flow也会取消
    runBlocking {
        try {
            withTimeout(100) {
                flowSimple().collect(::println)
            }
        } catch (e: Exception) {
            println(e.message)
        }
    }
    println("--------------------------------")
    runBlocking {//取消检查
        flow {
            (1..5).forEach {
                println("cancelFlow----emit $it")
                emit(it)//emit 在发射前对 ensureActive(该状态取决于是否调用cancel())检查，如果发现已经被取消。则直接抛出异常
            }
        }.collect { value ->//如果不用cancellable ，使用.onEach { currentCoroutineContext().ensureActive() 抛出异常}阻止flow发射
            if (value == 3) cancel()
            println(value)
        }
    }
    runBlocking<Unit> {// 然而出于性能原因，大多数其他流运算符不会自己执行额外的取消检查，例如扩展函数得到的flow
        (1..5).asFlow()
            .cancellable()//为了避免这种情况，在收集前执行是 cancellable()，这是最简单的方法
            .onEach { currentCoroutineContext().ensureActive() }//为了避免这种情况您可以 在收集前执行 .onEach { currentCoroutineContext().ensureActive() }，TODO 没看懂为什么返回false 就不发射了
            .collect { value ->
                if (value == 3) cancel()//由于不检查仍然发射
                println("onEach检查取消------$value")
            }
    }
}


//3. 捕获异常
fun flowException() {
    //TODO 官网解释，在try catch 块中 使用flow{}构建器发射值，违反了异常透明性。想不明白这个异常透明性是什么东西
    runBlocking {
        flow {
            emit(1)
            throw ArithmeticException("Div 0")
        }.catch { //Flow 的设计初衷是希望确保流操作中异常透明,try ... catch ... finally违背原则，不推荐
            println("caught error: $it")//遵守异常透明性的 catch 中间运算符只捕获上游异常. 例如 map写在catch下面就捕获不了map中的异常
//            emit("Caught $it")// catch 能把异常当做值重新发射出去
        }.onCompletion {//在  flow 收集、取消、异常之后执行类似finally， onCompletion 和 catch的执行顺序看谁先被调用，
            //与catch不同，onCompletion既能接收到发射异常也能接收到收集异常， onCompletion有个可空参数Throwable 用来确定收集(或发射)到底是异常还正常完成
            println("finally.")
        }.collect(::println)
    }
    println("----------------------------------------------------------------")
    runBlocking {//官方例子
        flow<Int> {
            (1..3).forEach {
                println("Emitting $it")
                emit(it)
            }
        }.map { value ->
            check(value <= 1) { "Crashed on $value" }
            "string $value"

        }.catch { emit("Caught $it") } // emit on exception
            .collect { value -> println(value) }
    }
    println("----------------------------------------------------------------")
    runBlocking {//catch是中间操作符，如果异常发生在collect中，则不能捕获此时可以把收集端的代码移到onEach中执行,collect什么都不做
        flow<Int> {
            (1..3).forEach {
                println("Emitting $it")
                emit(it)
            }
        }.onEach { value ->
            check(value <= 1) { "Collected $value" }
            println("onEach----$value")//onEach 要在catch前面才起作用
        }.catch { println("Caught $it") }.collect()

    }
}

//6. flow 完成时
fun flowCompletion() {
    //当flow执行终端操作之后，可能需要执行一个操作，可以通过两种方式 try finally 和 onCompletion操作符
    runBlocking {
        //6.1 在终端操作外面包上try finally
        try {
            (6..8).asFlow().collect { println("$it") }
        } finally {
            println("flow------finally")
        }
        //6.2使用 中间操作 onCompletion,该运算符在终端操作结束时调用. 使用这个操作符的优点是 onCompletion有个可空参数Throwable 用来确定收集(或发射)到底是异常还正常完成
        (9..12).asFlow()
            .onCompletion { cause -> println("flow-----onCompletion------${cause?.message}") }
            .collect { println("$it") }
    }

}

//4. 中间操作
fun middleFlow() {
    runBlocking {
        //4.1  map filter 与其他中间操作符最大的区别是 前者是挂起函数
        flowOf(1, 2).map {
            println("flow map---$it")
            it.toInt()
        }.collect { }
        //4.2 transform 创建了一个新流,类似于map，transform不仅对传进来的元素做修改，还能emit新的元素
        (4..7).asFlow().transform {
            println("--------")
            emit("emit $it")
            emit(1)
            println("--------")
        }.collect(::println)
//        println("flow num $c")
        //4.3 take 限制大小
        (1..3).asFlow().take(2).collect(::print)
        //4.4 onEach
//        返回一个流，该流在上游流的每个值向下游发出之前调用给定的操作
    }

}

//5. 末端操作符
fun terminalFlow() {
    runBlocking {
        //4.1 集合转换函数如toList toSet fist
        //4.2 reduce  累加函数
        println((1..3).asFlow().reduce { a, b -> a + b })
        //4.3 single 如果flow有多个元素就会报错,flow为空流抛出NoSuchElementException，flow包含多个元素的流抛出IllegalStateException。
        println(flow { emit(1) }.first())
        //4.4 fold类似reduce 可以设置初始值
        println((1..3).asFlow().fold(1) { a, b -> a + b })
        //4.5 collect 挂起函数，收集消耗flow；launchIn非挂起函数，新建协程执行，功能同collect(不阻塞原有协程)
        (1..3).asFlow().launchIn(this)//查看源码，发现收集操作在新建子协程中进行，注意launchIn还返回一个job，该job只用于取消collect子协程
    }
}

//5. flow的上下文
fun contextFlow() {
    //5.1 默认情况下，运行在外部协程的上下文中，也可以指定上下文
    //5.2 在flow生成元素的逻辑代码中 修改上下文是不允许的,直接运行报错
    flow {
        withContext(Dispatchers.Default) {
            println("sadas")//打印后发现不执行
        }
        emit(1)//emit放在修改上下文的协程中会报错
    }
    runBlocking {
        flow {
            for (i in 1..3) {
                delay(100) // pretend we are asynchronously waiting 100 ms
                emit(i) // emit next value
            }
        }.collect() { log(it.toString()) }//flow的逻辑代码运行在指定的上下文中 flowOn(Dispatchers.IO)，collect末端操作已经是流返回结果
//                运行在外部协程设置上下文中，在这里是主线程
    }
    runBlocking {
        (12..15).asFlow().cancellable()
            .map { log("flowOn之前的map----$it") }
            .flowOn(Dispatchers.Default)//flowOn 的上游数据运行在flowOn指定的协程中，下游数据以及collect不受影响
            .map { log("flowOn之后的map---$it") }.collect { log("收集") }
    }

}

//6. flow缓冲区 也是并发
fun bufferFlow() {
    val f = flow {
        for (i in 1..3) {
            delay(100) // pretend we are asynchronously waiting 100 ms
            emit(i) // emit next value
        }
    }
    runBlocking {
        val t = measureTimeMillis {
            f.collect {
                delay(50)
                println(it)
            }
        }
        println("未使用缓冲区耗时$t")
    }

    runBlocking {
        val t = measureTimeMillis {
            f.buffer().collect {
                delay(50)
                println(it)
            }
        }
        println("使用缓冲区耗时$t")
    }//buffer操作符可以使发射和收集的代码并发运行，收集端来不及消费的元素放在缓冲区，耗时取决于发射端和收集端最大时长，从而提高效率
}

//7. 合并
fun conflateFlow() {
    //7.1 conflate 发射端产生一个值会交给收集端，收集端处理，如果收集端处理每一个值的时间比发射端长；
    // 收集端处理一个值之后，就只会处理最新接收的，这里只打印0和99。
    runBlocking {
        flow {
            List(100) {
                emit(it)
            }
        }.conflate().collect { value ->
            println("Collecting $value")
            delay(100)
            println("$value collected")
        }
    }
    println("-----------------")
    //7.2 发射端产生的每一个数据都会被收集端处理，但是 发射端新产生一个值，收集端会立即结束手头工作，去处理新的数据。
    runBlocking {
        flow {
            List(3) {
                emit(it)
            }
        }.collectLatest { value ->
            println("Collecting $value")
            delay(100)
            println("$value collected")
        }
    }
}

//9. 重新产生一个flow并在发射前执行操作
fun eachFlow() {
    runBlocking {
        val flow = flow<Int> {
            (1..3).forEach {
                emit(it)
                delay(100)
            }
        }.onEach { println(it) }
        flow.collect()
//        flow.launchIn(this)// 指定协程作用域
    }
}

//10. 组合zip
fun zipFlow() {
    runBlocking {
        val numb = (1..3).asFlow().onEach { delay(200) } // numbers 1..3
        val strs = flowOf("one", "two", "three") // strings
        numb.zip(strs) { a, b -> "$a -> $b" } // compose a single string
            .collect { println(it) } // collect and print
    }

}

//11. combine 组合
fun combineFlow() {
    runBlocking {
        val numb = (1..3).asFlow().onEach { delay(1) } // numbers 1..3
        val strs = flowOf("one", "two", "three").onEach { delay(400) } // strings
        numb.combine(strs) { a, b -> "$a -> $b" } // compose a single string
            .collect { println(it) } // collect and print
    }
}//与zip不同，第一次两个流都发射，收集端才会执行。之后任何一个flow发射，收集端都会执行，这时取另一个flow缓冲区最近的值

//12. 消除嵌套flow
fun flatteningFlows() {
    runBlocking {
        val startTime = System.currentTimeMillis() // remember the start time
        (1..3).asFlow().onEach { delay(100) } // a number every 100 ms
            .flatMapConcat { requestFlow(it) }
            .collect { value -> // collect and print
                println("$value at ${System.currentTimeMillis() - startTime} ms from start")
            }
    }
}

fun requestFlow(i: Int): Flow<String> = flow {
    emit("$i: First")
    delay(500) // wait 500 ms
    emit("$i: Second")
}

//13 并发消除嵌套flow
fun flatMapMergeFlow() {//发射端不等待消费端消费，
    runBlocking {
        val startTime = System.currentTimeMillis() // remember the start time
        (1..3).asFlow().onEach { delay(100) } // a number every 100 ms
            .flatMapMerge { requestFlow(it) }//flattenMerge 功能相同
            .collect { value -> // collect and print
                println("$value at ${System.currentTimeMillis() - startTime} ms from start")
            }
    }
}

//14 并发消除嵌套+collect收集端只处理最新值
fun flatMapLatestFlow() {
    runBlocking {
        val startTime = System.currentTimeMillis() // remember the start time
        (1..3).asFlow().onEach { delay(100) } // a number every 100 ms
            .flatMapLatest { requestFlow(it) }
            .collect { value -> // collect and print
                println("$value at ${System.currentTimeMillis() - startTime} ms from start")
            }
    }
}

//15 StateFlow
fun stateFlow() {
    runBlocking {
        val state = MutableStateFlow(1)//

    }
}
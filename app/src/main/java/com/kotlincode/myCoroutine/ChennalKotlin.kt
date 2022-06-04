package com.kotlincode.myCoroutine

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.*

/**
 * https://zhuanlan.zhihu.com/p/83591180
 * 通道在概念上与 BlockingQueue 非常相似
 * 1. Deferred<T>能够在协程之间传递值，如果值是一连串的数字怎么传递呢？Channel能够在不同协程之间用流的方式传递值
 * 2. 同flow一样是冷数据，没有消费就不生产数据流
 * 3. send和(receive、for 、consumeEach等接收操作)都是挂起函数，前者当通道被占满就会挂起，后者通道没有数据就会挂起等待
 * 4. 扇入扇出，向信道发送和接收操作对于从多个协程调用它们的顺序是公平的。它们按照先入先出的顺序服务
 * 5. ticker()每隔指定的时间就会向通道发送一个Unit
 */
private fun log(message: String) = println("[${Thread.currentThread().name}] $message")
fun main() {
    //1. 传值示例
    println("-----example-----")
    example()
    //2. 关闭通道和迭代
    println("-----closeAndIterator-----")
    closeAndIterator()
    //3. channel构建起 produce
    println("-----buildChannel-----")
    buildChannel()
    //4. 多协程生产消费最后取消示例
    println("-----gcc-----")
    gcc()
    //5. 扇出 一对多
    println("-----fanOut-----")
    fanOut()
    //6. 扇入 多对一
    println("-----fanIn-----")
    fanIn()
    //7. tickChannel
    println("-----tickChannel-----")
    tickChannel()
}

//1.通道传值示例
fun example() {
    runBlocking {
        val channel = Channel<Int>(1)//capacity 容量，当产生的数据大于这个值没有消费就会挂起，从本例来看发送发在发送第二个元素时挂起
        launch {
            for (x in 1..5) {
                println("send1 $x")
                channel.send(x)//从打印结果看，生产超过缓冲区大小，send就会被挂起，直到channel被消费
                println("send2 $x")
            }
        }
        // here we print five received integers:
        repeat(5) {
            delay(100)
            println("receive ${channel.receive()}")
        }
        println("Done!")
    }
}

//2. 关闭通道和迭代
fun closeAndIterator() {
    runBlocking {
        val channel = Channel<Int>(1)
        launch {
            for (x in 1..5) {
                if (x == 3) {
                    break
                }
                channel.send(x * x)
            }
            channel.close() // we're done sending
            log("close channel. ClosedForSend = ${channel.isClosedForSend} ClosedForReceive = ${channel.isClosedForReceive}")
        }

        for (y in channel) println(y)//channel的迭代是一个接收操作，channel没有数据就会挂起，一直到有send发往通道
        println("Done!")
        log("close channel. ClosedForSend = ${channel.isClosedForSend} ClosedForReceive = ${channel.isClosedForReceive}")
        //由于 Channel 缓冲区的存在，这时候可能还有一些元素没有被处理完，所以要等所有的元素都被读取之后 isClosedForReceive 才会返回 true。
    }
}

//3. channel构建器
fun buildChannel() {
    runBlocking {
        val squares = produceSquares()//创建一个新的协程，将一系列生成的值发送给通道，最后返回ReceiveChannel
        squares.consumeEach { println(it) }//每个接收到的元素执行给定的操作，并在执行块后取消通道,该函数式挂起函数,在多个协程中运行没有for循环安全
        println("Done!")
    }
}

fun CoroutineScope.produceSquares(): ReceiveChannel<Int> = produce {
    for (x in 1..5) {
        send(x * x)
    }
}

//4.生产消费并取消示例
fun gcc() {
    runBlocking {
        val pn = produceNumber()
        val square = square(pn)
        repeat(5) {
            println(square.receive())
        }
        coroutineContext.cancelChildren()//结果发现协程取消后，生产者后消费者也都取消
//        println(square.receive())
    }
}

fun CoroutineScope.produceNumber() = produce<Int> {//同flow一样是冷数据，没有消费就不生产数据流
    var x = 1
    while (true) {
        send(x++)
        println("produce $x")
    }
}

fun CoroutineScope.square(rc: ReceiveChannel<Int>) = produce<Int> {
    rc.consumeEach { send(it * it) }
}

//5. 扇出 一对多 一个生产者对多个消费者
fun fanOut() {
    runBlocking {
        val receiveChannel = produce<Int> {
            var x = 1 // start from 1
            while (true) {
                send(x++) // produce next
                delay(100) // wait 0.1s
            }
        }
        repeat(5) {
            launch {
                for (msg in receiveChannel) {//与consumeEach不同的是，这个for循环在多个使用中使用非常安全。
                    println("Processor #$it received $msg")
                }
            }
        }
        delay(1000)
        receiveChannel.cancel()//关闭通道,并删除缓冲区所有元素
    }
}

//6. 扇入 多对一  多个生产者对一个消费者
fun fanIn() {
    runBlocking {
        val channel = Channel<String>()
        launch { sendString(channel, "foo", 200L) }
        launch { sendString(channel, "BAR!", 500L) }
        repeat(6) { // receive first six
            println(channel.receive())
        }
        coroutineContext.cancelChildren() // cancel all children to let main finish
    }
}

suspend fun sendString(channel: SendChannel<String>, s: String, time: Long) {
    while (true) {
        delay(time)
        channel.send(s)
    }
}

//7. tickChannel
fun tickChannel() {//
    runBlocking {
        val tickerChannel =
            ticker(delayMillis = 100, initialDelayMillis = 0) // create ticker channel
        var nextElement: Unit? = withTimeout(1) { tickerChannel.receive() }
        println("Initial element is available immediately: $nextElement") // no initial delay
        nextElement =
            withTimeoutOrNull(50) { tickerChannel.receive() } // all subsequent elements have 100ms delay
        println("Next element is not ready in 50 ms: $nextElement")

        nextElement = withTimeoutOrNull(60) { tickerChannel.receive() }
        println("Next element is ready in 100 ms: $nextElement")

        // Emulate large consumption delays
        println("Consumer pauses for 150ms")
        delay(150)
        // Next element is available immediately
        nextElement = withTimeoutOrNull(1) { tickerChannel.receive() }
        println("Next element is available immediately after large consumer delay: $nextElement")
        // Note that the pause between `receive` calls is taken into account and next element arrives faster
        nextElement = withTimeoutOrNull(60) { tickerChannel.receive() }
        println("Next element is ready in 50ms after consumer pause in 150ms: $nextElement")

        tickerChannel.cancel() // indicate that no more elements are needed
    }
}
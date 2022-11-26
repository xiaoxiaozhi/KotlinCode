package com.kotlincode.myCoroutine

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.*

/**
 * [一个英国人写的博客介绍Channel](https://kt.academy/article/cc-channel)
 * ![看图更好理解](https://kt.academy/_next/image?url=https%3A%2F%2Fmarcinmoskala.com%2Fcoroutines_book%2Fmanuscript%2Fresources%2Fchannel_mimo.png&w=1200&q=75)
 * channel支持任意数量的发送方和接收方，发送到信道的每个值只能被接收一次。
 * 查看源码 Channel 是一个实现另外两个接口的接口: public interface Channel<E> : SendChannel<E>, ReceiveChannel<E>
 * channel.send 和 channel.receive 是挂起点函数
 * 当channel中没有数据 receive将挂起所处协程等待，接收到数据才会往下走
 * 当信道达到其容量时，send 将被暂停
 * 所以 send发送一个数数据 receive接收一个数据协程才会往下走，就像delay(1000)延迟时间结束往下走，withContext(){}代码块执行完才会往下走一样
 * channelExample()是channel的简单示例，然而大部分时候接收方根本不知道channel到底有多少个值，所以我们直接监听Channel例如 for (element in channel) {println(element)}或者channel.consumeEach { element ->println(element)}
 * 这种发送元素的方法的常见问题是很容易忘记关闭通道，特别是在异常的情况下。如果一个协程因为异常而停止生成，另一个将永远等待元素。使用 product 函数避免这种情况，它新建了一个协程并且返回 ReceiveChannel
 * 如果需要从非挂起函数发送或接收，可以使用 trySend 和 tryReceive。这两个操作都是即时的，并返回 ChannelResult，其中包含有关操作成功或失败的信息及其结果。
 * 仅对容量有限的通道使用 trySend 和 tryRecept，因为它们不适用于会合通道。
 *
 *channel 构造函数参数
 *1. capacity Channel类型
 *   Unlimited：具有无限缓冲区的通道
 *   Buffered：具有具体容量大小的通道 Channel.BUFFERED 默认为64
 *   Rendezvous：默认设置 通道容量为0 Channel.RENDEZVOUS
 *   Conflated：通道容量为1 Channel.CONFLATED  生产端新的元素将替换以前的元素，接收端只能接收最后一个元素
 *   直接在 Channel 上设置Channel<T>(Channel.CONFLATED)，但也可以在调用 produce时设置  produce(capacity = Channel.UNLIMITED){}
 *2. onBufferOverflow 控制缓冲区满时发生的情况
 *   SUSPEND (default) - 当缓冲区满的时候，挂起send所在协程
 *   DROP_OLDEST         当缓冲区满的时候，删除最老元素 与相同 Channel.CONFLATION
 *   DROP_LATEST         当缓冲区满的时候，删除最新元素
 *   produce 函数不允许我们设置自定义 onBufferOverflow，因此要设置它，我们需要构造函数 Channel<Int>(capacity = 2,onBufferOverflow = BufferOverflow.DROP_OLDEST)
 *3. onUndeliveredElement
 *   当某个元素由于某种原因无法处理时调用，这通常意味着通道被关闭或取消，当send, receive, receiveOrNull, or hasNext 抛出错误时也可能发生这种情况。我们通常使用它来关闭由此通道发送的资源。
 *
 * 扇出
 * 多个协程可以从单个通道接收; 然而，为了正确地接收它们，我们应该使用 for 循环(consumeEach 在多个协程中使用是不安全的)
 * 扇入
 * 多个协同程序可以发送到单个通道
 *
 * Pipelines
 * 有时，我们设置两个通道，其中一个通道根据从另一个通道接收的元素生成元素。在这种情况下，我们称之为管道。
 *
 * Channel是公平的
 *
 * 最好使用channelFlow or callbackFlow
 */
fun main() {
    runBlocking {
        val startTime = System.currentTimeMillis()
//        channelExample()
        val channel = produce(capacity = Channel.CONFLATED) {
            try {
                repeat(5) { index ->
                    delay(100)
//                    if (index == 2) throw java.lang.IndexOutOfBoundsException("抛出一个异常")
                    send(index)
                    Util.log("Producing next one $index at ${System.currentTimeMillis() - startTime}")
                }
            } catch (e: Exception) {
                Util.log("${e.message}")
            }

        }

        for (element in channel) {
            Util.log("$element at ${System.currentTimeMillis() - startTime}")
            delay(1000)
        }
        Util.log("Done!")
    }


    //扇入
    runBlocking {
        val channel = Channel<String>()
        launch { sendString(channel, "foo", 200L) }
        launch { sendString(channel, "BAR!", 500L) }
        repeat(5) {
            println(channel.receive())
        }
        coroutineContext.cancelChildren()
    }

    //扇出
    runBlocking {
        val channel = produce {
            repeat(10) {
                delay(100)
                send(it)
            }
        }
        repeat(3) { id ->
            delay(10)
            launch {
                for (msg in channel) {
                    println("#$id received $msg")
                }
            }
        }

    }
}

//1.
fun channelExample() {
    runBlocking {
        val channel = Channel<Int>(1)//capacity 容量，当产生的数据大于这个值没有消费就会挂起，从本例来看发送发在发送第二个元素时挂起
        launch {
            try {
                for (x in 1..5) {
                    delay(1000)
                    if (x == 2) throw java.lang.IndexOutOfBoundsException("抛出一个异常")
                    channel.send(x)//从打印结果看，生产超过缓冲区大小，send就会被挂起，直到channel被消费
                    Util.log("send $x")
                }
            } catch (e: Exception) {
                Util.log("${e.message}")
//                channel.close()
            }
        }
        launch {
            for (i in channel) {
                Util.log("监听通道1 ${channel.receive()}")
            }
            Util.log("监听结束")
        }
        Util.log("Done!")
    }
}

fun closeResource() {
    Channel<Resource>(Channel.RENDEZVOUS) { resource ->
        resource.close()
    }
    //或者
//    val resourceToSend = openResource()
//    channel.send(resourceToSend)//举例这是要传进去的资源，如果这个资源失败在onUndeliveredElement 取消或者其他操作
    Channel<Resource>(
        capacity = Channel.RENDEZVOUS,
        onUndeliveredElement = { resource ->
            resource.close()
        }
    )
}


////2. 关闭通道和迭代
//fun closeAndIterator() {
//    runBlocking {
//        val channel = Channel<Int>(1)
//        launch {
//            for (x in 1..5) {
//                if (x == 3) {
//                    break
//                }
//                channel.send(x * x)
//            }
//            channel.close() // we're done sending
//            log("close channel. ClosedForSend = ${channel.isClosedForSend} ClosedForReceive = ${channel.isClosedForReceive}")
//        }
//
//        for (y in channel) println(y)//channel的迭代是一个接收操作，channel没有数据就会挂起，一直到有send发往通道
//        println("Done!")
//        log("close channel. ClosedForSend = ${channel.isClosedForSend} ClosedForReceive = ${channel.isClosedForReceive}")
//        //由于 Channel 缓冲区的存在，这时候可能还有一些元素没有被处理完，所以要等所有的元素都被读取之后 isClosedForReceive 才会返回 true。
//    }
//}
//
////3. channel构建器
//fun buildChannel() {
//    runBlocking {
//        val squares = produceSquares()//创建一个新的协程，将一系列生成的值发送给通道，最后返回ReceiveChannel
//        squares.consumeEach { println(it) }//每个接收到的元素执行给定的操作，并在执行块后取消通道,该函数式挂起函数,在多个协程中运行没有for循环安全
//        println("Done!")
//    }
//}
//
//fun CoroutineScope.produceSquares(): ReceiveChannel<Int> = produce {
//    for (x in 1..5) {
//        send(x * x)
//    }
//}
//
////4.生产消费并取消示例
//fun gcc() {
//    runBlocking {
//        val pn = produceNumber()
//        val square = square(pn)
//        repeat(5) {
//            println(square.receive())
//        }
//        coroutineContext.cancelChildren()//结果发现协程取消后，生产者后消费者也都取消
////        println(square.receive())
//    }
//}
//
//fun CoroutineScope.produceNumber() = produce<Int> {//同flow一样是冷数据，没有消费就不生产数据流
//    var x = 1
//    while (true) {
//        send(x++)
//        println("produce $x")
//    }
//}
//
//fun CoroutineScope.square(rc: ReceiveChannel<Int>) = produce<Int> {
//    rc.consumeEach { send(it * it) }
//}
//
////5. 扇出 一对多 一个生产者对多个消费者
//fun fanOut() {
//    runBlocking {
//        val receiveChannel = produce<Int> {
//            var x = 1 // start from 1
//            while (true) {
//                send(x++) // produce next
//                delay(100) // wait 0.1s
//            }
//        }
//        repeat(5) {
//            launch {
//                for (msg in receiveChannel) {//与consumeEach不同的是，这个for循环在多个使用中使用非常安全。
//                    println("Processor #$it received $msg")
//                }
//            }
//        }
//        delay(1000)
//        receiveChannel.cancel()//关闭通道,并删除缓冲区所有元素
//    }
//}
//
////6. 扇入 多对一  多个生产者对一个消费者
//fun fanIn() {
//    runBlocking {
//        val channel = Channel<String>()
//        launch { sendString(channel, "foo", 200L) }
//        launch { sendString(channel, "BAR!", 500L) }
//        repeat(6) { // receive first six
//            println(channel.receive())
//        }
//        coroutineContext.cancelChildren() // cancel all children to let main finish
//    }
//}
//
//suspend fun sendString(channel: SendChannel<String>, s: String, time: Long) {
//    while (true) {
//        delay(time)
//        channel.send(s)
//    }
//}
//
////7. tickChannel
//fun tickChannel() {//
//    runBlocking {
//        val tickerChannel =
//            ticker(delayMillis = 100, initialDelayMillis = 0) // create ticker channel
//        var nextElement: Unit? = withTimeout(1) { tickerChannel.receive() }
//        println("Initial element is available immediately: $nextElement") // no initial delay
//        nextElement =
//            withTimeoutOrNull(50) { tickerChannel.receive() } // all subsequent elements have 100ms delay
//        println("Next element is not ready in 50 ms: $nextElement")
//
//        nextElement = withTimeoutOrNull(60) { tickerChannel.receive() }
//        println("Next element is ready in 100 ms: $nextElement")
//
//        // Emulate large consumption delays
//        println("Consumer pauses for 150ms")
//        delay(150)
//        // Next element is available immediately
//        nextElement = withTimeoutOrNull(1) { tickerChannel.receive() }
//        println("Next element is available immediately after large consumer delay: $nextElement")
//        // Note that the pause between `receive` calls is taken into account and next element arrives faster
//        nextElement = withTimeoutOrNull(60) { tickerChannel.receive() }
//        println("Next element is ready in 50ms after consumer pause in 150ms: $nextElement")
//
//        tickerChannel.cancel() // indicate that no more elements are needed
//    }
//}
package com.kotlincode.myCoroutine

import kotlin.String
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.*

/**
 * [一个英国人写的博客介绍Channel](https://kt.academy/article/cc-channel)
 * 通道在概念上与 BlockingQueue 非常相似
 * channel支持任意数量的发送方和接收方，发送到信道的每个值只能被接收一次。
 * 查看源码 Channel 是一个实现另外两个接口的接口: public interface Channel<E> : SendChannel<E>, ReceiveChannel<E>
 * channel.send、channel.receive、consumeEach、迭代 是挂起点函数
 * 当channel中没有数据 receive将挂起所处协程等待，接收到数据才会往下走
 * 当信道达到其容量时，send 将被暂停
 * 所以 send发送一个数数据 receive接收一个数据协程才会往下走，就像delay(1000)延迟时间结束往下走，withContext(){}代码块执行完才会往下走一样
 * 1.传值示例
 *   example()第一个协程使用channel发送数据，send是挂起函数，当通道满的时候协程挂起。第二个协程接受数据，当通道是空的时候挂起协程。
 *   然而大部分时候接收方根本不知道channel到底有多少个值，所以我们直接监听Channel例如 for (element in channel) {println(element)}或者channel.consumeEach { element ->println(element)}
 *   send
 * 2.关闭通道
 *   close关闭通道channel有两个值isClosedForSend、isClosedForReceive，判断关闭后发送和接收操作是否还再执行。
 *   channel.close()之后isClosedForSend 立马为true 由于通道里还有数据没接收完，isClosedForReceive为false。
 *   channel.close执行过后 继续send就会报异常。如果通道里面还有数据没取出，receive则能正常取出，等通道数据取出完毕。如果是迭代或consumeEach则会结束，如果继续receive则会报错
 *   (close方法我觉得用处就是不让通道继续send数据，receive数据。以及解除迭代或consumeEach)
 * 3.channel构建器 produce
 *   在协程中构建通道的模式非常常见，kotlin把他封装成produce方法，它在协程中创建一个channel(produce{send(元素)})并且返回ReceiveChannel用来receive数据或者迭代
 *   example()的channel创建方法很容易忘记在异常的情况下channel。如果一个协程因为异常而停止生成，receive所处的协程将永远等待元素。使用 product 函数避免这种情况
 *   receiveChannel.cancel()//关闭通道,并删除缓冲区所有元素
 *   相应的 sendChannel = actor{在这里接收 receive()}  sendChannel.send(发送元素) 创建的是一个SendChannel，用于发送数据的通道
 * 4.Pipelines
 *   我们设置两个通道，其中一个通道根据从另一个通道接收的元素生成元素(不止两个)
 * 5.扇出
 *   多个协程可以从单个通道接收; 然而，为了正确地接收它们，我们应该使用 for 循环(consumeEach 在多个协程中使用是不安全的)。
 *   channel是公平的分配数据遵循FIFO (先进先出)原则。按顺序创建三个协程，channel按照顺序给它们推送数据。0号接1、1号接2、2号接3、0号接4........
 * 6.扇入
 *   同上 过程反过来
 * 7.定时器通道
 *   ticker{} 计时器通道是一种特别的会合通道，每次经过特定的延迟都会从该通道进行消费并产生 Unit。  TODO 待总结
 *
 *-----------------------------------https://kt.academy/article/cc-channel------------------------------------------------------------------------
 *channel 构造函数参数
 * 1.capacity Channel类型
 *   Unlimited：具有无限缓冲区的通道
 *   Buffered：具有具体容量大小的通道 Channel.BUFFERED 默认为64
 *   Rendezvous：默认设置 通道容量为0 Channel.RENDEZVOUS   0代表 通道容量是1 容量从0开始计数
 *   Conflated：通道容量为1 Channel.CONFLATED  生产端新的元素将替换以前的元素，接收端只能接收最后一个元素
 *   直接在 Channel 上设置Channel<T>(Channel.CONFLATED)，但也可以在调用 produce时设置  produce(capacity = Channel.UNLIMITED){}
 * 2.onBufferOverflow 控制缓冲区满时发生的情况
 *   SUSPEND (default) - 当缓冲区满的时候，挂起send所在协程
 *   DROP_OLDEST         当缓冲区满的时候，删除最老元素 与相同 Channel.CONFLATION
 *   DROP_LATEST         当缓冲区满的时候，删除最新元素
 *   produce 函数不允许我们设置自定义 onBufferOverflow，因此要设置它，我们需要构造函数 Channel<Int>(capacity = 2,onBufferOverflow = BufferOverflow.DROP_OLDEST)
 * 3.onUndeliveredElement
 *   当某个元素由于某种原因无法处理时调用，这通常意味着通道被关闭或取消，当send, receive, receiveOrNull, or hasNext 抛出错误时也可能发生这种情况。我们通常使用它来关闭由此通道发送的资源。
 *
 * [溢栈上有答案说flow和channel最大区别是他们的用法一个是冷流一个是热流，flow用处比channel多](https://stackoverflow.com/questions/59412793/kotlin-coroutines-channel-vs-flow)
 * 1. Deferred<T>能够在协程之间传递值，如果值是一连串的数字怎么传递呢？Channel能够在不同协程之间用流的方式传递值
 * 2. 同flow一样是冷数据，没有消费就不生产数据流
 * 3. send和(receive、for 、consumeEach等接收操作)都是挂起函数，前者当通道被占满就会挂起，后者通道没有数据就会挂起等待
 * 4. 扇入扇出，向信道发送和接收操作对于从多个协程调用它们的顺序是公平的。它们按照先入先出的顺序服务
 * 5. ticker()每隔指定的时间就会向通道发送一个Unit
 *
 * TODO trySend 和 tryReceive是什么英国人写的博客中介绍
 * TODO 最好使用channelFlow or callbackFlow
 *
 *
 */
private fun log(message: String) = println("[${Thread.currentThread().name}] $message")
fun main() {
    //1. 传值示例
    println("-----example-----")
    example()
    //2. 关闭通道和迭代
    println("-----closeAndIterator-----")
    closeAndIterator()
    //3. channel构建器 produce
    println("-----buildChannel-----")
    buildChannel()
    //4. Pipelines
    println("-----Pipelines-----")
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
                delay(200)
                channel.send(x)//从打印结果看，生产超过缓冲区大小，send就会被挂起，直到channel被消费
                Util.log("send $x")
            }
        }

        launch {
            repeat(5) {
//            delay(100)
                Util.log("receive ${channel.receive()}")
            }
        }
        //然而大部分时候接收方根本不知道channel到底有多少个值，
        // 所以我们直接监听Channel
//        launch {
//            for (element in channel) {
//                println(element)
//            }
//            //或者
//            channel.consumeEach { element -> println(element) }
//        }
        Util.log("Done!")
    }
}

//2. 关闭通道和迭代
fun closeAndIterator() {
    runBlocking {
        val channel = Channel<Int>(Channel.BUFFERED)
        launch {
            for (x in 1..5) {
                channel.send(x)
//                channel.close()//向已经关闭的通道发送数据报异常 ClosedSendChannelException:
            }
            channel.close() // 执行后channel.isClosedForSend 值立马为true
            log("close channel. ClosedForSend = ${channel.isClosedForSend} ClosedForReceive = ${channel.isClosedForReceive}")
        }

        for (y in channel) {
            delay(1000)
            println("receive $y")// close过后，channel还有数据 receive仍然能接收到数据，全部取完后迭代解除
        }//channel的迭代是一个接收操作，channel没有数据就会挂起，一直到有send发往通道
//        repeat(10) {
//            println("receive ${channel.receive()}")// close过后，channel还有数据 receive仍然能接收到数据,多取会报异常
//        }
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
    awaitClose()
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

fun CoroutineScope.produceNumber() = produce<Int> {
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
                delay(100) // wait 0.1s
                send(x++) // produce next
            }
        }
        repeat(3) {
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
        val startTime = System.currentTimeMillis()
        val tickerChannel =
            ticker(delayMillis = 100, initialDelayMillis = 0) // create ticker channel
        var nextElement: Unit? = withTimeout(1) { tickerChannel.receive() }
        println("Initial element is available immediately: $nextElement at ${System.currentTimeMillis() - startTime}") // no initial delay
        nextElement =
            withTimeoutOrNull(50) { tickerChannel.receive() } // all subsequent elements have 100ms delay
        println("Next element is not ready in 50 ms: $nextElement at ${System.currentTimeMillis() - startTime}")

        nextElement = withTimeoutOrNull(60) { tickerChannel.receive() }
        println("Next element is ready in 100 ms: $nextElement at ${System.currentTimeMillis() - startTime}")

        // Emulate large consumption delays
        println("Consumer pauses for 150ms")
        delay(150)
        // Next element is available immediately
        nextElement = withTimeoutOrNull(1) { tickerChannel.receive() }
        println("Next element is available immediately after large consumer delay: $nextElement at ${System.currentTimeMillis() - startTime}")
        // Note that the pause between `receive` calls is taken into account and next element arrives faster
        nextElement = withTimeoutOrNull(60) { tickerChannel.receive() }
        println("Next element is ready in 50ms after consumer pause in 150ms: $nextElement at ${System.currentTimeMillis() - startTime}")

        tickerChannel.cancel() // indicate that no more elements are needed
    }
}
package com.kotlincode.myCoroutine

import kotlin.String
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.*
import kotlinx.coroutines.flow.*
import java.lang.Exception

/**
 * https://zhuanlan.zhihu.com/p/114295411
 * [LiveData，StateFlow，SharedFlow对比](https://juejin.cn/post/7007602776502960165)
 * [Kotlin 协程三 —— 数据流 Flow](https://blog.csdn.net/gqg_guan/article/details/126103976)
 * kotlin flow
 * 一个挂起函数异步返回一个值，但我们想要异步返回多个值？这时候就要用到Flow
 * 1.冷流
 *   一个 Flow 创建出来之后，不消费则不生产，多次消费则多次生产，生产和消费总是相对应的。
 * 2.flow的取消
 *   构建器创造出来的协程 (fLow{}) 在发射前进行 ensureActive()取消检查，当终端操作所在协程或者作用域取消的时候，flow也会取消生产。
 *   扩展函数创造出来的协程(集合.asFlow、 flowOf) 出于性能考虑没有在发射前增加取消检查，当终端操作所在协程或者作用域取消的时候，flow继续生产。为了解决这个问题增加 cancellable()或者onEach { currentCoroutineContext().ensureActive() }
 *   Flow没有提供取消方法，可以通过取消Flow所在协程的方式来取消
 * 3.Flow的几种创建
 *   flowOf(元素、元素) 、flow{emit(元素)}、 由集合、数组、范围类 调用asFlow 转化为流
 *   TODO 元素类型不需要统一吗？？？官网没说明
 * 4.中间操作
 *   与集合的map 和 filter不同，flow的map 和 filter中可以使用挂起函数
 *   transform 相比于map更加灵活的转换操作。 创建一个新的流，接收原来流的元素可以对每个元素进行 转化、筛选、跳过、多次发送。既然是创建新的流，每一个转换后的元素都要emit()发射出去
 *   take(num) 大小限制操作 当flow产生指定个数的元素，就会取消协程，并抛出异常AbortFlowException.实验发现异常不用try catch捕获 也不会导致APP崩溃
 * 5.终端操作
 *   toList() toSet() asSequence() :转换成集合
 *   first(): 仅返回流发出的第一个元素，然后取消流的集合。如果流为空则引发 NoSuchElementException
 *   single()：适用于只有一个元素的流，返回流仅有的一个元素，如果是空流引发 NoSuchElementException，如果流能产生多个元素引发 IllegalStateException
 *   reduce():将所提供的操作应用于集合元素并返回积累的结果 例如 (a,b,c,d,e,f) 1号元素和2号元素操作结果再和 3号元素操作 比如 阶乘的运算过程就和reduce一致
 *   fold(initValue)：与reduce相比有初始值，功能类似
 *   collect:挂起点函数，需要在协程中执行
 *   launchIn:对 scope.launch{flow.collect()}的封装，返回job。方便取消
 * 6.flow上下文
 *   流元素的产生总是发生在终端操作所处的上下文(协程线程一致)。生产和终端不允许运行在不同的上下文(协程和线程均不一致)
 *   但有时候生产端产生数据是一项耗时操作需要运行在子线程中，而终端操作比如更新界面则需要运行在主线程，这时候就要使用flowOn(Dispatch.Default)解除这种限制，flowOn 操作符创建了另一个协程。
 *   个人认为 flowOn目的就是为了生产端和终端不在一个线程和协程
 *   attention：特别注意例如 flowOn前后都是中间操作，则 flowOn设置之前的生产以及中间操作运行至在flowOn指定的上下文，flowOn之后的中间操作以及终端操作运行在终端操作所处的上下文中，代码在下面
 *   attention：flowOn操作也使用了buffer()所以它改变了改变流的顺序(一个元素生产-->中间--->终端)测试发现，变成了  s s s  t t  s t s t t t(s代表生产t代表终端) 也就是不等终端消费完就又开始生产。这就是典型生产者和消费者背压问题
 * 7.flow缓冲区
 *   在一个流上使用一个buffer()操作符，生产端产生和终端消费同时进行，而不是按顺序运行它们: 生产端测试发现缓冲区大小66个元素.生产端和终端操作不再一个协程。 缓冲区默认大小64，也可以设置buffer(自定义大小) buffer(0)代表缓冲区大小1. 缓冲区是从0开始计数
 *   attention:生产端和终端属于不同的协程 buffer创建了一个协程？？？flowOn让生产端和终端操作处于不同的协程和线程
 * 8.合并
 *   conflate和buffer一样也是开辟一个缓冲区，让生产端和终端并行。不同的是，生产端只消费最新的元素。最近emit的元素 (叫合并感觉不合适)
 *   collectLatest另一种合并方法，生产端每次发出新值时取消之前的终端操作并重新启动终端。查看下面代码就会发现终端操作每次都执行，由于终端操作耗时长每次都执行不完
 *   这两种方法都能解决背压
 *   attention：collectLatest 生产端和终端处于统一线程但总是更换不同的协程，这是为什么？？？
 * 9.组合
 *   9.1 zip() 用于组合两个流中的相关值：两个流的元素都准备好之后才会结合成一个流输出，看下面代码。例如当我们想要并行进行两个网络调用，并希望两个网络调用都完成时，两个网络调用的结果都在一个回调中
 *   9.2 combine() 合并流产生第一个元素的情况同zip。第二个元素时开始不同，任意分支流产生一个新元素，合并流就会产生一个新元素。这时其它流还没有产生新元素的话就用其它流最近产生的元素。
 *   attention ：zip和combine支持多个流(看源码最多支持5个流)
 *10.展平流
 *   如果我们得到了一个包含流的流（Flow<Flow<String>>）需要将其进行展平为单个流以进行下一步处理。集合与序列都拥有 flatten 与 flatMap 操作符来做这件事。流具有异步的性质，因此需要不同的展平模式
 *   10.1 flattenConcat 消除嵌套，注意作用域是 flow<flow<T>>
 *   10.2 flatMapConcat 注意作用域是 flow<T> 。假设这样一个流flow.map{ flow{} }。返回flow<flow<T>>。如果想遍历流里面的值就要双层collect. 把map替换成flatMapConcat就可以一层collect实现遍历
 *   10.3 flatMapMerge  flow{生产端}.flatMapMerge{ map端}.collect{终端操作} 生产端和map 是运行在一个线程上的不同协程属于并发操作。只要map端emit 终端就会立即执行。
 *                      如果生产端和map端没有挂起点函数，则生产端先执行完，然后是map端
 *                      如果有挂起点函数，生产端和map端会来回切换
 *   10.4 flatMapLatest 类似collectLatest。flow{生产端}.flatMapLatest{ map端}.collect{终端操作} 生产端产生新值都会重新启动flatMapLatest
 *11.异常捕获
 *   try{ flow{}.中间操作{}.终端操作{} }catch{}。 try catch 可以接收 流生产端，中间操作 末端操作产生的异常。
 *   Flow 的设计初衷是希望确保流操作中异常透明,try ... catch ... finally违背原则，不推荐，flow有专门封装异常的方法，使用catch操作符保证异常透明性
 *   catch 操作符的代码块可以分析异常并根据捕获到的异常以不同的方式对其做出反应：
 *   可以使用 throw 重新抛出异常。
 *   可以使用 catch 代码块中的 emit 将异常转换为值发射出去。
 *   可以将异常忽略，或用日志打印，或使用一些其他代码处理它。
 *   catch 中间运算符只捕获上游异常. 例如 map写在catch下面就捕获不了map中的异常；例如 collect { ... } 块（位于 catch 之下）抛出一个异常，就捕获不了：
 *   onCompletion 在 flow 收集、取消、异常之后执行类似finally， onCompletion 和 catch的执行顺序看谁先被调用，
 *   与catch不同，onCompletion既能接收到发射异常也能接收到收集异常， onCompletion有个可空参数Throwable 用来确定收集(或发射)到底是异常还正常完成
 *12.流完成动作
 *   当流收集完成时（普通情况或异常情况）它可能需要执行一个动作有两种方式完成：命令式或声明式
 *   12.1 命令式 finally 块，使用 finally 块在 collect 完成时执行一个动作。
 *   12.2 声明式onCompletion,该运算符在终端操作结束时调用. 使用这个操作符的优点是 onCompletion有个可空参数Throwable 用来确定收集(或发射)到底是异常还正常完成，接收到null说明flow成功完成
 *13.launchIn操作符在新协程中启动流的终端操作
 *   launchIn是终端操作符，放在最后调用，调用后流的生产端和中间操作开始启动。 launchIn(协程作用域) 作用是开启新的协程
 *   launchIn 源代码就是scope.launch { flow.collect() }. 如果不使用flowOn 生产端、中间操作都运行在 收集端所在的协程中，launchIn作为终端操作符没有打印流结果的功能，为了打印流我们使用中间操作符onEach{}
 *   launchIn 返回一个 Job，可以在不取消整个作用域的情况下仅取消相应的流收集或对其进行join。
 *14.callbackFlow{}
 *   [android官网关于kotlin在Android的应用](https://developer.android.google.cn/kotlin/flow#callback)
 *   [kotlin官网callbackFlow的api介绍](https://kotlinlang.org/api/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines.flow/callback-flow.html)
 *   [赵彦军 使用callbackFlow](https://blog.csdn.net/zhaoyanjun6/article/details/121840157) 看medium觉得赵彦军写的没抓住重点，到底是什么？？？
 *   [medium](https://proandroiddev.com/callbacks-in-a-mad-world-wrapping-your-old-callback-listeners-with-callbackflow-863f9e146281)
 *   底层使用channel来进行中转，首先通过produce创建一个ReceiveChannel。然后在调用collect的时候，在将channel的值取出来emit出去。
 *   callbackFlow 是一个冷流构建器，旨在将基于回调的类型转换为基于流的类型
 *   callbackFlow 允许使用 send 函数从不同的 CoroutineContext 发出值，或者在 offer 函数的协程之外发出值。
 *   在内部，callbackFlow 使用一个通道，这在概念上非常类似于阻塞队列。通道配置了一个容量，即可以缓冲的最大元素数。在 callbackFlow 中创建的通道的默认容量为64个元素。
 *   当您尝试向完整通道添加新元素时，send 将挂起生成器，直到有空间容纳新元素为止，而 offer 不会将该元素添加到通道并立即返回 false。
 *   attention：在callbackFlow末尾必须使用awaitClose，否则报错java.lang.IllegalStateException: ‘awaitClose { yourCallbackOrListener.cancel() }’ should be used in the end of callbackFlow block.
 *              手动调用close()或者协程取消才会调用 awaitClose{}。否则就会一直处于运行状态不会结束。
 *15.channelFlow
 *   kotlin官网和android官网都没有描述
 *   [来自medium](https://medium.com/mobile-app-development-publication/kotlins-flow-channelflow-and-callbackflow-made-easy-5e82ce2e27c0)
 *   channelFlow就是一个带buffer的Flow，默认情况下它有64个缓冲区，也可以通过buffer设置缓冲区大小[具体参看这篇文章](https://medium.com/mobile-app-development-publication/kotlins-channel-flow-with-rendezvous-is-not-the-same-as-kotlin-flow-dfb5cff235d5)
 *   个人觉得 flow{}.buffer(64) 同channelFlow效果一样。  flow.buffer(0)和channelFlow{}.buffer(0)效果一样
 *   attention:发送数据用send 接受数据用 collect
 *16.StateFlow和SharedFlow
 *   [kotlin官方对StateFlow的API描述](https://kotlinlang.org/api/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines.flow/-state-flow/)
 *   [kotlin官方对SharedFLow的Api描述](https://kotlinlang.org/api/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines.flow/-shared-flow/)
 *   [Android官方文档对这两个类在Android上的应用介绍](https://developer.android.google.cn/kotlin/flow/stateflow-and-sharedflow)
 *   [来自掘金](https://juejin.cn/post/6937138168474894343)
 *   感觉看API描述就够了
 *   StateFlow 和 SharedFlow 是 Flow API，允许数据流向多个使用方发出值。就是支持一对多collect。StateFlow是SharedFlow的子类
 *   StateFlow是SharedFlow的特殊化版本，replay固定为1，缓冲区大小默认为0。貌似要代替Channel，因为在最佳实践的例子中已经找不到channel
 *   16.1 SharedFLow
 *        SharedFlow是一种热流，以广播方式在所有收集器之间共享发出值，以便所有collect都接收到值。与flow不同，flow是冷流，每次调用collect都会重新启动一遍(flow{重新执行一遍})
 *        SharedFlow永远不会完成也就是说 collect 一直会挂起所处协程, 在没有collect的时候 emit会一直发送，当所有数据都发送完，此时再调用collect将一个数据都收集不到
 *        只能通过MutableSharedFlow<>创建SharedFlow，通过emit发送数据所有collect收到数据前挂起协程，collect接收数据
 *        MutableSharedFlow(
 *        replay,                  共享流在其重播缓存中保存特定数量的最新值。每个新订阅者首先从重播缓存获取值，然后获取新的发出值。当前重播缓存的快照可以通过 replayCache 属性获得，
 *                                 并且可以使用 MutableSharedFlow.resetReplayCache 函数重置它。
 *        extraBufferCapacity,     缓冲池容量 = replay + extraBufferCapacity  onBufferOverflow策略是根据这个值触发的
 *        onBufferOverflow         当缓冲区满了时候的策略，默认 SUSPENDED 挂起emit，至少有一个collect的情况下才会触发onBufferOverflow策略在没有订阅方的情况下，
 *                                 没有collect情况下SharedFlow只存储最新值，onBufferOverflow策略永远不会触发。
 *                                 特别是在没有collect情况下，emit从不挂起会一直发送数据，此时就算设置了onBufferOverflow策略也不起作用
 *        )
 *        SharedFlow目的是为了替代BroadcastChannel，所有的操作都是线程安全的
 *        flowOn, buffer、RENDEZVOUS 、 cancellable 对SharedFlow没有作用
 *        attention:tryEmit会返回一个 Boolean值，true代表传递成功，false代表失败并挂起所在协程，直到有新的缓存空间。使用tryEmit缓冲池必须大于0(即replay + extraBufferCapacity>0).否则tryEmit发送数据，collect接收不到
 *   16.2 StateFlow
 *        StateFlow 是热数据，通过stateFLow.update{最新值}向其收集器发出最新值。StateFlow.value是只读属性所以一般使用子类MutableStateFlow。子类通过函数MutableStateFlow<T>(初始值)获取。必须赋初值，之后就不要value=新值，而是用update{新值}，后者看源码会发现有防抖效果
 *        SharedFlow永远不会完成也就是说 collect 一直会挂起所处协程，接收端只接收最新的值，例如 00:01 发送第一个值 00:02发送第二个值  collect在00:02调用，只能接收到第二个值(这点和SharedFlow不同，SharedFlow要想实现相同效果就要把replay设置为1)
 *        MutableStateFlow(参数功能同上)
 *        StateFlow是为了代替ConflatedBroadcastChannel
 *        flowOn, conflate, buffer ， CONFLATED ，RENDEZVOUS , distinctUntilChanged， cancellable对StateFlow没有作用
 *        StateFlow 和 LiveData 不同点
 *          StateFlow 需要将初始状态传递给构造函数，而 LiveData 不需要。
 *          当 View 进入 STOPPED 状态时，LiveData.observe() 会自动取消注册使用方，而从 StateFlow 或任何其他数据流收集数据的操作并不会自动停止。如需实现相同的行为，您需要从 Lifecycle.repeatOnLifecycle 块收集数据流。
 *   16.3 shareIn、stateIn将冷流变成热流
 *        作为MutableSharedFlow 和 MutableStateFlow的替代办法，shareIn、stateIn能将任意冷流转化成热流
 *        stateIn(
 *        scope: CoroutineScope, flow 生产者所在的协程作用域
 *        started: SharingStarted.Eagerly：急切模式，没有collect 流就已经启动,如果转成StateFLow，当调用collect能收到最新的值，如果转成SharedFlow，replay=0 当调用collect 收不到值，replay>0 能收到值
 *                               .Lazily：在第一个订阅者出现之后启动上游流，这保证第一个订阅者获得所有发出的值，而后续订阅者只保证获得最新的重播值。即使所有订阅者都消失了，上游流仍然处于活动状态，但是只有最近的重播值在没有订阅者的情况下被缓存。
 *                               .WhileSubscribed：只要存在collect流始终保持在启动状态，直到所有collect都被取消也就是说collect所处协程取消
 *        initialValue:初始值， 如果是 shareIn 这里就是replay
 *        )
 *--------------------------------------------------------------------------------------------------------------
 * * Sequences forEach(遍历一个值)，yield(产出)一个值，Sequences不执行完，遍历就会一直等待是同步过程。与它相比
 * 1. 利用序列在不阻塞主线程情况下，一个一个返回元素
 * 2. Sequence 无法使用delay
 * 3. 背压问题在生产者的生产速率高于消费者的处理速率的情况下出现
 * note:5. 在flow生成元素的逻辑代码中 修改上下文是不允许的,直接运行报错
 * note:1. flow在不指定协程(flowOn)的情况下,逻辑代码和终端操作处于同意协程中，逻辑代码就算写在 Default 协程中，也不顶用
 */
private val _events = MutableSharedFlow<String>()

@OptIn(ExperimentalStdlibApi::class)
fun main() {

    //1. Flow冷流
    println("-----coldFlow------")
    coldFlow()
    //2.取消流
    println("-----cancelFlow------")
    cancelFlow()
    //3. Flow的几种创建
    println("-----createFlows------")
    createFlows()
    //4. 中间操作
    println("-----middleFlow------")
    middleFlow()
    //5. 终端操作符
    println("-----terminalFlow------")
    terminalFlow()
    //6. flow上下文
    println("-----contextFlow------")
    contextFlow()
    //7. flow缓冲区
    println("-----7.buffer------")
    bufferFlow()
    //8. 合并
    println("-----8.conflate------")
    conflateFlow()
    //9. 组合
    println("-----9.1zip------")
    zipFlow()
    println("-----9.2combine------")
    combineFlow()
    //10.展开流，消除嵌套flow
    println("-----10.flatMapConcat------")
    flatteningFlows()
    //11. 捕获异常
    println("-----flowException------")
    flowException()
    //12. 流完成
    println("-----flowCompletion------")
    flowCompletion()
    //13. 启动流
    flowLaunchIn()
    //14.callbackFlow
//    runBlocking {
//        callbackFlow<SocketEvent> {//TODO 冷流
//            val socketListener = object : WebSocketListener() {
//                override fun onMessage(webSocket: Socket, text: String) {
////                    sendString()
////                    send(SocketEvent.StringMessage(text))
//                    trySend(SocketEvent.StringMessage(text))//发送不成功返回失败的值。 send成不成功都不返回
////                      offer()//可以在非生产端的协程内调用。 废弃API使用trySend代替,trySend还有这个特性吗？
////                    sendBlocking()//使用trySendBlocking代替
////                    trySendBlocking(SocketEvent.StringMessage(text))//看文档好像是在runBlocking中调用，用作测试用
//                }
//
//                override fun onOpen(webSocket: Socket, response: ResponseCache) {
//                    super.onOpen(webSocket, response)
//                }
//
//
//            }
//
//            // Add the listener object to our socket instance
//            attachWebSocketListener(socketListener)
////            close()
//            awaitClose {
////                yourCallbackOrListener.cancel()// 官网就是这样写的，要求释放 listener
//            }//手动调用close()或者协程取消才会调用 awaitClose{}
//        }.catch {
//            //捕获生产端的异常
//        }.collect {
//            println("callbackFlow----collect")
//        }
//
//    }
    //15.channelFlow
    println("-----15. channelFlow------")
    runBlocking {
        channelFlow {
            for (i in 1..5) {
                println("Emitting $i")
                send(i)
            }
        }
            .collect { value ->
                delay(100)
                println("Consuming $value")
            }
    }

    //16.1 SharedFlow
    runBlocking {
//        private val _events = MutableSharedFlow<String>() //隐藏可读写的SharedFlow, 这行代码写在main函数上方
        val events = _events.asSharedFlow() //公开只读SharedFlow, 没有emit方法
        launch {
            yield()
            repeat(5) {
                Util.log("emit----$it")
                _events.emit("$it") // 至少一个collect的情况下才会挂起直到所有collect都接收到数据
//                _events.tryEmit("$it").also(::println)//缓冲池必须大于0 (replay + extraBufferCapacity>0)否则发送数据collect接收不到
            }
//                _events.resetReplayCache()//清空重播参数
        }
        repeat(2) { num ->
            launch(Dispatchers.Default) {//在没有collect的情况下不会触发onBufferOverflow策略，也就是说emit不会挂起一直发射。所以这里要在其他线程上执行，从测试结果看 collect执行在 emit之前
                events.collect { value ->
                    delay(1000)
                    Util.log("#$num---value=$value")
                }
//                println("SharedFlow 的终端 永远会处于挂起状态--------")
            }
        }
    }
    //16.2 StateFlow
    runBlocking {
        val _selected = MutableStateFlow<Int>(1)
        val selected = _selected.asStateFlow()
        repeat(2) { index ->
            launch {
//                delay(1200)开启后由于只接收最新值 value =1 就接受不到了
                selected.collect {
                    Util.log("StateFlow #$index collect-----$it")
                }
                Util.log("StateFlow #$index end")
            }
        }//在子线程中得到值，因为StateFLow是热流值被主动推送给在主线程中的collect，多个collect同时接收到值，只接收最新的值

        launch(Dispatchers.Default) {
            delay(1000)
            _selected.update { 2 }
            delay(1000)
            _selected.update { 3 }
        }
    }
    //16.3 shareIn、stateIn将冷流变成热流
    runBlocking<Unit> {
        Util.log("开始")
        val f = async {
            flow<Int> {
                emit(1)
                Util.log("flow 所在协程")
            }.stateIn(this, SharingStarted.Eagerly, 0)
        }
        f.await().collect {
            Util.log("flow值---$it")
        }
    }

}


fun flowSimple(): Flow<Int> = flow { // flow builder
    for (i in 1..5) {
//        delay(100) // pretend we are doing something useful here
        Util.log("flow $i")
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

//3. Flow的几种创建
fun createFlows() {
    //3.1 元素创建Flow
    flowOf(1, 2)
    //3.2 代码块创建 Flow
    flow<Int> {
        emit(1)
        emitAll(flowOf(2, 3))
    }
    //3.3 列表转换
    listOf<Int>(1, 2).asFlow()
    arrayOf(1, 2).asFlow()
    (1..12).asFlow()
}

//1 Flow冷流
fun coldFlow() {//一个 Flow 创建出来之后，不消费则不生产，多次消费则多次生产，生产和消费总是相对应的。
    runBlocking {
        val flow = flowSimple1()
        flow.collect(::println)//Flow 可以被重复消费
        flow.collect(::println)
    }
}

// 2.flow的取消
fun cancelFlow() {// 所在协程或者作用域取消之后，flow都会取消
    runBlocking {
        try {
            //超时取消,作用域取消 flow也被取消
            withTimeout(100) {
                flowSimple().collect(::println)
            }
            //所在的协程取消后，flow也被取消了
            launch {
                flowSimple().collect { value ->
                    if (value == 3) cancel()
                    println(value)
                }
            }
        } catch (e: Exception) {
            println(e.message)
        }
    }
    println("Flow构建器发射前进行取消检查-------------------------")
    runBlocking {//取消检查
        launch {
            flow {
                (1..5).forEach {
                    println("cancelFlow----emit $it ")
                    emit(it)//构建器flow{} 在发射前增加了ensureActive(该状态取决于是否调用cancel())检查，所以如果已经取消则不再发射
                }
            }.collect { value ->//如果不用cancellable ，使用.onEach { currentCoroutineContext().ensureActive() 抛出异常}阻止flow发射
                if (value == 3) cancel()
                println(value)
            }
        }

    }
    println("出于性能原因扩展函数生成的流则不进行取消检查-------------------------")
    runBlocking<Unit> {//试验发现 不加
        launch {
            (1..5).asFlow()
//                .cancellable()//为了避免这种情况，一种方法是在收集前执行是 cancellable()，这是最简单的方法，如果不加的话协程取消后，flow会一直生产知道完结
                .onEach { currentCoroutineContext().ensureActive() }//为了避免这种情况您可以 另一种方法是 在收集前执行 .onEach { currentCoroutineContext().ensureActive() }，TODO 没看懂为什么返回false 就不发射了
                .collect { value ->
                    if (value == 3) cancel()//由于不检查仍然发射
                    println("onEach检查取消------$value")
                }
        }

    }
}

//4. 中间操作
fun middleFlow() {
    runBlocking {
        (4..7).asFlow()
            .map {
                println("flow map---$it")
                delay(1)//与集合的map 和 filter不同，flow的map 和 filter中可以使用挂起函数
                it.toString()
            }
            .transform {
                if (it.equals(1)) {
                    emit("value :$it*2")
                }
                emit("emit $it")
                emit(1)
                println("--------")
            }.collect(::println)

        (1..3).asFlow()
            .take(2).collect { println("tack----$it") }

    }

}

//5. 末端操作符
fun terminalFlow() {
    runBlocking {
        println((1..3).asFlow().reduce { a, b -> a + b })//累加函数
        println(flow { emit(1) }.first())//返回流发出的第一个元素，然后取消流的集合。如果流为空则引发 NoSuchElementException
        flow { emit(1) }.single()//适用于只有一个元素的流，返回流仅有的一个元素，如果是空流引发 NoSuchElementException，如果流能产生多个元素引发 IllegalStateException
        println(flowOf("a", "b", "c", "d").reduce { a, b -> a + b })//返回字符串 "abcd"
        println((1..3).asFlow().fold(1) { a, b -> a + b })//初始值(1)+1 =2 再加上2=4 再加上3 =7

//        //4.5 collect 挂起函数，收集消耗flow；launchIn非挂起函数，新建协程执行，功能同collect(不阻塞原有协程)
//        (1..3).asFlow().launchIn(this)//查看源码，发现收集操作在新建子协程中进行，注意launchIn还返回一个job，该job只用于取消collect子协程
    }
}

//6. flow的上下文
fun contextFlow() {
    runBlocking {
//        generateSequence(1) { it + 1 }.asFlow()//用无限流+flowOn就会发现，生产和终端是无序交替进行，因为他们运行在不同的线程、协程上面
        (12..15).asFlow()//运行在flowOn指定的上下文中(协程、线程)
            .cancellable()
            .map { Util.log("flowOn之前的map----$it") }//运行在flowOn指定的上下文中
            .flowOn(Dispatchers.Default)//设置之后发现生产端和终端处于不同的协程和线程中，TODO 怎么操作让他俩处在统一协程不同线程中呢???
            .map { Util.log("flowOn之后的map---$it") }//运行在终端操作所处的上下文中
            .collect { Util.log("收集") }//运行在终端操作所处的上下文中
    }
}

//7. flow缓冲区
fun bufferFlow() {
    runBlocking {
        generateSequence(1) { it + 1 }.take(10).asFlow().cancellable()
            .onEach {
                Util.log("生产----${it}")
            }
            .buffer()// 测试发现缓冲区能装载66个int类型，其它未测试
            .collect {
                Util.log("收集-----$it")
            }
    }
}

//8. 合并
fun conflateFlow() {
    runBlocking {
        (1..5).asFlow().onEach {
            println("emit $it")
        }.conflate().collect { value ->
            println("Collecting $value")
            delay(100)
            println("$value collected")
        }
    }
    println("-----------------")
    //8.2 发射端产生的每一个数据都会被收集端处理，但是 发射端新产生一个值，收集端会立即结束手头工作，去处理新的数据。
    runBlocking {
        (1..5).asFlow().onEach {
            println("emit $it")
        }.collectLatest { value ->
            println("Collecting $value")
            delay(100)
            println("$value collected")
        }
    }
}

//9.1组合zip
fun zipFlow() {
    runBlocking {
        val startTime = System.currentTimeMillis()
        val numb = (1..3).asFlow().onEach { delay(200) } // numbers 1..3
        val strs = flowOf("one", "two", "three") // strings
        numb.zip(strs) { a, b -> "$a -> $b" } // compose a single string
            .collect {
                Util.log("${System.currentTimeMillis() - startTime} $it")
            } // collect and print
    }
}

//9.2 combine 组合
fun combineFlow() {
    runBlocking {
        val nums = (1..3).asFlow().onEach { delay(300) } // numbers 1..3 every 300 ms
        val strs = flowOf("one", "two", "three").onEach { delay(400) } // strings every 400 ms
        val startTime = System.currentTimeMillis() // remember the start time
        nums.combine(strs) { a, b -> "$a -> $b" } // compose a single string with "combine"
            .collect { value -> // collect and print
                println("$value at ${System.currentTimeMillis() - startTime} ms from start")
            }
    }
}//与zip不同，第一次两个流都发射，收集端才会执行。之后任何一个flow发射，收集端都会执行，这时取另一个flow缓冲区最近的值

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


//10. 消除嵌套flow
fun flatteningFlows() {
    runBlocking {
        //10.1 flattenConcat 作用域是flow<flow<T>>
        flow {
            emit(flowOf(1, 2))
            emit(flowOf(3, 4))
        }.flattenConcat().collect { value ->
            print(value)
        }
        //10.2 flatMapConcat
        (1..3).asFlow().onEach { delay(100) } // a number every 100 ms
            .flatMapConcat { requestFlow(it) }
            .collect { value -> // collect and print
                println("flatMapConcat-----$value")
            }
        //10.3 flatMapMerge 有点复杂不建议使用
        val startTime1 = System.currentTimeMillis()
        flow {
            println("emit 1 ${System.currentTimeMillis() - startTime1}")
            emit(1)
            delay(100)
            println("emit 2 ${System.currentTimeMillis() - startTime1}")
            emit(2)
            delay(100)
            println("emit 3 ${System.currentTimeMillis() - startTime1}")
            emit(3)
        }.flatMapMerge {
            flow {
                emit("flatMapMerge1 :$it")
                delay(200)
                emit("flatMapMerge2 : $it")
                delay(1000)
            }
        }.collect { value ->
            println("$value  ${System.currentTimeMillis() - startTime1} ")
        }
    }
}

//11. 捕获异常
fun flowException() {
    //TODO 官网解释，在try catch 块中 使用flow{}构建器发射值，违反了异常透明性。想不明白这个异常透明性是什么东西
    runBlocking {
        flow {
            emit(1)
            throw ArithmeticException("Div 0")
        }.catch { // catch 只捕获上游异常. 例如 map写在catch下面就捕获不了map中的异常；例如 collect { ... } 块（位于 catch 之下）抛出一个异常，就捕获不了：
            println("caught error: $it")//可以将异常忽略，或用日志打印，或使用一些其他代码处理它
//            emit("Caught $it")// catch 能把异常当做值重新发射出去
//            throw ArithmeticException("Div 1")// 在catch中再抛出异常
        }.onCompletion {//在  flow 收集、取消、异常之后执行类似finally， onCompletion 和 catch的执行顺序看谁先被调用，
            //与catch不同，onCompletion既能接收到发射异常也能接收到收集异常， onCompletion有个可空参数Throwable 用来确定收集(或发射)到底是异常还正常完成
            println("finally.")
        }.collect(::println)
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

//12. flow 完成时
fun flowCompletion() {
    //当flow执行终端操作之后，可能需要执行一个操作，可以通过两种方式 try finally 和 onCompletion操作符
    runBlocking {
        //12.1 命令式 finally 块
        try {
            (6..8).asFlow().collect { println("$it") }
        } finally {
            println("flow------finally")
        }
        //12.2声明式onCompletion,该运算符在终端操作结束时调用. 使用这个操作符的优点是 onCompletion有个可空参数Throwable 用来确定收集(或发射)到底是异常还正常完成
        (9..12).asFlow()
            .onCompletion { cause -> println("flow-----onCompletion------${cause?.message}") }
            .collect { println("$it") }
    }
}

//13.launchIn 操作符简化在新建协程中启动流的终端操作
fun flowLaunchIn() {
    runBlocking<Unit> {
        (1..3).asFlow().onEach { Util.log("Event 1: $it") }.launchIn(this)
        Util.log("Done")
    }
}


//并发消除嵌套flow
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

//并发消除嵌套+collect收集端只处理最新值
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

// StateFlow
fun stateFlow() {
    runBlocking {
        val state = MutableStateFlow(1)//

    }
}

fun requestFlow(i: Int): Flow<String> = flow {
    emit("$i: First")
    delay(500) // wait 500 ms
    emit("$i: Second")
}

fun attachWebSocketListener(socket: WebSocketListener) {
//    // Instansiate client
//    val client = OkHttpClient()
//
//    // Build a reuqest
//    val request = Request.Builder().url(<SOCKET_URL).build()
//
//    // Set the listener to the socket
//    socket = client.newWebSocket(request, webListener)
}
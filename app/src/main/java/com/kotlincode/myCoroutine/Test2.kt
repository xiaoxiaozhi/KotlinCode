package com.kotlincode.myCoroutine

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.*
import java.lang.System.currentTimeMillis

fun events(): Flow<Int> = (1..3).asFlow().onEach {
    delay(100)
    Util.log("Event 1: $it")
}

//private val _events = MutableSharedFlow<String>()
fun main() {
//    runBlocking<Unit> {
//        Util.log("开始")
//        val f = async {
//            flow<Int> {
//                emit(1)
//                Util.log("flow 所在协程")
//            }.shareIn(this, SharingStarted.Eagerly, 0)
//        }
//        delay(1000)
//        f.await().collect {
//            Util.log("flow值---$it")
//        }
//    }
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
}
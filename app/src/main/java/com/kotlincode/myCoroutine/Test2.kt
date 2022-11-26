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
    runBlocking<Unit> {
        Util.log("开始")
        val f = async {
            flow<Int> {
                emit(1)
                Util.log("flow 所在协程")
            }.shareIn(this, SharingStarted.Eagerly, 0)
        }
        delay(1000)
        f.await().collect {
            Util.log("flow值---$it")
        }
    }
}
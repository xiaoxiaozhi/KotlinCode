package com.kotlincode.myCoroutine

import androidx.lifecycle.Lifecycle
import kotlinx.coroutines.*

/**
 * Flow Sequnce 对比
 * 1.
 */
fun main() {
//    runBlocking {
//        runBlocking {
//            Util.log("runBlocking----")
//            Util.log("CoroutineScope1----${this.hashCode()}")
////            val job = launch {
//            val result = try {
//                val job1 = withTimeout(100) {//引发超时取消协程会报 TimeoutCancellationException
//                    Util.log("withTimeoutOrNull----")
//                    Util.log("CoroutineScope2----${this.hashCode()}")
//                    launch {
//                        repeat(20) {
//                            repeat(Int.MAX_VALUE) {}
//                            Util.log("i = $it")
//                            repeat(Int.MAX_VALUE) {}
//                            repeat(Int.MAX_VALUE) {}
//                            Util.log("job----${this.isActive}")
//                        }
//                    }
////                    job.join()
////                    Util.log("job----${job.isActive}")
//                }
//                Util.log("job----${job1.isActive}")
//            } catch (e: Exception) {
//                Util.log("Exception------${e.message}")
//            }
//
//            Util.log("isActive1------${isActive}")
////            }
////            job.join()
////            Util.log("isActive2------${job?.isActive}")
////            Util.log("result is $result")// 超时前协程正常执行则返回协程返回的结果，否则返回null
//        }
//    }

    runBlocking {
        launch {
            withTimeout(100) {
                try {
                    repeat(20) {
                        repeat(Int.MAX_VALUE) {}
                        Util.log("i = $it")
                        repeat(Int.MAX_VALUE) {}
                        repeat(Int.MAX_VALUE) {}
                        Util.log("job----${this.isActive} com ${this.coroutineContext[Job]?.isCancelled}")
                    }
                } catch (e: Exception) {
                    Util.log("Exception------${e.message}")
                }

            }
        }
    }
}
package com.kotlincode.myCoroutine

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.kotlincode.R
import kotlinx.coroutines.*

/**
 * 协程在Activity中应用
 * 1. MainScope 专注于图形界面
 * 2. 通过委托的方式，让Activity实现携程作用域，销毁的时候，取消协程
 */
private fun log(message: String) = println("[${Thread.currentThread().name}] $message")
class CoroutineActivity : AppCompatActivity(),
    CoroutineScope by CoroutineScope(Dispatchers.Main) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_coroutine)
        coroutineThreadLocal()
    }

    override fun onDestroy() {
        super.onDestroy()
        cancel()
    }

    fun coroutineThreadLocal() {
        val threadLocal = ThreadLocal<String>()
        runBlocking {
            threadLocal.set("hello")
            log("threadLocal1 value is ${threadLocal.get()}")
            launch(Dispatchers.Default + threadLocal.asContextElement("word")) {
                log("threadLocal2 value is ${threadLocal.get()}")
                withContext(Dispatchers.Main) {
                    log("threadLocal3 value is ${threadLocal.get()}")
                }
            }//threadLocal.asContextElement(value = "word") 还可以在线程切换前提前赋值
        }
    }
}
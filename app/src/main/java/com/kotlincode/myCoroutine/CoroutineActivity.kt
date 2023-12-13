package com.kotlincode.myCoroutine

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.kotlincode.R
import kotlin.String
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.take

/**
 * [一篇翻译自谷歌大佬的文章](https://blog.csdn.net/cpcpcp123/article/details/112425954)
 * [这是原文](https://medium.com/androiddevelopers/exceptions-in-coroutines-ce8da1ec060c)
 *
 * [按照这篇文章的总结](https://blog.csdn.net/cpcpcp123/article/details/112425954)
 * 协程处理异常
 * 1. 异常传播
 *     子协程未捕获到的异常将不会被重新抛出，而是一级一级向父作用域传递，这种异常传播将导致父父作用域失败，进而导致其子作用域的所有请求被取消。
 *     launch 在协程代码块中用try catch 才能捕获异常，注意在协程构建器外面try catch不能捕获异常
 *     async在结果 Deferred 对象中捕获所有异常 try{deferred.await()}catch{}因此给他用 CoroutineExceptionHandler没有效果。
 *     当async作为根协程时，被封装到deferred对象中的异常才会在调用await时抛出。用GlobalScope演示
 *     如果async作为一个子协程时，异常捕获同launch
 * 2. CoroutineExceptionHandler
 *    如果 try-catch 不在协程代码块中，那么它不会重新抛出异常，而是传播到顶级协程/父协程的工作，导致应用程序崩溃。
 *    CoroutineExceptionHandler 上下文元素，未捕获异常的协程设置后，可在这里捕获。是由于协程结构化并发的特性的存在，子作用域的异常经过一级一级的传递，最后由CoroutineExceptionHandler进行处理
 *    也就是说在CoroutineExceptionHandler被调用时，协程已经被取消
 *    只适用于launch构建的协程， 在作用域或根协程中设置上下文元素 才起作用
 * 3. SupervisorJob和supervisorScope
 *    阻止异常向上传播
 *    使用SupervisorJob ，子协程的失败不会影响到其他子协程。SupervisorJob 不会取消自身或它的其他子协程，而且SupervisorJob 不会传播异常而是让它的协程处理。
 *    与.kt稍有不同在Android中，无论我们使用何种类型的Job，未捕获的异常最终都会被抛出使App崩溃。常用做法是在根协程加入CoroutineExceptionHandler捕获异常，需要注意的是，如果没有捕获异常，实验结果将于描述不符
 *    SupervisorJob只有在以下两种作用域中才会起作用：使用supervisorScope{...}或CoroutineScope(SupervisorJob())创建的作用域
 * 4. MainScope
 *    MainScope创建一个作用域，设置了Dispatch.Main 和 SupervisorJob 两个上下文元素，mainScope.launch{}创建协程，
 *    在Activity销毁的时候在onDestroy中 调用mainScope.cancel() 销毁作用域内所有创建的子协程避免内存泄漏。 通过这种方式管理整个Activity的协程。
 * note：直接用 CoroutineScope(SupervisorJob()).launch{} 在.kt中开启不了协程，实际在Activity中就可以
 * note: 为什么在 runBlocking中{} 必须用 GlobalScope开启协程才能实现 ，官方展示效果。用launch{}或者 await()没用
 */
private fun log(message: String) = println("[${Thread.currentThread().name}] $message")
class CoroutineActivity : AppCompatActivity() {
    private val mainScope = MainScope()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_coroutine)
        val s = CoroutineScope(Job())
        CoroutineScope(Job()).launch {
            flow {
                try {
                    emit(1)
                    emit(2)
                    println("This line will not execute")
                    emit(3)
                }catch(e:Exception){
                    println("Exception----$e")
                }
                finally {
                    println("Finally in numbers")
                }
            }
                .take(2).collect { println("tack----$it") }
        }

    }

    override fun onResume() {
        super.onResume()
        //1. 异常传播
        val scope1 = CoroutineScope(Job())
        scope1.launch {
            try {
                throw IndexOutOfBoundsException("IndexOutOfBoundsException")
            } catch (e: Exception) {
                Util.log("scope1.launch -----${e.message}")
            }
        }
        //1.1 async捕获异常
        scope1.launch {
            val deferred = GlobalScope.async {//当async作为根协程时，被封装到deferred对象中的异常才会在调用await时抛出
                throw IndexOutOfBoundsException("IndexOutOfBoundsException")
            }
            try {
                deferred.await()
            } catch (e: Exception) {
                Util.log("scope1.async -----${e.message}")
            }

            //如果async作为一个子协程时，异常捕获同launch
            async {
                try {
                    throw IndexOutOfBoundsException("IndexOutOfBoundsException")
                } catch (e: Exception) {
                    Util.log("scope2.async -----${e.message}")
                }
            }
        }

        val handler = CoroutineExceptionHandler { _, exception ->
            Util.log("CoroutineExceptionHandler got $exception with suppressed ${exception.suppressed.contentToString()}")
        }
        //2. CoroutineExceptionHandler
        val scope2 = CoroutineScope(Job())
        scope2.launch(handler) {// 该例在根协程中设置或者您也可以在开启协程的作用域中设置 例如CoroutineScope(Job()+handler)
            async {
                throw Exception("Failed coroutine")
            }.await()
            //----------------------------
//            async { throw Exception("Failed coroutine") }
            //----------------------------
//            launch { throw Exception("Failed coroutine") }
            //以上这几种都能在CoroutineExceptionHandler中被捕获
        }

        //3. SupervisorJob
        supervisor()
        //3.1 监督作用域  的使用
        supervisor1()
        //4. MainScope
        MainScope().launch {
            Util.log("MainScope")
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        mainScope.cancel()//TODO 不是说绑定了Activity生命周期吗？怎么还要取消
    }


    fun doSomething() {
        repeat(10) { i ->
            mainScope.launch {
                delay((i + 1) * 200L) // variable delay 200ms, 400ms, ... etc
                Util.log("Coroutine $i is done")
            }
        }
    }

    //3. SupervisorJob
    private fun supervisor() {
        val coroutineEH = CoroutineExceptionHandler { _, exception ->
            Util.log("CoroutineExceptionHandler got $exception with suppressed ${exception.suppressed.contentToString()}")
        }
        with(CoroutineScope(Job() + coroutineEH)) {//增加了异常捕获，实验结果和上文描述一致；如果没有异常捕获使用SupervisorJob还是Job 两个子协程都会执行
            launch {
                Util.log("SupervisorJob.launch1")
                throw IndexOutOfBoundsException()
            }
            launch {
                Util.log("SupervisorJob.launch2")
            }
        }

    }

    //3.1 supervisorScope
    private fun supervisor1() {
        val coroutineEH = CoroutineExceptionHandler { _, exception ->
            Util.log("supervisor1 CoroutineExceptionHandler got $exception with suppressed ${exception.suppressed.contentToString()}")
        }
        with(CoroutineScope(Job() + coroutineEH)) {
            launch {
                supervisorScope() {
                    launch {
                        Util.log("supervisorScope.launch1")
                        throw IndexOutOfBoundsException()
                    }
                    launch {
                        Util.log("supervisorScope.launch2")
                    }
                }
                Util.log("scope.launch")
            }
        }
    }
}
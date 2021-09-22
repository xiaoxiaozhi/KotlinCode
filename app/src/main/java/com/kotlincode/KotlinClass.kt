package com.kotlincode

import android.net.IpSecManager

/**
 * 面向对象
 */
fun main() {
    println("-----对象表达式-----")
    //1. 对象表达式
    drawCircle()
    println(Unit.numberOfProcess())
}

//1. 对象表达式创建匿名内部类
fun drawCircle() {
    val circle = object {
        val x = 10
        val y = 10
        val radius = 30
    }//对象表达式由 object关键字后跟{}组成，最基本的对象表达式只对将几个局部变量组合在一起有用
    println("circle x = ${circle.x} , circle y = ${circle.y} , circle radius = ${circle.radius}")
}

//1.1 对象表达式实现单一接口，创建匿名内部类
fun createRunnable(): Runnable {
    val runnable = object : Runnable {
        override fun run() {
            println("call Runnable")
        }
    }//object : 接口,接口{}
    return runnable
}

//1.2 单一抽象方法接口。 接口名{}
fun createRunnable1() = Runnable { println("单一") } //如果匿名内部类实现单一抽象方法的接口，可以直接实现不用再指定变量（接口名{}）

//1.3 对象表达式创建继承多个接口的匿名内部类
fun createRunnable2(): Runnable {
    return object : Runnable, AutoCloseable {
        override fun run() {
        }

        override fun close() {
        }
    }
}

//1.4 单例模式，对象表达式 创建单例 ，Unit 并不是 类二是一个实例，不能通过Unit创建其它实例
object Unit {
    const val radiusInKM = 59000
    fun numberOfProcess() = Runtime.getRuntime().availableProcessors()
}//在 object 名称 {} kotlin认为这是语句而不是表达式 ，使用对象表达式创建匿名内部类的实例

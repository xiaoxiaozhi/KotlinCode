package com.kotlincode.oop

/**
 * 面向对象
 * 1.对象表达式
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

//1.4 单例模式，对象表达式 创建单例 ，Unit 并不是 类而是一个实例，不能通过Unit创建其它实例
object Unit {
    const val radiusInKM = 59000 //编译时常量，编译器将 const val 属性的值内联到使用它们的位置。[描述](https://kotlinlang.org/docs/whatsnew11.html#constant-inlining)
    fun numberOfProcess() = Runtime.getRuntime().availableProcessors()
}//在 object 名称 {} kotlin认为这是语句而不是表达式 ，使用对象表达式创建匿名内部类的实例

//1.5  顶级函数和单例，参见Test.kt
// 如果一组函数是高级的、通用的和广泛使用的，那么就把它们直接放在一个包中
// 如果一组函数需要依赖于状态，你可以将此状态与那些相关的函数一起放在一个单例中，
// 当我们关注行为、计算和动作时，函数和单例是很有意义的。但是如果我们想处理状态，那么类是一个更好的选择
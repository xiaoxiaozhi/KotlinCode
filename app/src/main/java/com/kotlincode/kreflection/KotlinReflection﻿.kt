package com.kotlincode.kreflection

/**
 * 反射  TODO Kotlin的反射怎么跟Java不一样？？？
 * 1. 类引用
 *    最基本的最基本的反射功能是获取对 Kotlin 类的运行时引用 val c = MyClass::class
 * 2. 可调度的引用
 *    2.1 函数引用 看下面
 *    2.2 属性引用 看下面
 *    2.3 构造函数引用 看下面
 *    2.4 绑定属性和函数引用 。。。。
 *    2.5 绑定构造函数引用
 */
val x = "123`"
fun main() {
    val widget: String = "123"

    widget::class.constructors
    widget::class.java.simpleName
    { "Bad widget: ${widget::class.qualifiedName}" }
    println("${widget}")// ::代表对象

    //2.1 函数引用
    val numbers = listOf(1, 2, 3)
    println(numbers.filter(::isOdd))
    //2.2 属性引用
    println("${::x}")
    //2.3 零参构造函数
    println(::Foo)
    //2.4 引用对象的方法‘
    val numberRegex = "\\d+".toRegex()
    println(numberRegex.matches("29"))
    val isNumber = numberRegex::matches// 需要绑定到其它变量，调用
    println(isNumber("29"))
    //2.5 绑定构造函数引用
    val o = Outer()
    val boundInnerCtor = o::Inner


}

fun isOdd(x: Int) = x % 2 != 0
class Foo
class Outer {
    inner class Inner
}
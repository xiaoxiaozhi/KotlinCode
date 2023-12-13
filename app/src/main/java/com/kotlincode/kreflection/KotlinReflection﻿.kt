package com.kotlincode.kreflection

import kotlin.String

/**
 * 反射  TODO Kotlin的反射怎么跟Java不一样？？？
 * 1. 类引用
 *    最基本的最基本的反射功能是获取对 Kotlin 类的运行时引用 val c = MyClass::class
 * 2. 可调度的引用
 *    个人认为函数引用本质是简化了的lambda表达式， ::函数名/属性=lambda {action:Int->函数(action)} 转变为(::函数)
 *    2.1 函数引用 看下面，T.()->Unit 和(T)->Unit 需要的函数一样即 函数(T)
 *    2.2 属性引用 看下面
 *    2.3 构造函数引用 看下面
 *    2.4 绑定属性和函数引用 。。。。
 *    2.5 绑定构造函数引用
 * [::class vs ::class.java](https://stackoverflow.com/questions/59781916/what-is-the-difference-between-class-and-class-java-in-kotlin)
 * 通过使用::class，您可以获得 KClass 的一个实例。它是 Kotlin 反射 API，可以处理像属性、数据类等 Kotlin 特性。
 * 通过使用::Class.java，您可以获得 Class 的一个实例。它是 Java 反射 API，可以与任何 Java 反射代码交互，但是不能与某些 Kotlin 特性一起工作。
 */
val x = "123`"
fun main() {
    val widget: String = "123"

    widget::class.constructors
    widget::class.java.simpleName
    { "Bad widget: ${widget::class.qualifiedName}" }
    println("${widget}")// ::前面省略了对象，即当前引用该属性或方法的实例

    //2.1 函数引用
    val numbers = listOf(1, 2, 3)
    println(numbers.filter(::isOdd))
    numbers.apply (::other)
    //2.2 属性引用
    println("${::x}")
    //2.3 零参构造函数
    println(::Foo)// ()->Foo 返回一个Foo对象
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
fun other(l:List<Int>){

}
class Foo
class Outer {
    inner class Inner
}
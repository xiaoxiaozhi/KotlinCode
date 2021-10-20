package com.kotlincode.efficient

import java.net.URL
import kotlin.math.pow

/**
 * 扩展类
 * 1. 注入方法
 * - 从外部将方法添加到这些类中
 * - 当扩展函数和同名的实例方法之间发生冲突时，如果导入了扩展函数的包，扩展函数总是获胜。
 * - 扩展函数不属于  类.函数名  前面的类，它是顶级函数，属于package的类和方法都可以调用
 * 2. 注入属性
 * - 扩展属性不是类内部的一部分，所以它不能使用幕后字段
 * 3. 注入第三方类
 * 4. 注入静态方法
 * 5. 类内部注入
 * - 类外部对Pair<Int, Int>的实例使用该扩展函数，编译器将报错
 *
 */
fun main() {
    //1. 扩展函数：方法注入自定义类
    println(Circle().contains(Point(1, 1)))
    //2. 扩展属性
    println("circle are ${Circle().are}")
    //    Circle().are =  // 没有幕后字段,无法给该属性赋值
    //3. 注入第三方类
    println("".reversed())
    //4. 注入伴生对象
    println(String.toUrl("www.baidu.com"))
    //5. 从类内部注入


}

//1. 自定义类扩展函数
fun Circle.contains(point: Point) =
    (point.x - cx).toDouble().pow(2) + (point.y - yx).toDouble().pow(2) < radius.toDouble().pow(2)

//2. 扩展属性
val Circle.are: Double
    get() = 2 * Math.PI * radius

data class Point(val x: Int = 2, val y: Int = 2)
data class Circle(val cx: Int = 1, val yx: Int = 1, val radius: Int = 1)

//3. 对第三方类注入扩展函数
fun String.isPalindrome() = reversed() == this//判断文字是否是回文
fun String.reversed() = "123"

//4. 对伴生对象注入扩展函数
fun String.Companion.toUrl(link: String) = URL(link)

//5. 从类内部注入
class Point1(private val x: Int = 2, private val y: Int = 2) {
    private val pair = Pair(x, y)
    private val firstSign = if (pair.first < 0) "" else "+"
    private val secondSign = if (pair.second < 0) "" else "+"
    fun Pair<Int, Int>.point2String() =
        "${firstSign}$first ${this@Point1.secondSign}${this.second}"//5.1 类外部对Pair<Int, Int>的实例使用该扩展函数
    //5.2 扩展函数中提到的属性或方法指向扩展接收者(Pair)。如果不在扩展者身上它将到分发接收者(Point1)上寻找。扩展接收者优先绑定到属性和方法

    override fun toString(): String = pair.toString()
}


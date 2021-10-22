package com.kotlincode.efficient

import kotlin.math.pow

/**
 * 高效利用kotlin
 * 重载运算符
 * 1. 不要在+或–运算符重载函数中改变对象
 * 中缀表示法
 * 1. 在操作数中间插入或植入操作符
 * 2. 中缀方法只能接受一个参数——没有vararg（可变参数），也没有默认参数
 * 3.
 *
 */
fun main() {
    //1. 重载运算符
    println((1 to 2) + (3 to 4))
    //1.1 复合运算符
    var p = (1 to 2)
    p += (3 to 4)//
    println(p)//对于复合运算符，该例实现了plus就可以对+=做出反应，不需要再实现plusAssign()。如果既没有实现+也没有实现+= 将报错
    //2.
    Circle().contains1(Point())
    Circle() contains1 Point()
}

//1. 重载运算符  operator fun 类.运算符专有方法名
operator fun Pair<Int, Int>.plus(other: Pair<Int, Int>) =
    Pair(first + other.first, second + other.second)//plus()，是+的专用方法名


infix fun Circle.contains1(point: Point) =
    (point.x - cx).toDouble().pow(2) + (point.y - yx).toDouble().pow(2) < radius.toDouble().pow(2)
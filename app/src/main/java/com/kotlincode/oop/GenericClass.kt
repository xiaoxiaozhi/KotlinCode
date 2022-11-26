package com.kotlincode.oop

import java.io.Closeable

/**
 * 泛型类
 * 1. kotlin泛型类
 *    类名后面加通配符<T>组成一个泛型类 Box<T> ，<T>表示构造参数、方法参数和类属性的类型，实例化这样的类Box<Int>(1) 如果能推断出来则可以省略泛型 Box(1)
 *    通配符用<大写字母>表示，一般有<T> <E> <V>.....
 */
fun main() {
    //1.带泛型的的类
    Box<Int>(1)//或
    Box(1)// 如果参数能够推断出来类型则可以不加 泛型

    println(PriorityPair(1, 2))
    println(PriorityPair("A", "B"))
}

class Box<T>(t: T) {
    var value = t
}

//1. 单类型约束 类名<泛型标识:类>
class PriorityPair<T : Comparable<T>>(member1: T, member2: T) {
    val first: T
    val second: T

    init {
        if (member1 >= member2) {
            first = member1
            second = member2
        } else {
            first = member2
            second = member1
        }
    }

    override fun toString(): String {
        return "($first,$second)"
    }
}

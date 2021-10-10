package com.kotlincode.oop

import java.io.Closeable

/**
 * 泛型类
 */
fun main() {
    println(PriorityPair(1, 2))
    println(PriorityPair("A", "B"))
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

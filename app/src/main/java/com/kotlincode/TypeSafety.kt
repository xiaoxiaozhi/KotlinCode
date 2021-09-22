package com.kotlincode

import java.lang.RuntimeException
import kotlin.math.sqrt

/**
 * 类型安全
 */
fun main() {
    println("-----Any-----")
    //1. any  kotlin中所有的类都继承自Any，目的是为了提供一些通用的方法 equals、hashCode、toString、to
    println("-----Nothing-----")
//    println(computeSqrt(-1).javaClass)
    println("-----null-----")
//    nickName(null)//null 作为参数编辑器不ton过
    println(nickName1("Bill"))
    println(nickName2(null))
    println(nickName3(null))
}

//2. Nothing 类 没有实例表示一个永远不存在的值或者结果，当用作方法返回时表示函数永远不会返回
fun computeSqrt(n: Int): Double {
    if (n > 0) {
        return sqrt(n.toDouble())
    } else {
        throw RuntimeException("No negative please")//实际打印并未看见Nothing类，存疑
    }
}

//3. null kotlin 不允许null 作为方法参数 也不允许作为返回
fun nickName(name: String): String {
    if (name == "William") {
        return "Bill"
    } else {
//        return null     //作为方法返回，编辑器不通过‘
    }
    return ""
}

// 3.1 使用可空类型 返回和接收 null
fun nickName1(name: String?): String? {
    return if (name == "William") {//每一个非空类型 都有对应的可空类型（非空？），可空类型能够引用非空和null
        "Bill"
    } else {
        null     //作为方法返回，编辑器不通过
    }
}

//3.2 安全调用运算符
fun nickName2(name: String?): String? {
    return if (name == "William") {//每一个非空类型 都有对应的可空类型（非空？），可空类型能够引用非空和null
        "Bill"
    } else {
        name?.reversed()?.toUpperCase()   //对于可空类型的方法调用，使用安全调用运算符 (?.)
    }
}

//3.3 Elvis 运算符
fun nickName3(name: String?): String? {
    return if (name == "William") {//每一个非空类型 都有对应的可空类型（非空？），可空类型能够引用非空和null
        "Bill"
    } else {
        name?.reversed()?.toUpperCase() ?: "Joker"   //对于可空类型的方法调用，使用安全调用运算符 (?.)
    }
}


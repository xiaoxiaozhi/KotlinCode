package com.kotlincode.typesafe

/**
 * 泛型
 */
fun main() {
    //1. 类型不变性(Array<T>和List<out E>)
    val bananas: Array<Banana> = arrayOf<Banana>()
    arrayOf<Fruit>() + Orange()
//    receiveFruit(bananas)// 同java一样不允许传递，泛型类型不变性决定，理由：一筐子香蕉不是从一筐子水果继承来的
    val orange: List<Orange> = listOf();
    receiveFruit(orange)//限制了数组，但没有限制列表,原因是数组接口和列表接口后者有向下兼容List<out E>
}

open class Fruit
class Banana : Fruit()
class Orange : Fruit()

fun receiveFruit(fruit: Array<Fruit>) {
    println("Num of fruit:${fruit.size}")
}

fun receiveFruit(fruit: List<Fruit>) {
    println("Num of fruit:${fruit.size}")
}


package com.kotlincode.oop

import java.lang.RuntimeException

/**
 * sealed 密封类
 * 1. Kotlin的sealed类对于同一个文件中定义的其他类进行扩展是开放的，但是对于其他的类——也就是final或者不是open的类，则是关闭的。
 * 2. sealed类的构造函数默认是private, 只能被本文件中定义的类继承
 */
fun main() {
    println(Ace("diamond"))
    println(Queen("club"))

}

sealed class Card(val suit: String)
class Ace(suit: String) : Card(suit)
class King(suit: String) : Card(suit) {
    override fun toString(): String {
        return "King of $suit"
    }
}

class Queen(suit: String) : Card(suit) {
    override fun toString(): String {
        return "Queen of $suit"
    }
}

class Jack(suit: String) : Card(suit) {
    override fun toString(): String {
        return "Jack of $suit"
    }
}

class Pip(suit: String, val number: Int) : Card(suit) {
    init {
        if (number < 2 || number > 10) {
            throw RuntimeException("pip has to be between 2 and 10")
        }
    }
}

fun process(card: Card) = when (card) {
    is Ace -> "${card.javaClass.name} of ${card.suit}"
    is King, is Queen, is Jack -> "$card"
    is Pip -> "${card.number}"
}
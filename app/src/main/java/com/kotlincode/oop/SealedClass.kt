package com.kotlincode.oop

import kotlin.String
import java.lang.RuntimeException

/**
 * [密封类 vs 枚举、抽象](https://hi-dhl.blog.csdn.net/article/details/108173544)
 * 枚举类：限制枚举每个类型只允许有一个实例
 *       限制所有枚举类型使用相同的类型的值 enum class Color {Red(1)}. 密封类每个类型有自己的变量
 * 结论：google项目中都是用密封类代替之前的枚举类。只有在涉及反序列化、只用作单例不需要额外变量(没有构造函数)使用枚举,其它情况用密封类
 * --------------------------------------------------------------------------------
 * sealed 密封类
 * 1. Kotlin的sealed类对于同一个文件中定义的其他类进行扩展是开放的，但是对于其他的类——也就是final或者不是open的类，则是关闭的。
 * 2. sealed类的构造函数默认是private, 只能被本文件中定义的类继承
 * 这样做目的是确保第三方库无法扩展你定义的 sealed class，达到限制类的扩展目的
 */
fun main() {
    println(Ace("diamond"))
    println(Queen("club"))

}
sealed class MyColor {
    class Yellow : MyColor()

    class Red : MyColor()

    class Black : MyColor()
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
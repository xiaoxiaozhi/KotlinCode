package com.kotlincode.oop

/**
 * 枚举
 */
fun main() {
    println(Ace1(Suit.CLUBS))
    //2. 通过字符串获取枚举实例
    val diamond = Suit.valueOf("DIAMONDS")
    println(diamond)
    //3. 遍历枚举
    for (value in Suit.values()) println(value.display())
}

//1. 枚举
enum class Suit(val symbol: Char) {
    CLUBS('\u2663'), DIAMONDS('\u2666'), HEARTS('\u2665') {
        override fun display(): String {
            return "${super.display()} $symbol"
        }
    },

    //1.1 最后一个实例后面要加分号;
    SPADES('\u2660');

    open fun display() = "$symbol$name"

}

sealed class Card1(val suit: Suit)
class Ace1(suit: Suit) : Card1(suit) {
    override fun toString(): String = "ace  0f $suit "
}

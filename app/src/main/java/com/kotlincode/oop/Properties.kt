package com.kotlincode.oop

/**
 * TODO  [待总结](https://kotlinlang.org/docs/properties.html#getters-and-setters)
 * 1.声明属性
 * 2.Getters和Setters
 * 3.编译时常量
 *
 *
 */
const val c = "WWW"
fun main() {

    //3.编译时常量
    println("$c")//编译时常量，编译器将 const val 属性的值内联到使用它们的位置

}
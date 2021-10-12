package com.kotlincode.oop

/**
 * 抽象类
 * 1. 类必须标记为abstract才能被视为抽象
 * 2. 抽象类中的方法必须标记为abstract。
 * 抽象类和接口之间的主要区别是：
 * 1. 在接口中定义的属性没有幕后字段，它们必须依赖抽象方法来从实现类中得到属性。另外，抽象类中的属性可以使用幕后字段。
 * 2. 你可以实现多个接口，但最多可以从一个类（抽象的或非抽象的）扩展
 */
fun main() {
    val obj = object : Musician("jack", "shandong") {
        override fun instrumentType(): String {
            return "$name,$actionFrom"
        }
    }
    obj.implementation()
}

//1. 抽象类
abstract class Musician(val name: String, val actionFrom: String) {
    val info: String = name + actionFrom//1.1 抽象类允许有属性
    abstract fun instrumentType(): String //1.2 抽象类拥有抽象方法
    fun implementation(): String {//1.3 抽象类允许有方法实现
        return ""
    }

}
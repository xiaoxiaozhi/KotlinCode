package com.kotlincode.oop

import java.lang.RuntimeException

/**
 * 继承
 * 1. kotlin 中的类默认都是final不可继承
 * 2. 重写方法可以标记为final override，防止子类进一步重写该方法。
 * 3. 基类中的val属性可以在派生类中用val、var重写；基类中的var就只能用派生类中的var重写
 */
fun main() {

}

open class Vehicle(val year: Int, open var color: String) {
    open val km = 0
    final override fun toString(): String {
        return "Vehicle(year=$year, color='$color', km=$km)"
    }

    fun repaint(newColor: String) {
        color = newColor
    }
}

open class Car(year: Int, color: String) : Vehicle(year, color) {
    override var km: Int = 0 //基类中的var就只能用派生类中的var重写
        set(value) {
            if (value < 1) {
                throw RuntimeException("cant set navigate value")
            }
            field = value
        }

    fun drive(distance: Int) {
        km += distance
    }
}


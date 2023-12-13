package com.kotlincode.oop

import kotlin.String

/**
 * 面向对象
 * 1. 伴生对象
 */
fun main() {
    //1.通过类名调用伴生对象属性和方法
    MachineOperator.checkedIn
    MachineOperator.minimumBreak()
    //2. 伴生对象的引用
    MachineOperator.Companion //当伴生对象实现接口，又需要把伴生对象传递给需要的函数时
    MachineOperator1.MachineOperatorFactory//用伴生对象显式名称引用伴生对象
    //3. 伴生对象作为工厂类，创建所属类的实例
    println(MachineOperator2.create("TV").checkIn())
    //4. 伴生类成员和方法不是静态
    //当你引用一个伴生对象的成员时，Kotlin编译器会负责将调用路由到适当的单例实例

}

//1. 伴生对象使类拥有属性和方法，它不属于任何实例，所以实例也就无法调用
class MachineOperator(private val name: String) {
    fun checkIn() = checkedIn++
    fun checkOut() = checkedIn--

    companion object {
        var checkedIn = 0
        fun minimumBreak() = "15 min every 2hour"
    }
}

//2.1 伴生对象起名
class MachineOperator1(private val name: String) {
    fun checkIn() = checkedIn++
    fun checkOut() = checkedIn--

    companion object MachineOperatorFactory {
        var checkedIn = 0
        fun minimumBreak() = "15 min every 2hour"
    }
}

//3. 伴生对象作为工厂类，创建所属类的实例
class MachineOperator2 private constructor(private val name: String) {
    fun checkIn() = checkedIn++
    fun checkOut() = checkedIn--

    companion object {
        var checkedIn = 0
        fun create(name: String): MachineOperator2 {
            val instance = MachineOperator2(name)
            println(instance.checkIn())
            return instance
        }
    }
}





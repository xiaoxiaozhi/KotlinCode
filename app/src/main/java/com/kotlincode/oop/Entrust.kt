package com.kotlincode.oop

import kotlin.reflect.typeOf

/**
 * 委托
 * 1. 一个对象将它的一些职责委托或传递给另一个类的实例
 * 2. 委托比继承更加灵活，建议使用委托
 * 何时选择委托
 * 1. 如果你想用一个类的对象来代替另一个类的对象，请使用继承
 * 2. 如果你想让一个类的对象只使用另一个类的对象，请使用委托
 */
fun main() {

    //1. 委托给类
    val manager = Manager()
    manager.work()
    //2. 委托给一个参数
    Manager1(JavaProgrammer()).meeting()   //方法中调用委托实例
    Manager1(JavaProgrammer()).staff.work()//通过参数调用委托实例
    Manager1(JavaProgrammer()).work()      //调用委托方法 同 1
}

interface Worker {
    fun work()
    fun tackVacation()
}

class JavaProgrammer : Worker {
    override fun work() {
        println("write java ")
    }

    override fun tackVacation() {
        println("...code at the beach...")
    }
}

class CSharpProgrammer : Worker {
    override fun work() {
        println("write C# ")
    }

    override fun tackVacation() {
        println("...branch at the ranch...")
    }
}

//1. 委托给类    接口 by 实例
class Manager : Worker by JavaProgrammer()//无法访问委托实例，委托给参数很好的解决了这个问题

//2. 委托给参数
class Manager1(val staff: Worker) : Worker by staff {
    fun meeting() = println("organizing meeting with ${staff.javaClass.simpleName}")
}
package com.kotlincode.oop

/**
 * 数据类
 * 1.data class 的属性最好都定义在构造函数中，使用val修饰，修改数据，对象的地址也会改变切记
 * 2.data class 必须是final 不能被继承
 * 3.Kotlin将自动为构造函数中的属创建equals()、hashCode()和toString()方法
 * 4.主构造函数至少定义一个属性
 */
fun main() {
    //1. copy函数
    val task = Task(111, "data task", true, false)
    println(task)
    val task1 = task.copy(id = 2)//创建并赋值一个新对象，默认参数不包含在 body{}中定义的属性
    println("task1 = $task1 ")
    println("task1.age = ${task1.age}")//在类体重定义的属性也会被赋值过去
    //2. 解构
    val (id, name, completed, assigned) = task // 解构不包含在 类体中定义的属性
    //注意解构是基于顺序而不是名称
}

//Kotlin的数据类是专用类，主要用于承载数据而不是行为
data class Task(val id: Int, val name: String, val completed: Boolean, val assigned: Boolean) {
    val age: Int = 3
}//
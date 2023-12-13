package com.kotlincode

import kotlin.String

/**
 * 外部迭代和参数选配
 * 在foreach{}中删除元素会报错需要使用迭代器
 */
fun main() {
    ranges()
    println("")
    ergodic()
    println("")
    whenFunction()
    println(systemInfo())
    // 在迭代器中遍历的同时删除元素避免报错
    val s = mutableListOf<String>("1", "2", "3", "4", "5", "6", "7")
    while (s.iterator().hasNext()) {
        s.remove(s.iterator().next().apply(::print))
    }
}

//1.范围类 ranges
fun ranges() {
    //1.1 正向迭代 ..运算符 前面和后面的值是一个闭区间
    val array: IntRange = 1..5//int数组
    println("start ${array.first} end ${array.last}")
    val charArray: CharRange = 'a'..'e'//a到e的字符数组
    for (value in 1..5) print("$value ")  //一旦创建了范围就可以使用for迭代
    val strArray: ClosedRange<String> =
        "help".."hell" //IntRange 的基类 ClosedRange<?> 没有迭代函数iterator不能使用for循环迭代
    println("")
    //1.1.1 until 前闭后开区间
    for (value in 1 until 5) print("$value ")
    println("")
    //1.2 反向迭代 使用扩展函数downTo() 闭区间
    for (value in 5.downTo(1)) print("$value ")
    println("")
    //1.2.1 中缀表示法调用扩展函数
    for (value in 5 downTo 1) print("$value ")
    println("")
    //1.3 step 步长 规则的跳过一些值
    for (value in 1 until 10 step 2) print("$value ")
    println("")
    //1.4 filter 筛选 不规则的跳过一些值
    for (value in (1..9).filter { it % 2 == 0 }) print("$value ")


}

//2.遍历数组和列表
fun ergodic() {
    //2.1 创建数组  使用arrayOf()
    val array = arrayOf(1, 2, 3)
    println(array.javaClass)//Integer 数组，如果想创建int数组使用 intArrayOf()
//    for (value in array) print("$value")//使用for循环遍历
    //2.1 创建列表
    val list = listOf<Int>(1, 2, 3)
    println(list.javaClass)
    for (value in list.indices) print("index = $value , value = ${list[value]} ")// 使用indices 获取 列表的索引,list.get(value) 获取值
    //2.2 通过解构 得到值和索引(数组和列表都可以)
    for ((index, value) in list.withIndex()) print("index = $index , value = $value ")// 使用 withIndex 获取解构
}

// 3. when表达式 代替 if-else
fun whenFunction() {
    println(isAlive(true, 1))
    print(whatToDo(""))
}

//3.1 -> 允许后面是一个代码块{} 但是这种情况不推荐
fun isAlive(alive: Boolean, number: Int): Boolean = when {//kotlin 编译器将检查是否有else 如果没有会提示添加
    number < 2 -> false
    number > 3 -> false
    number == 3 -> true
    else -> alive && number == 2
}

//3.2 when() 传入一个参数，参数有什么作用吗？
fun whatToDo(dayOfWeek: Any) = when (dayOfWeek) {
    "Saturday", "Sunday" -> "Relax"
    in listOf<String>("Monday", "Tuesday", "Wednesday", "Thursday") -> "work hard"
    in 1..4 -> "work hard"
    "Friday" -> "Party"
    is String -> "What ?"
    else -> "no clue"
}

//3.3 when 用作语句(有无返回的区别)此时kotlin 并不关心是否提供了else分支
fun whatToDo1(dayOfWeek: Any) {
    when (dayOfWeek) {
        "Saturday", "Sunday" -> "Relax"
        in listOf<String>("Monday", "Tuesday", "Wednesday", "Thursday") -> "work hard"
        in 1..4 -> "work hard"
        "Friday" -> "Party"
        is String -> "What ?"
//        else -> "no clue"
    }
}

//3.4 when变量和作用域：把用于匹配的变量限制在when 作用域内部
fun systemInfo(): String {
    return when (val cores = Runtime.getRuntime().availableProcessors()) {
        1 -> "one core,packing this one to the museum"
        in 2..16 -> "you have $cores cores "
        else -> "$cores cores ,I want your machine"
    }
}


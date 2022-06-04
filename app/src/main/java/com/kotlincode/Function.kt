package com.kotlincode

/**
 *函数，
 * 1.所有的函数都可以当做表达式
 * 2.根据1所以函数都有返回值，如果没有明确指定则返回Unit
 * 3.参数必须指定类型,参数默认val修饰，函数体内参数不能被修改
 * 4.默认参数一般放在参数列表之后
 */
fun main() {
    println(greet())
    println(greet("Eva"))
    println(notReally())
    println(notReally(1))
//    switch("ok")
    createPerson("jack", height = 190, weight = 90)//对所有其它参数使用命名参数的时候，可以忽略age
    println("最大值:${max(1, 8, 3, 4)}")
    greetMany("hello", "tom", "jerry")
    val (first, second, third) = getFullName() //解构，将对象中的值提取到变量上面
    println("$first,$second,$third")
    val (first1, _) = getFullName()//解构，只要第一个
    val (_, second3) = getFullName()//解构，只要中间的值
    val (_, _, third2) = getFullName()//解构，只要最后值
    val (first2, _, third1) = getFullName()//解构，只要第一和第三
}

//1. 创建一个最小函数: fun 函数名 参数列表(可以为空) ，如果函数体只是一个表达式，
// 那么使用=运算符而不是{},这样的函数叫做短函数，只有函数是表达式而不是{}的时候kotlin才会进行类型推断，所以return不能用于短函数
fun greet() = "hello"

//2.Unit  如果表达式没有返回任何内容，kotlin奖把类型推断为Unit
fun sayHello() = println("hello")

//3.参数 必须指定类型，所有的参数都是val修饰，在函数体不能被修改
fun greet(name: String): String {
//    name = name.toUpperCase();
    return "hello $name"
}

//4.带有{}块的函数
//fun notReally() :Int= { 2 } 返回类型 = {} 这种形式是错误的
fun notReally() = { 2 }// kotlin 会认为=后面跟{} 是一个lambda表达式、或者是一个匿名函数
fun notReally(value: Int) = { n: Int -> n * value }// kotlin 推断也是一个lambda表达式，={}做法kotlin极不推荐

//5. 默认参数(变量名:类型 = 默认值) 在本例中默认参数还可以是一个字符串模板 msg:String ="${name.length}"
fun greet(name: String, msg: String = "hello") = "$msg,$name"

//5.1默认参数通常放在参数列表最后，调换也是可以的,但是这样做，会被迫给默认参数一个值，违背了默认参数的目的
fun switch(name: String = "Eva", msg: String) = "$msg,$name"

//6.命名参数,使用命名参数提高可读性, createPerson("jack",18,190,90) 调用时这样的一段代码可能看不懂，这时候使用命名参数提高可读性
fun createPerson(name: String, age: Int = 18, height: Int = 189, weight: Int = 80) =
    println("$name,$age $height $weight")

//7.可变数量参数,  numbers 类型被指定为 Int 而不是IntArray， 关键字vararg 将参数注释为指定类型的数组
//如果函数有多个参数的时候，只有一个参数能被vararg修饰
fun max(vararg numbers: Int): Int {
    var large = Int.MIN_VALUE
    for (number: Int in numbers) {
        large = if (number > large) number else large
    }
    return large
}

//7.1 函数接收两个参数其中一个是可变数量参数, 可变参数最好在最后一个，否则 其它参数就要使用命名参数
fun greetMany(msg: String, vararg names: String) = println("$msg  ${names.joinToString("、")}")

//8.spread运算符 *  ,max函数接收可变数量的参数，但有时候需要传递一个数组或者列表，vararg并不能接收数组或者列表
//这时候需要使用spread 把数组和列表拆成离散值， 使用方式：max(*intArrayOf(1,2,3)) ,在参数前面加 *

//9.解构 将值从现有对象提取到变量中。结构化或构造是从变量中创建对象而解构正好相反
fun getFullName() = Triple("john", "Quincy", "Adams")



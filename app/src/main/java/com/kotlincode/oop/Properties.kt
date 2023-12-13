package com.kotlincode.oop

/**
 * TODO  [待总结](https://kotlinlang.org/docs/properties.html#getters-and-setters)
 * 1.声明属性
 *   var可变属性 val只读属性
 * 2.Getters和Setters
 *   val 修饰的属性不能有 setter()
 *   private set 私有化set赋值报错，直接用val就好了为什么还要private set 有场景这么用吗？？？
 * 3.编译时常量
 *
 *
 */
const val c = "WWW"
fun main() {
    //1.

    name = "123"
    println("$name")
    //2.

//    PPP().sdpSettings ="123" 私有化set 赋值报错
    //3.编译时常量
    println("$c")//编译时常量，编译器将 const val 属性的值内联到使用它们的位置

}

//2. 系统对get和set的默认实现
var name: String? = "Android"
    get() = field //默认实现方式，可省略
    private set(value) { //默认实现方式，可省略
        field = value //value是setter()方法参数值，field是属性本身
    }

class PPP {
    var sdpSettings: String = ""
        private set

}
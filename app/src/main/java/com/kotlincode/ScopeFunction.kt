package com.kotlincode.efficient

import kotlin.String
import java.util.Locale
import kotlin.random.Random

/**
 * 作用域函数
 * 1. 带有接收者的函数类型，和扩展函数类似允许在函数体内部访问接收者对象的成员
 * 2. 主要功能是为调用者函数提供内部作用域。
 * 4. takeIf and takeUnless
 *    takeIf接收一个判断条件表达式，如果判断表达式为 true 则返回对象本身，false返回 null .takeUnless 与 takeIf 相反
 *
 */
fun main() {
    val length = 3
    //1. 没有指定接收方
    val printInt: (Int) -> Unit = { println("$it length $length") }//length不在lambda的内部作用域内。
    // 因此，编译器将length绑定到词法作用域（即lambda表达式的上面）内的变量
    //1.1 带有接收者的函数类型
    val printIt: String.(Int) -> Unit = { println("$this length $this.length") }
    printIt("hello", 6)//当调用一个带有接收方的lambda时，需要传递一个额外的参数
    "hello".printIt(6) //另一种调用方式，实际上kotlin就是把它当做扩展函数，函数名printIt。语法String.(Int)表示lambda将在String实例的上下文中执行。

    val result = "RESULT"
    //2. lambda被当做参数传入 没有this
    result.let { it.lowercase(Locale.ROOT) }              //返回lambda结果
    result.also { println() }                    //返回接收者
    //2.1 明确接收方 有this
    result.run { println(this.javaClass.name) }  //返回lambda结果
    result.apply { println(this.javaClass.name) }//返回接收者
    //2.2 调用者是参数，返回lambda结果
    with(result) {

    }
    //2.3 没有调用者，返回lambda结果
    run {

    }
    //3. 嵌套lambda的作用域
    top {
        println("$this ,$length")
        nest {
            println("inner lambda $this ${toDouble()}")
            println("$length")//作用域找不到变量length就向上找
            println("${this@top}")
        }
    }
    //4. takeIf and takeUnless
    val number = Random.nextInt(100)// 生成一个范围在0~x（不包含X）内的任意正整数
    val evenOrNull = number.takeIf { it % 2 == 0 }
    val oddOrNull = number.takeUnless { it % 2 == 0 }
    println("even: $evenOrNull, odd: $oddOrNull")
}

fun top(func: String.() -> Unit) = "hello".func()
fun nest(func: Int.() -> Unit) = (-2).func()
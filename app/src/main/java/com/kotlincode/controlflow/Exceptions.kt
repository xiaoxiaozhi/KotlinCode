package com.kotlincode.controlflow

/**
 * Kotlin 的所有异常类都继承 Throwable 类。每个异常都有一个消息、一个堆栈跟踪和一个可选原因。
 * 若要引发异常对象，请使用 throw 表达式: throw Exception("Hi There!")
 * 要捕获异常，请使用 try... catch 表达式
 * 1. try是一个表达式
 *    Try 表达式的返回值要么是 try 块中的最后一个表达式，要么是 catch 块(或块)中的最后一个表达式。Finally 块的内容不影响表达式的结果。
 * 2. Kotlin 不主动异常检查
 * 3. Noting类型
 *    throw 表达式返回Nothing类型。此类型没有值，用于标记永远无法到达的代码位置。在您自己的代码中，可以使用 Nothing 来标记永远不会返回的函数:
 *    [翻译文章](https://zhuanlan.zhihu.com/p/26890263) TODO 有点复杂 待看
 * 4. 使用runCatching{}函数式处理错误
 *    源码比较简单看下就能明白
 *
 */
fun main() {
    //1. try是一个表达式
    val a: Int? = try {
        3
    } catch (e: NumberFormatException) {
        null
    }
    println("try return $a")
    //
//    val s = null ?: throw IllegalArgumentException("Name required")
//    println("$s")
    println("----------")
    val x = null // 'x' has type `Nothing?`
    val l = listOf(null) //List<Nothing>
    //4. 使用 runCatching
    runCatching {
        getRandomNumber()
    }
        .onSuccess { println(it) }
        .onFailure { println(it.message) }

}

private fun getRandomNumber(): Int {
    val randomNumber = (1..20).shuffled().first()
    if (randomNumber % 2 == 0)
        return randomNumber
    else
        throw Exception("The random number is odd.")
}
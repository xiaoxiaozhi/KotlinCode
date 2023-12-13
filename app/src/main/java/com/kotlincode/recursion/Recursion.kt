package com.kotlincode.recursion

import kotlin.String
import java.math.BigInteger
import kotlin.system.measureTimeMillis

/**
 * 递归
 * 1. 尾递归调用优化：tailrec关键字 + 函数体最后一个操作是本函数
 */
lateinit var  lateinitVar: String
fun main() {
    //1. 见识递归
    print(sort(listOf(2, 1, 3, 4, 5)))
    //2. 尾调用优化
    // 当调用层数很多，代码就会运行失败,尾调用优化真正的好处是将递归过程编译成迭代过程
    println("\nreduce 实现阶乘${(1..10).reduce { acc, i -> acc * i }}")//reduce 函数实现阶乘
//    println((1..100000000).8fold(1.toBigInteger()) { acc, i -> acc * i.toBigInteger() })//fold 函数实现阶乘,足够大时 会产生堆栈溢出
//    println("\nfactorial = ${factorial(50000)}")
    //3. 扩展函数实现记忆
    lateinit var fib1: (Int) -> Long//lambda自己调用自己才有作用 ::fib,相当于lambda里面调用函数
    fib1 = { n: Int ->
        when (n) {
            0, 1 -> 1L
            else -> fib1(n - 1) + fib1(n - 2)
        }
    }.memoize<Int, Long>()
    println(measureTimeMillis { println(fib1(100)) })
//    println(measureTimeMillis { println(::fib.memoize<Int,Long>()(50)) })// 这种实现方式，不能达到优化目的，原因未知
    //4. 委托实现记忆
}

//1. 见识递归
fun sort(numbers: List<Int>): List<Int> = if (numbers.isEmpty()) numbers
else {
    val pivot = numbers.first()
    val tail = numbers.drop(1)//返回包含除前n个元素以外的所有元素的列表
    val lessOrEqual = tail.filter { it <= pivot }
    val larger = tail.filter { it > pivot }
    sort(lessOrEqual) + pivot + sort(larger)
}

//2. 尾调用优化
//2.1 函数体最后一个操作是n*函数不是 所以它不是尾递归调用优化，
tailrec fun factorialRec(n: Int): BigInteger =
    if (n <= 0) 1.toBigInteger() else n.toBigInteger() * factorialRec(n - 1)

//2.2 尾调用优化    tailrec + 函数体最后一个操作是本函数
tailrec fun factorial(n: Int, result: BigInteger = 1.toBigInteger()): BigInteger =
    if (n <= 0) result else factorial(n - 1, result * n.toBigInteger())

//3. 记忆函数结果
//3.1 普通斐波那契数列
fun fib(n: Int): Long = when (n) {
    0, 1 -> 1L
    else -> fib(n - 1) + fib(n - 2)
}

//3.2 对lambda表达式注入扩展函数
fun <T, R> ((T) -> R).memoize(): ((T) -> R) {
    val original = this
    val catch = mutableMapOf<T, R>()
    return { n: T -> catch.getOrPut(n) { original(n) } }
}

//4. 委托实现缓存
//class Memoize<T, R>(val func: (T) -> R) {
//    val cache = mutableMapOf<Int, Long>()
//    operator fun getValue(thisRef: Any?, property: KProperty<*>) =
//        { n: T ->
//            cache.getOrPut(n) { func(n) }
//
//        }
//}
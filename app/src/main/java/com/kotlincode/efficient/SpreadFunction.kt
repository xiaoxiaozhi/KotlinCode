package com.kotlincode.efficient

/**
 * 扩展函数
 *
 */
fun main() {
    val incrementDouble = ::increment.andThen(::double)
    println((::increment)(3))//::increment获取对函数的引用
    println(incrementDouble(5))
    println(
        "${
           tes {
                println("111111111111111111$c")
            }
        }"
    )

}

fun <T, R, U> ((T) -> R).andThen(next: ((R) -> U)): (T) -> U {
    return { T -> next(this(T)) }
}

fun increment(number: Int): Double = number + 1.toDouble()
fun double(number: Double): Double = number * 2
class ABC {
    val c: Int = 0
}

fun tes(a: ABC.() -> Unit) {
    ABC().a()
}
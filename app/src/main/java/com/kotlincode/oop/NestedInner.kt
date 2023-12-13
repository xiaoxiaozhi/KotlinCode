package com.kotlincode.oop

/**
 * https://kotlinlang.org/docs/nested-classes.html
 * 嵌套类和内部类
 * 感觉最大的区别就是标记为“内部”的嵌套类可以访问其外部类的成员
 * 匿名内部类也可以调用外部类成员
 * inner class XX
 * object {
 *      XXX
 *      XXX
 *  }
 *
 */
fun main() {
    val demo = Outer.Nested().foo() // == 2
    val demo1 = Outer1().Inner().foo() // == 1

}

class Outer {
    private val bar: Int = 1

    class Nested {
        fun foo() = 2
    }
}


class Outer1 {
    private val bar: Int = 1

    inner class Inner {
        fun foo() = bar
    }

    private val ob = object {
        fun sd() {
            bar
        }
    }
}


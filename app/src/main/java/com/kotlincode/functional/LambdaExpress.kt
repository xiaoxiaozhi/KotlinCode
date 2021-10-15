package com.kotlincode.functional

/**
 * lambda表达式
 * 1. lambda表达式是没有名称的函数，其返回类型是推断的
 * 2. {参数列表 -> 函数体} 函数体是单个语句或者多行语句
 * 3. 如果传递给函数的lambda只有一个参数，那么我们可以省略参数声明和->，而使用一个特殊的隐式名称it。
 */
fun main() {
    //1. lambda只有一个参数，我们可以省略参数声明和->，而使用一个特殊的隐式名称it
    println((0 until 10).none { it % 2 == 0 })//none 的括号也可以省略
    //2. 接收lambda
    walkTo({ print(it) }, 5)
    //2.1 当lambda 时函数最后一个参数的时候，可以对函数重新排列
    walk1To(5) {
        println(it)
    }
    //3.1 实例方法引用1
    walk1To(5, ::println)
    //3.2 实例方法引用2
    walk1To(5, System.out::println)
    //4. 函数返回函数
    val names = listOf<String>("Pam", "Pat", "Paul", "Paula")
    println(names.find(predicateOfLength(5)))
    println(names.find(predicateOfLength(4)))
    //5. 参数存储lambda
    val checkLength5: (String) -> Boolean = { it.length == 5 }
    println(names.find(checkLength5))
    //6.1 匿名函数 通过参数传递匿名函数
    val checkLength4 = fun(input: String): Boolean = input.length == 4
    println(names.find(checkLength4))
    //6.2 匿名函数 直接传递匿名函数
    println(names.find(fun(input: String): Boolean = input.length == 4))//和lambda调用方式一样
    // 匿名函数式最后一个参数时候 函数不支持重排，匿名函数比起lambda没有什么优势，所以最好使用lambda

}

// 2. 接收lambda
fun walkTo(action: (Int) -> Unit, n: Int) {
    (1 until n).forEach(action)
}

fun walk1To(n: Int, action: (Int) -> Unit) {
    (1 until n).forEach(action)
}

//4. 函数返回函数
fun predicateOfLength(length: Int) = { input: String -> input.length == length }
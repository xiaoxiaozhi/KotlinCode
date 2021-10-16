package com.kotlincode.functional

import java.lang.RuntimeException

/**
 * lambda表达式
 * 1. lambda表达式是没有名称的函数，其返回类型是推断的
 * 2. {参数列表 -> 函数体} 函数体是单个语句或者多行语句
 * 3. 如果传递给函数的lambda只有一个参数，那么我们可以省略参数声明和->，而使用一个特殊的隐式名称it。
 * 4. 默认情况下 lambda不允许使用return
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
    walk1To(5, ::println)// ::+函数名=lambda
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
    // 匿名函数式最后一个参数时候 函数不支持重排，匿名函数比起lambda没有什么优势，最好使用lambda
    //7. 闭包  依赖外部状态这样的lambda叫做闭包
    val factor = 2
    val doubleInt: (Int) -> Int = { it * factor }
    println(doubleInt(2))
    //8. 默认情况下 lambda不允许使用 return
//    caller()
    //8.1 带标签的return 跳出lambda
    caller1()
    //8.2 只有当接收lambda的函数使用inline定义时，才可能使用非局部return来从所定义的包含函数中退出
    caller2() // 查看forEach定义 他是一个inline函数,forEach 定义在caller2 函数中，退出后继续执行9. 如果定义在main()中，将直接退出不再执行9.
    //9. 带有lambda的内联函数
    println("---带有lambda的内联函数---")
    invokeTwo(1, ::report, ::report)//执行后发现调用堆栈有6层，使用inline 关键字减少堆栈
    //9.1 内联函数优化
    inlineInvokeTwo(1, ::report, ::report)//编译阶段是个函数，运行阶段 释放inLineInvokeTwo函数，减少调用层数
    //9.2 消除内联函数优化
    noinlineInvokeTwo(1, ::report, ::report)//如果内联的函数非常大，并且从很多不同的地方调用，
    // 那么生成的字节码可能比不使用inline时要大得多,不要盲目地优化。
    // 执行后可以观察到 action2 的堆栈层数恢复到优化前
    //9.3 crossinline参数
    //最后一行嵌入到lambda中的action2 (input)调用不能被内联,而crossInlineInvokeTwo 是个内联函数会产生冲突，编译器会报错
    //增加crossinline关键字 action2将在调用被内联，不是在invokeTwo()函数内，而是在调用它的任何地方
    crossInlineInvokeTwo(1, ::report, ::report)


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

//8. 默认情况下 lambda不允许使用return
fun invokeWith(n: Int, action: (Int) -> Unit) {
    println("enter invokeWith $n")
    action(n)
    println("exit invokeWith $n")
}

fun caller() {
    (1..3).forEach {
        invokeWith(it) {
            println("enter for $it")
//            if (it == 2) return  //编译器不通过，lambda不允许有return
            println("exit for $it")
        }
    }
    println("end of caller")
}

fun caller1() {
    (1..3).forEach {
        invokeWith(it) {
            println("enter for $it")
            if (it == 2) return@invokeWith
            println("exit for $it")
        }
    }
    println("end of caller")
}

fun caller2() {
    (1..3).forEach {
        println("before $it")
        if (it == 2) return// 查看forEach定义 他是一个inline函数
        println("after $it")
    }
}

//9. 带有lambda的内联函数
fun invokeTwo(n: Int, action1: (Int) -> Unit, action2: (Int) -> Unit): (Int) -> Unit {
    println("enter invokeTwo $n")
    action1(n)
    action2(n)
    println("exit invokeTwo $n")
//    return (fun(n: Int) { println("lambda returned from invokeTwo") })
    return ::println
}

inline fun inlineInvokeTwo(n: Int, action1: (Int) -> Unit, action2: (Int) -> Unit): (Int) -> Unit {
    println("enter invokeTwo $n")
    action1(n)
    action2(n)
    println("exit invokeTwo $n")
//    return (fun(n: Int) { println("lambda returned from invokeTwo") })
    return ::println
}

inline fun noinlineInvokeTwo(
    n: Int,
    action1: (Int) -> Unit,
    noinline action2: (Int) -> Unit //noinline 关键字消除内联函数优化
): (Int) -> Unit {
    println("enter invokeTwo $n")
    action1(n)
    action2(n)
    println("exit invokeTwo $n")
//    return (fun(n: Int) { println("lambda returned from invokeTwo") })
    return ::println
}

inline fun crossInlineInvokeTwo(
    n: Int,
    action1: (Int) -> Unit,
    crossinline action2: (Int) -> Unit //noinline 关键字消除内联函数优化
): (Int) -> Unit {
    println("enter invokeTwo $n")
    action1(n)
//    action2(n)
    println("exit invokeTwo $n")
//    return (fun(n: Int) { println("lambda returned from invokeTwo") })
    return { action2(it) }
}

fun report(n: Int) {
    println("")
    println("called with $n")
    val stackTrace = RuntimeException().stackTrace
    println("stack depth ${stackTrace.size}")
    println("partial listing of stack:")
    stackTrace.take(stackTrace.size).forEach(::println)
}

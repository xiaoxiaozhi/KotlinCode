package com.kotlincode

import java.lang.Exception
import java.util.*

/**
 * 语句与表达式
 */
fun main() {
    val greet = "hello"
    println(greet::class)
    println(greet.javaClass)
    var factor = 2;
    fun doubleInt(arg: Int): Int = arg * factor;
    factor = 1;
    print(doubleInt(2))
    //1. 相等性检查
    equality()
    //1. 字符串
    println("$factor")//①字符串模板--->${变量、表达式、函数}也可以省略{} 例如 $变量
    println("转义字符串\$,\"are you ok\"")//②转义字符串使用的越多，越混乱；
    println("""\n fox say: are you ok"  """);//③原始字符串三个" 内部也可以使用字符串模板${}，但是没有转义字符
    println(
        """
    Tell me and I forget.
    |Teach me and I remember.
    |Involve me and I learn.
    |(Benjamin Franklin)
    """.trimMargin()
    )//④多行字符串通原始字符串一样以三个"开始，三个"结束。除了"""所在的第一行，剩余每行默认有一个缩进，trimMargin()函数消除缩进直到|
    // 如果不想用|做分隔符也可以使用其他字符 trimMargin("<")
    //2. 更多的表达式更少的语句
    val introduce = if (Date().time > 0) "我今年18岁" else "未满18岁"//①if是一个表达式 ，根据年龄获取描述，想想java在这里会怎么做
    println("$introduce")
    println(tryExpr(true))//②try catch 也是一个表达式，没有异常的情况下 try 代码块最后一条表达式将成为结果，有异常的情况下则是catch
    //3.赋值不是表达式
    var a = 1
    var b = 2
    var c = 3
    a = b      //赋值
    println(a)
    //a = b = c//这种情况会报错 赋值不是表达式 相当于 a= (b=c) 由于b=c不是表达式 ，所以不能给a赋值
}

//1. 相等性检查 ==和===
fun equality() {
    //1.1 == 值相等
    println("hi" == "hi")//  true 类似java 中的equals，但是kotlin的==更好能够处理null，kotlin的==运算符实际是equals方法的映射，点进去发现是equals的源码
    println("hi" == null)//  false
    //1.2 === 引用比较 两个引用是否相等

}

fun tryExpr(bloweUp: Boolean): Int {
    return try {
        if (bloweUp) {
            throw Exception();
        } else {
            3
        }
    } catch (e: Exception) {
        4
    } finally {
        5
    }
}




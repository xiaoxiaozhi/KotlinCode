package com.kotlincode

import java.lang.RuntimeException
import java.lang.StringBuilder
import kotlin.math.sqrt

/**
 * 类型安全
 * 1.安全调用运算符 !.和 Elvis运算符 ?:是处理可空类型的最好方式
 * 2.尽可能多的使用智能转换，只有无法使用被迫要显示转换的时候用一定要用安全显式转换as?
 */
fun main() {
    println("-----Any-----")
    //1. any  kotlin中所有的类都继承自Any，目的是为了提供一些通用的方法 equals、hashCode、toString、to
//    println(computeSqrt(-1).javaClass)
    println("-----null-----")
//    nickName(null)//null 作为参数编辑器不ton过
    println(nickName1("Bill"))
    println(nickName2(null))
    println(nickName3(null))
    println(nickName5(null))
    println("-----类型检查-----")
    println(fetchMessage("2"))
//    println("${(fetchMessage1(2) as String).length}")//as显示转换不推荐，类型转化错误会报异常
    println("${(fetchMessage1(2) as? String)?.length}")//显示转换不推荐,转换错误会返回null

}

//1.1. Nothing 类 没有实例表示一个永远不存在的值或者结果，当用作方法返回时表示函数永远不会返回
fun computeSqrt(n: Int): Double {
    if (n > 0) {
        return sqrt(n.toDouble())
    } else {
        throw RuntimeException("No negative please")//实际打印并未看见Nothing类，存疑
    }
}

//2. null kotlin 不允许null 作为方法参数 也不允许作为返回
fun nickName(name: String): String {
    if (name == "William") {
        return "Bill"
    } else {
//        return null     //作为方法返回，编辑器不通过‘
    }
    return ""
}

// 2.1 使用可空类型 返回和接收 null
fun nickName1(name: String?): String? {
    return if (name == "William") {//每一个非空类型 都有对应的可空类型（非空？），可空类型能够引用非空和null
        "Bill"
    } else {
        null
    }
}

//2.2 安全调用运算符
fun nickName2(name: String?): String? {
    return if (name == "William") {//每一个非空类型 都有对应的可空类型（非空？），可空类型能够引用非空和null
        "Bill"
    } else {
        name?.reversed()?.toUpperCase()   //对于可空类型的方法调用，使用安全调用运算符 (?.)
    }
}

//2.3 Elvis 运算符
fun nickName3(name: String?): String {
    return if (name == "William") {//每一个非空类型 都有对应的可空类型（非空？），可空类型能够引用非空和null
        "Bill"
    } else {
        name?.reversed()?.toUpperCase() ?: "Joker"   //如果不为空返回左侧表达式值，否则返回右侧表达式的值
    }
}

//2.3 !!非空断言运算符  （不建议使用）
fun nickName4(name: String?): String {
    return if (name == "William") {//每一个非空类型 都有对应的可空类型（非空？），可空类型能够引用非空和null
        "Bill"
    } else {
        name!!.reversed()
            .toUpperCase()    //如果确定可空变量不为空，想停止非空检查，使用!!运算符，但是这样带来问题，如果可空变量本来就是null，运行时会报空异常
    }
}

//2.4 when
fun nickName5(name: String?) = when (name) {
    "William" -> "Bill"
    null -> "Joker"
    else -> name.reversed().toUpperCase()
}//如果对可控引用不单单是提取值二是根据引用做不同操作，最好使用when

//3. is运算符与智能转换
fun fetchMessage(msg: Any) = when (msg) {
    null -> "msg is null"
    is Int -> "msg = $msg"               //is 运算符，实例是否是指定类的实例，是的话就返回true
    is String -> "msg 长度${msg.length}" //一旦用is运算符确定类型，kotlin就可以执行智能转换，无需显式转换就可以调用 String的length方法
    else -> "other type"
}

//3.1 as和as?  转换运算符和安全转换运算符 显式转换
fun fetchMessage1(id: Int) =
    if (id == 1) "Record found" else StringBuilder("date not found")//有风险不推荐显式转换

class Animal {
    override fun equals(other: Any?): Boolean {
        return other is Animal
    }
}
//4. 泛型

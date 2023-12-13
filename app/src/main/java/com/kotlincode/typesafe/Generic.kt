package com.kotlincode.typesafe

import kotlin.String
import java.io.Closeable
import java.io.StringWriter

/**
 * 基本和java泛型一致，回顾请参考java泛型java 通配符是? kotlin是 *； 多下界表示 java是 <? super 类名&类名> kotlin 是 where T : Closeable, T : Appendable
 * 另外多了一个关键字reified
 * 泛型
 */
fun main() {
    //1. 函数参数类型不变性(Array<T>和List<out E>)
    val bananas = arrayOf<Banana>()
//    receiveFruit1(bananas)// 同java一样不允许传递，泛型类型不变性决定，理由：一筐子香蕉不是从一筐子水果继承来的
    val orange: List<Orange> = listOf();
    receiveFruit2(orange)//限制了数组，但没有限制列表,原因是数组接口和列表接口后者有向下兼容List<out E>
    //2. 协变
    val apples = Array<Apple>(3) { _ -> Apple() }//用不到的参数用下划线省略
    val fruits = Array<Fruit>(3) { _ -> Fruit() }
    copyFromTo(apples, fruits)
    //3. 逆变
    val things = Array<Any>(3) { _ -> Any() }
    copyFromTo1(apples, things)
    //4. where 参数类型约束
    val write = StringWriter()
    write.append("hello")
    useAndClose(write)
    println(write)
    //4.2
    val write1 = StringWriter()
    write1.append("hello")
    userAndClose1(write1)
    println(write1)
    //5. 星投影
    printValue(arrayOf(1, 2))
    //6. 具体化类型参数 reified
    println(findFirst<NonFiction>(listOf(NonFiction("learn to code"), Fiction("game"))).name)

    val numbers: MutableList<in Int> = mutableListOf<Number>()
    numbers.add(100)
    println("类型----${numbers[0]?.javaClass?.simpleName}")

}

open class Fruit {
    override fun toString(): String {
        return hashCode().toString();
    }
}

class Banana : Fruit()
class Orange : Fruit()
class Apple : Fruit()
class Grape : Fruit()

fun receiveFruit1(fruit: Array<Fruit>) {
    println("Num of fruit:${fruit.size}")
}

fun receiveFruit2(fruit: List<Fruit>) {
    println("Num of fruit:${fruit.size}")
}

//2. 协变 使方法参数接收类型的子类
fun copyFromTo(from: Array<out Fruit>, to: Array<Fruit>) {
    for (i in from.indices) {
        to[i] = from[i]
//        from[i] = Fruit()//2.1 在kotlin和java中使用协变的参数只能读取不能修改
    }
    println("to value is ${to.size}")
}

//3. 逆变 使方法参数接收类型的父类
fun copyFromTo1(from: Array<out Fruit>, to: Array<in Fruit>) {
    for (i in from.indices) {
        to[i] = from[i]
        val fruit = to[i]//3. 使用逆变的参数即可以读也可以写
    }
}

//4.1 通过参数类型约束，约束参数为具有该方法的类型
fun <T : Closeable> useAndClose(input: T) {
    input.close()
}//但类型参数约束  <T:类型>

//4.2 多类型参数约束
fun <T> userAndClose1(input: T) where T : Closeable, T : Appendable {
    input.append("123")
    input.close()
}//where 泛型:类,泛型:类

//5. 星投影
fun printValue(input: Array<*>) {
    for (value in input) {
        println(value)
    }
}// 对传入的参数只允许读不允许写

//6. 具体化类型参数
abstract class Book(val name: String)
class Fiction(name: String) : Book(name)
class NonFiction(name: String) : Book(name)

inline fun <reified T> findFirst(books: List<Book>): T {
    val selected = books.filter { book -> book is T }
    if (selected.isEmpty()) {
        throw RuntimeException(" Not found")
    }
    return selected[0] as T
}//当方法中需要明确参数类型： 内联函数 reified关键字
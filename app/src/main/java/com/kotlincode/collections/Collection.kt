package com.kotlincode.collections

import android.os.Build
import androidx.annotation.RequiresApi
import com.kotlincode.myCoroutine.Util
import kotlin.String
import kotlinx.coroutines.*
import java.lang.Exception


/**
 * 集合 列表、序列操作查看代码LambdaIteration.kt
 * 1. 元组
 *    1.1 Pair   两个值的元组 元组中的值可以是不同类型
 *    1.2 Triple 三个值的元组
 * 2. Array  数组
 * 3. List有序集合
 *    Iterable会急切地执行这些步骤: 每个处理步骤完成并返回其结果——一个中间集合。在此集合上执行以下步骤。
 * 4. Set    无序集合
 * 5. Map    映射
 * 6. Sequences
 *    官方文档比较简陋没有什么例子
 *    与集合不同，序列不包含元素，它们在迭代时产生元素。当序列开始迭代，构建器执行。构建器每产生一个元素，就会通过中间操作、终端操作。
 *    而集合是一次性产生所有元素，然后所有元素通过中间操作产生新的集合才会进行下一步中间操作直至终端操作。
 *    序列的中间操作:和集合相同 (filter、map......)
 *    序列的终端操作有:forEach 、to集合类
 *
 *    attention：序列的构建器和终端操作可能处于不同的线程和协程上面。当开始终端操作时，构建器会在终端操作的线程和协程上面运行。
 *    TODO [集合转换操作](https://kotlinlang.org/docs/collection-transformations.html)尚未总结： Map、Zip、Associate、Flatten、String representation
 */
@RequiresApi(Build.VERSION_CODES.N)
fun main() {
    println("-----二值元组-----")
    pairFunction()
    println("-----数组-----")
    arrays()
    println("-----List-----")
    lists()
    println("-----Set-----")
    sets()
    println("-----Map-----")
    maps()
    println("-----Sequences-----")
    generateSequences()
    println("-----sorted-----")
    kotlinSort()
}

/**
 * 1.1 Pair元组
 */
fun pairFunction() {
    // 1.1 创建Pair
    println(Pair("Tom", "Jerry"))//kotlin 提供了两种特定类型的元组Pair和Triple 用于快速创建
    //1.2 to方法创建   第一个值 to 第二个值
    println(mapOf("Tom" to "Cat", "Jerry" to "Mouse"))// "Tom" to "Cat"  返回一个pair实例
    airport()
    println("")
}

fun airport() {
    val airportCodes = listOf<String>("LAX", "SFO", "PDX", "SEA")
    val temperatures = airportCodes.map { code -> code to getTemp(code) }//
    for (value in temperatures) {
        print("${value.first},${value.second}  ")
    }
}

fun getTemp(name: String): String = "${Math.round(Math.random() * 30) + name.count()}"

//2.数组
fun arrays() {
    //2.1 创建数组的最简单方式是 arrayOf() 一旦创建了数组就可以使用[index] 访问
    println("arrayOf() 创建一个Integer类型的数组${arrayOf(1, 2, 3).javaClass}如果想创建原始类型数组，最好使用intArrayOf()")//
    val numbers = intArrayOf(1, 2, 3)
    //2.2 操作方法
    numbers + 4        //增加一个元素返回新的列表
//    numbers - 1      //不支持-操作符
    numbers[0]         //取值
    numbers[0] = 2     //修改值
    numbers.size       //数组的大小
    numbers.average()  //数组平均值,数值类型才有
    1 in numbers       //判断1是否在数组中
    numbers.contains(1)//判断1是否在数组中
}

//3.列表
fun lists() {
    //3.1 不可变列表
    val fruits = listOf("Apple", "Banana", "Grape")//listOf()创建不可变列表使用
    //3.2 可变列表
    val numbers = mutableListOf(1, 2, 3)
//    numbers.map {
//        println("numbers map = $it")
//        it + 1 }.forEach { item -> println("numbers forEach = $item") }
    numbers[0] = 2 //改变值
    println("创建可变列表---$numbers")
    //3.3 List 集合类型
    println("不可变List集合类型${fruits.javaClass} 可变List集合类型${numbers.javaClass}")
    //3.4 取值
    fruits[0]//使用[]比使用get()更加清晰方便
    //3.5 列表的一些方法 同 数组 一样
    //3.6 + 运算符创建新的列表
    val fruits2 = fruits + "Orange"
    println(fruits2)
    //3.7 - 运算符创建新的列表
    val fruits3 = fruits - "Grape"//如果-运算符后面的参数不属于列表，则返回原列表
    println(fruits3)
}

//4. Set 无序集合 set中的元素是唯一的
fun sets() {
    //4.1 创建一个不可变的 Set集合
    val fruits = setOf("Banana", "Apple", "Apple")
    println(fruits)
    //4.2 创建一个可变的 Set集合
    val fruits1 = mutableSetOf("Banana", "Apple", "Apple")
    fruits1.add("Grape")//添加  //Set不能取值？？？？
    println(fruits1)
    //4.3 类型
    println("不可变Set集合类型${fruits.javaClass} 可变Set集合类型${fruits1.javaClass}")
    //4.4 + - in contains average等操作同List
    //4.5
}

//5. Map 映射
@RequiresApi(Build.VERSION_CODES.N)
fun maps() {
    //5.1 同列表一样Map也提供了可变和不可变接口
    val values = mapOf<String, String>("baidu" to "www.baidu.com", "sine" to "www.sine.com")
    for ((key, value) in values) println("key = $key , value = $value")
    //5.2 类型
    println("不可变${values.javaClass} 可变${mutableMapOf("" to "").javaClass}")
    //5.3 遍历
    for (key in values.keys) print("key = $key ")         //keys属性遍历所有的健
    for (value in values.values) print("value = $value ") //values属性遍历所有的值
    println("")
    //5.4 操作
    values.containsKey("baidu")                    //判断健是否存在
    values.containsValue("www.apple.com")          //判断值是否存在
    println("baidu 是否存在 ${"baidu" in values}") //in 判断键值是否存在
    values + ("tencent" to "www.tencent.com")     // 增加一个键值对 返回新的map
    values - "baidu"                              // 删去一个键值对
    //5.5 根据键取值
    values.get("baidu")
    values["baidu"]
    values.getOrDefault("apple", "default") //如果没有则返回默认值
}

//6. 序列
fun generateSequences() {
    sequenceOf("1", "2", "3")//6.1 从一堆元素构建序列
    listOf<String>("1", "2", "3", "4").asSequence()//6.2 从可迭代对象构建序列
    generateSequence(1) { if (it < 8) it + 2 else null }//6.3 从generateSequence函数构建对象，参数是第1个值，直到返回null 序列结束
    sequence {//6.4 从代码块构建序列
        yield(1)// 单个元素插入到序列
        println("yield1")
        yieldAll(listOf(3, 5))
        println("yield35")
        yieldAll(generateSequence(7) { it + 2 })
    }

    //序列迭代，sequence执行hasNext，才会触发sequence内部逻辑，直到第一个yield为止
    val seq = sequence {
        Util.log("A1")
        yield(1)
        Util.log("A2")
        Util.log("B1")
        yield(2)
        Util.log("B2")
        Util.log("Done")
    }

    Util.log("before sequence")
    for (item in seq) {
        Util.log("Got $item")
        break
    }
    // 序列的计算过程处于它迭代的线程和协程上面
    runBlocking() {
        val words = "The quick brown fox jumps over the lazy dog".split(" ")
        //convert the List to a Sequence
        val wordsSequence = words.asSequence()
        val lengthsSequence = wordsSequence.filter { Util.log("filter: $it"); it.length > 3 }
            .map { Util.log("length: ${it.length}"); it.length }
            .take(4)
        println("Lengths of first 4 words longer than 3 chars")

        newSingleThreadContext("aaa").use {
            launch(it) {
                println(lengthsSequence.toList())
            }
        }
    }
    // 取列取消
    println("取列序列---------------")
    runBlocking {
        val seq = sequence {
            repeat(Int.MAX_VALUE) {}
            yield(1)
            repeat(Int.MAX_VALUE) {}
            yield(2)
            repeat(Int.MAX_VALUE) {}
            yield(3)
            repeat(Int.MAX_VALUE) {}
            yield(4)
            repeat(Int.MAX_VALUE) {}
            yield(5)
            repeat(Int.MAX_VALUE) {}
            yield(6)
        }
        launch(Dispatchers.Default) {
            try {
                withTimeout(10) {
                    seq.forEach {
                        if (isActive) {
                            println(it)
                        }
                    }
                }
            } catch (e: Exception) {
                println(e.message)
            }
        }
    }
}


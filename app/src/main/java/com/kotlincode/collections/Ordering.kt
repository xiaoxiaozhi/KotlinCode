package com.kotlincode.collections

import kotlin.String

/**
 * 1.排序
 *   1.1 自然秩序：它是为 Compable 接口的继承者定义的。。当没有指定其他顺序时，自然顺序用于对它们进行排序。大多数内置类型都属于此列(整数、浮点、char、String)
 *       Comparable的子类 用 compareTo()比较大小 正值表示接收方大于参数；负值表示它小于参数;零表示对象是相等的;
 *   1.2 自定义排序：创建Comparator比较器为非可比对象定义顺序，或者为可比类型定义非自然的顺序
 *   1.3 使用sorted() 和 sortedDescending()函数为自然元素排序(升序、降序)
 *   1.4 使用sortedBy() 和 sortedByDescending()函数对非自然元素排序，该函数将集合元素映射到 继承了Compable的元素，并按该值的自然顺序对集合进行排序。
 *       假设 listOf("one", "two", "three", "four")是非自然元素，无法排序，但是他们的长度为自然元素，利用这个进行排序
 *       若要为集合排序定义自定义顺序，您可以使用sortedWith ()函数提供自己的比较器
 *   1.5 翻转顺序 reversed() 和asReversed() 返回原始集合的反转后的副本，后者不同的是，如果原始集合有修改，会反应在副本集合上
 *   1.6 随机排序 shuffled()
 *   ---------------------------来自网络--------------------
 *   1.7 多级排序
 *       非常方便 用有没有- 来确定升序还是降序， 注意 不可改变集合用 sortWith排序  可改变集合用sortedWith排序。
 *
 *
 */
fun main() {
    kotlinSort()
}

fun kotlinSort() {
    //1.1 自然排序
    println(Version(1, 2) > Version(1, 3))
    println(Version(2, 0) > Version(1, 5))
    //1.2 自定义排序
    val lengthComparator = Comparator { str1: String, str2: String -> str1.length - str2.length }
    println(listOf("aaa", "bb", "c").sortedWith(lengthComparator))
    println(listOf("aaa", "bb", "c").sortedWith(compareBy { it.length })) //更简单的方法定义比较器，查看源码发现，每个元素提供一个能够自然排序的元素，并按照该元素进行排序
    //1.3 使用sorted() 和 sortedDescending()
    val numbers = listOf("one", "two", "three", "four")
    println("Sorted ascending: ${numbers.sorted()}")
    println("Sorted descending: ${numbers.sortedDescending()}")
    //1.4 sortedBy() 和 sortedByDescending()
    val numberss = listOf("one", "two", "three", "four")
    val sortedNumbers = numberss.sortedBy { it.length }
    println("Sorted by length ascending: $sortedNumbers")
    val sortedByLast = numberss.sortedByDescending { it.last() }
    println("Sorted by the last letter descending: $sortedByLast")
    //1.5 翻转
    val nums = mutableListOf("one", "two", "three", "four")
    println(nums.reversed())
    val reversedNumbers = nums.asReversed()
    println(reversedNumbers)
    nums.add("123")
    println(reversedNumbers)
    //1.6 随即排序
    val numbersss = listOf("one", "two", "three", "four")
    println(numbersss.shuffled())//每次结果都不一样

    //1.7 多级排序
    val sortedList = personList.sortedWith(compareBy(
        { it.name }, // 按姓名升序
        { -it.age }, // 按年龄降序
        { it.height } // 按身高升序
    ))

    sortedList.forEach { println(it) }
}

class Version(val major: Int, val minor: Int) : Comparable<Version> {
    override fun compareTo(other: Version): Int = when {
        this.major != other.major -> this.major.compareTo(other.major) // compareTo() in the infix form
        this.minor != other.minor -> this.minor.compareTo(other.minor)
        else -> 0
    }
}

val personList = listOf(
    Person("John", 30, 175),
    Person("Alice", 25, 165),
    Person("Bob", 25, 180),
    Person("John", 25, 180),
    Person("Alice", 30, 170)
)

data class Person(val name: String, val age: Int, val height: Int)

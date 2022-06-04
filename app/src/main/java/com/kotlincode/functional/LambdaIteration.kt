package com.kotlincode.functional


/**
 * 用lambda表达式实现内部迭代
 * Kotlin标准库为集合添加了许多扩展函数。
 * 1. filter 过滤 first、last
 * 2. map    映射 flatMap、flatten
 * 3. reduce 累计 sum
 * 4. sortedBy
 * 5. 简单地说，集合是急切的，而序列是懒惰的。序列是对集合进行优化的包装器，旨在提高性能。比如，中间操作 map filter 结尾操作forEach。列表是所有的元素都在map执行完再执行filter，直到forEach。而序列是一个元素进过map filter 再到 forEach
 *    [列表和序列的处理流程图](https://kotlinlang.org/docs/sequences.html#sequence-processing-example)
 *    如果集合很小，那么性能上的差异几乎可以忽略不计。在这种情况下，更容易调试和推理的及早求值可能会更好
 */
fun main() {
    //1. 外部迭代与内部迭代
    for (i in 1..10) if (i % 2 == 0) print(i)//外部迭代
    println("")// 元素超过千级，内部迭代会损失些性能
    (1..10).filter { it % 2 == 0 }.forEach(::print)//内部迭代
    //2. reduce()
    println("\n---reduce---")
    val result = people.filter { it.age > 20 }.map { it.firstName }.reduce { acc, s -> "$acc,$s" }
    println(result)// reduce 传入两个参数 acc是前面参数的结果，s是下一个参数
    //2.1 sum
    println("sum = ${people.map { it.age }.sum()}")//专用的reduce操作sum()
    //3.1 过滤 first、last
    println("first = ${people.first()}")
    println("first = ${people.last()}")
    //4.1 映射 flatten
    println("families size is ${families.size},after flatten size is ${families.flatten().size}")//2纬列表转1纬列表
    //4.2 flatMap  map与flatten结合
//    val namesAndReversed =
//        people.map { it.firstName }.map { it.toLowerCase() }
//            .flatMap { name: String -> listOf<String>(name, name.reversed()) }.forEach(::println)// 未知原因报错，暂且注释

    //5.1 排序sortedBy 升序
    println(people.sortedBy(Person::age).first())//升序
    //5.2 降序sortedByDescending
    println(people.sortedByDescending(Person::age).first())//升序
    //6. 分组 groupBy 根据类的 属性进行分组
    people.groupBy { it.firstName.first() }.forEach(::println)
    //6.1 对类中的属性分组
    people.groupBy({ it.firstName.first() }) { it.firstName }.forEach(::println)
    //7. 延迟计算 使用序列提高性能
    println("---延迟计算---")
    println(
        people.filter(::isAdult).map(::fetchFirstName).first()
    )//可以观察到每一个元素都执行filter和map，如果这个列表有成千上万个，性能浪费将十分巨大
    println("---序列优化---")
    println(
        people.asSequence().filter(::isAdult).map(::fetchFirstName).first()
    )//使用asSequence()列表转换成序列
//    people.asSequence().filter(::isAdult).map(::fetchFirstName).forEach { println("people ---$it") }
    //序列执行中间操作时会返回一个序列，当调用最后一个终端方法的时候，延迟的filter()和map()操作将被调用。


}

data class Person(val firstName: String, val age: Int, var toll: String = "s")

val people = listOf(
    Person("Sara", 18),
    Person("Jill", 20), Person("Paula", 30), Person("Paul", 25)
)
val families = listOf(
    listOf(Person("Sara", 18), Person("Jill", 20)),
    listOf(Person("Paula", 30), Person("Paul", 25))
)

fun isAdult(person: Person): Boolean {
    println("isAdult called for ${person.firstName}")
    return person.age > 17
}

fun fetchFirstName(person: Person): String {
    println("fetchFirstName called for ${person.firstName}")
    return person.firstName
}
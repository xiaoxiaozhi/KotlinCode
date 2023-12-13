package com.kotlincode.test

import android.os.SystemClock


suspend fun main() {
    //遍历一个删除一个能行吗？？？ foreach实现不了，需要迭代器来删除元素
//    val s = mutableListOf<String>("1", "2", "3", "4", "5", "6", "7")
//    s.iterator().also {
//        while (it.hasNext()) {
//            print(it.next())
//            it.remove()
//        }
//    }
//    println(s.size)
//    val executorService = Executors.newSingleThreadExecutor()
//    repeat(3) {
//        executorService.execute {
//            println("1234")
//            Thread.sleep(1000)
//        }
//    }

//    newSingleThreadContext("context1").use {
//
//    }
//    newSingleThreadContext("context1").use {
//        println("5678")
//        delay(1000)
//    }
//    groupBy2()
//    var t2 = TT("1")
//    println("TT---${t2.hashCode()}")
//    t2.name="2"
//    println("TT---${t2.hashCode()}")
//    val t3 = TTT("1")
//    println("TTT---${t3.hashCode()}")
//    t3.name="2"
//    println("TTT---${t3.hashCode()}")
//    println("TTT---${t3.name}")
//    ("" + "").takeIf {
//        it.isEmpty() }?.run {
//        "空"
//    }.also (::println)
    println("----${Long.MIN_VALUE}")
    println("联调标题-2023年度骑行报告".contains("骑行报告"))

}

data class TT(var name: String)
class TTT(var name: String)

private fun groupBy() {
    val mList = arrayListOf(0, 1, 2, 3, 4, 5, 6)
    val groupByList = mList.groupBy {
        //定义key
        it % 2 == 0
    }
    println(groupByList)
    //这里返回两个元素的map
    //{true=[0, 2, 4, 6], false=[1, 3, 5]}
    groupByList.entries.forEach {
        println("${it.key}==========${it.value}")
    }
    //true==========[0, 2, 4, 6]
    //false==========[1, 3, 5]

}

private fun groupBy2() {
    val mList = arrayListOf(0, 1, 2, 3, 4, 5, 6)
    val groupByList = mList.groupBy {
        //定义key
        if (it % 2 == 0) {
            "偶数"
        } else {
            "奇数"
        }
    }
    println(groupByList)
    //这里返回两个元素的map
    //{偶数=[0, 2, 4, 6], 奇数=[1, 3, 5]}
    groupByList.entries.forEach {
        println("${it.key}==========${it.value}")
    }
    //偶数==========[0, 2, 4, 6]
    //奇数==========[1, 3, 5]

}


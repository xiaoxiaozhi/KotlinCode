package com.kotlincode

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * [序列化](https://kotlinlang.org/docs/serialization.html) 太简单了感觉没啥用，看下面
 * [序列化指南](https://github.com/Kotlin/kotlinx.serialization/blob/master/docs/serialization-guide.md) TODO
 * 序列化是将应用程序使用的数据转换为可以通过网络传输或存储在数据库或文件中的格式的过程。反序列化则是从外部源读取数据并将其转换为运行时对象的相反过程
 * 一些序列化格式(JSON、 protocol buffers)与平台无关，因此可以在不同平台之间传播
 * 在Kotlin中序列化工具在一个单独的包中 kotlinx.serialization.它有两部分组成:Gradle插件和org.jetbrains.kotlin.plugin.serialization库
 * (点开@Serializable发现该注解存在于kotlinx-serialization-core-jvm-1.4.1.jar与描述不符)
 * 1. Kotlin序列化用到的工具包
 *    kotlinx.serialization提供一些列工具给所支持的平台JVM，js，Native。以及提供一系列工具支持 JSON、CBOR、protocol buffers等协议
 *    这些工具以org.jetbrains.kotlinx:kotlinx-serialization开头以支持的协议结尾例如：org.jetbrains.kotlinx:kotlinx-serialization-json
 * 2. Kotlin序列化支持的格式
 *    Kotlin序列化支持以下协议：
 *    JSON: kotlinx-serialization-json(除了这个库是正式的，其他所有库的是实验性质的)
 *    Protocol buffers: kotlinx-serialization-protobuf
 *    CBOR: kotlinx-serialization-cbor
 *    Properties: kotlinx-serialization-properties
 *    HOCON: kotlinx-serialization-hocon (only on JVM)
 * 3. 实践 Json序列化
 *    [官网的不行,在溢栈找到了其它方式](https://stackoverflow.com/questions/71988144/serializer-for-class-is-not-found-mark-the-class-as-serializable-or-prov)
 *    在根目录的build.gradle中增加
 *    plugins {
 *       id 'org.jetbrains.kotlin.jvm' version '1.7.21' apply false
 *       id 'org.jetbrains.kotlin.plugin.serialization' version '1.7.21' apply false
 *    }
 *    然后再在app的build.gradle中添加依赖   implementation 'org.jetbrains.kotlinx:kotlinx-serialization-json:1.4.1'
 *    然后在proguard-rules.pro文件中 [添加这段](https://github.com/Kotlin/kotlinx.serialization#android)
 *    以上方式实测会报找不到序列化类的错误，根据在溢栈找到方法，还要在build.gradle 中添加插件   plugins {id 'kotlinx-serialization'}
 *    3.1 序列化Kotlin对象  给要被序列化的对象增加@Serializable 注解  Json.encodeToString(Data(42, "str")) 得到该对象的Json字符串
 *    3.2 序列化Kotlin对象的集合
 *    3.2 反序列化 从字符串得到Kotlin对象
 *
 *    在.kt文件中运行失败在Activity中运行成功
 *
 */
fun main() {

    //3.1 序列化对象
    val json = Json.encodeToString(Data(42, "str"))
    println("序列化对象-----$json")
    //3.2 序列化该对象的集合
    val dataList = listOf(Data(42, "str"), Data(12, "test"))
    val jsonList = Json.encodeToString(dataList)
    println("序列化该对象的结合-----$jsonList")

    //3.3 反序列化
    val obj = Json.decodeFromString<Data>("""{"a":42, "b": "str"}""")
    println("反序列化-----$obj")

}


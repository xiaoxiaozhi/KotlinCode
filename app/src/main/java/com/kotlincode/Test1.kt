package com.kotlincode

fun main() {
    greet111("asd","sd")
    createPerson333("asd",12,23,434)
}
fun greet111(name: String, msg: String = "hello") = "$msg,$name"

//5.1默认参数通常放在参数列表最后，调换也是可以的,但是这样做，会被迫给默认参数一个值，违背了默认参数的目的
fun switch222(name: String = "Eva", msg: String) = "$msg,$name"

//6.命名参数,使用命名参数提高可读性, createPerson("jack",18,190,90) 调用时这样的一段代码可能看不懂，这时候使用命名参数提高可读性
fun createPerson333(name: String, age: Int = 18, height: Int = 189, weight: Int = 80) =
    println("$name,$age $height $weight")
class Person111(val name: String) {
    override fun equals(other: Any?): Boolean {
        return when (other) {
            !is Person -> false
            else -> this.name == (other as Person111).name
        }
    }

}
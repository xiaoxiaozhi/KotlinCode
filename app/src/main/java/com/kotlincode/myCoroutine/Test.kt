package com.kotlincode.myCoroutine
private fun log(message: String) = println("[${Thread.currentThread().name}] $message")
fun main(){
    val sequence = sequence {
        log("A1")
        log("A1")
        log("A1")
        log("A1")
        yield(1)
        log("A2")
        log("B1")
        yield(2)
        log("B2")
        log("Done")
    }
    log("before sequence")
    for (item in sequence) {
        log("Got $item")
        break
    }
}
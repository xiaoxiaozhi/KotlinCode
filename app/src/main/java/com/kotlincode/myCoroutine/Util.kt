package com.kotlincode.myCoroutine

class Util {
    companion object {
        fun log(message: String) = println("[${Thread.currentThread().name}] $message")
    }
}
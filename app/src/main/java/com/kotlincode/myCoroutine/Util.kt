package com.kotlincode.myCoroutine

import kotlin.String

class Util {
    companion object {
        fun log(message: String) = println("[${Thread.currentThread().name}] $message")
    }
}
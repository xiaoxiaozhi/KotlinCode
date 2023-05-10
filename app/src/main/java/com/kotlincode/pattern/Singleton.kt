package com.kotlincode.pattern

import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

object Singleton {
    val executor: ExecutorService by lazy { Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()) }
}
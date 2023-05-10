package com.kotlincode.pattern

/**
 * 1.单例模式
 *   1.1 无参数单例模式：object对象将被实例化，它的 init 块将在第一次访问时以线程安全的方式惰性地执行,这是在 JVM 上实现单例的首选方法，因为它支持线程安全的惰性初始模式，而不必依赖于像复杂双重检查锁定模式那样的锁定算法
 *   1.2 有参数单例模式：依赖注入、或者类本身自定义的工厂方法,SingletonHolder给类提供了获取单例的工厂方法(单参数，多参数照葫芦画瓢)
 */
fun main() {
    //1. 单例模式
    Singleton.executor.execute { println("单例模式") }
//    Manager.getInstance().doFun()//带参数的
}
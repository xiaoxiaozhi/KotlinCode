package com.kotlincode.oop

/**
 * 接口
 * 1. 通java一样kotlin接口拥有 抽象方法，带有实现的方法，静态方
 * 2. 静态方法通过伴生对象实现
 * 3. 接口不允许拥有属性
 * 内部类
 * 1. 内部类可以直接访问外部类的成员包括private属性
 * 2. this 指向TVRemote 实例 this@TV1指向外部类的实例
 * 3. super@TV1 指向tv1 的基类
 * 创建匿名内部类
 */
fun main() {
    //1. 接口
    val tv = TV()
    val remote = TVRemote(tv)
    val anotherTv = TV()
    val combineRemote = Remote.combine(remote, TVRemote(anotherTv))
    combineRemote.up()
    println("${tv.volume}")
    //2. 内部类
    val tv1 = TV1()
    tv1.remote.up()
    println("${tv1.toString()} remote = ${tv1.remote.hashCode()} tv1 = ${tv1.remote.toString()}")
    tv1.remote.doubleUp()
    println("${tv1.toString()} remote = ${tv1.remote.hashCode()} tv1 = ${tv1.remote.toString()}")
    tv1.remote.down()
    println("${tv1.toString()} remote = ${tv1.remote.hashCode()} tv1 = ${tv1.remote.toString()}")
}

interface Remote {
    fun up()//1. 抽象方法
    fun down()
    fun doubleUp() {//2. 具体实现
        up()
        up()
    }

    companion object {
        //伴生对象实现静态方法
        fun combine(first: Remote, second: Remote) = object : Remote {
            override fun up() {
                first.up()
                second.up()
            }

            override fun down() {
                first.down()
                second.down()
            }

        }
    }
}

class TV {
    var volume = 0
}

class TVRemote(val tv: TV) : Remote {
    override fun up() {
        tv.volume++
    }

    override fun down() {
        tv.volume--
    }
}

//2. 嵌套类和内部类
class TV1 {
    private var volume = 0
    val remote: Remote
        //每次调用remote属性都会 重新生成一个
        get() = TVRemote()

    inner class TVRemote() : Remote {
        override fun up() {//2.1 内部类可以直接访问外部类的成员包括private属性
            volume++
        }

        override fun down() {
            volume--
        }

        override fun toString(): String {
            return "Remote:${this@TV1.hashCode()}" //2.2 this 指向TVRemote 实例 this@TV1指向外部类的实例
        }
    }

    override fun toString(): String {
        return "volume = $volume"
    }

    //3. 创建匿名内部类  与 inner内部类完全一致
    val remote1
        get() = object : Remote {
            override fun up() {

            }

            override fun down() {

            }
        }

}

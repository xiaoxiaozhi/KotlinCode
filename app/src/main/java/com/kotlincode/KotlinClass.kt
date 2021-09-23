package com.kotlincode

import java.lang.RuntimeException

/**
 * 面向对象
 * 1. 对象表达式
 * 2. 创建类
 */
fun main() {
    println("-----对象表达式-----")
    //1. 对象表达式
    drawCircle()
    println(Unit.numberOfProcess())
    println("-----创建类-----")
    //2. 创建类
    generateCar()
}

//1. 对象表达式创建匿名内部类
fun drawCircle() {
    val circle = object {
        val x = 10
        val y = 10
        val radius = 30
    }//对象表达式由 object关键字后跟{}组成，最基本的对象表达式只对将几个局部变量组合在一起有用
    println("circle x = ${circle.x} , circle y = ${circle.y} , circle radius = ${circle.radius}")
}

//1.1 对象表达式实现单一接口，创建匿名内部类
fun createRunnable(): Runnable {
    val runnable = object : Runnable {
        override fun run() {
            println("call Runnable")
        }
    }//object : 接口,接口{}
    return runnable
}

//1.2 单一抽象方法接口。 接口名{}
fun createRunnable1() = Runnable { println("单一") } //如果匿名内部类实现单一抽象方法的接口，可以直接实现不用再指定变量（接口名{}）

//1.3 对象表达式创建继承多个接口的匿名内部类
fun createRunnable2(): Runnable {
    return object : Runnable, AutoCloseable {
        override fun run() {
        }

        override fun close() {
        }
    }
}

//1.4 单例模式，对象表达式 创建单例 ，Unit 并不是 类二是一个实例，不能通过Unit创建其它实例
object Unit {
    const val radiusInKM = 59000
    fun numberOfProcess() = Runtime.getRuntime().availableProcessors()
}//在 object 名称 {} kotlin认为这是语句而不是表达式 ，使用对象表达式创建匿名内部类的实例

//1.5  顶级函数和单例，参见Test.kt
// 如果一组函数是高级的、通用的和广泛使用的，那么就把它们直接放在一个包中
// 如果一组函数需要依赖于状态，你可以将此状态与那些相关的函数一起放在一个单例中，
// 当我们关注行为、计算和动作时，函数和单例是很有意义的。但是如果我们想处理状态，那么类是一个更好的选择

//2.1 最小的类
class Car  //class 关键字后面跟着类名  class 类名

//2.2 定义一个类，拥有只读属性 val
class Car1(val yearOfMake: Int)// 为Car1类创建了一个构造函数，构造函数接收一个参数，类型为Int，同时kotlin还为该属性提供了get方法

//2.2.1 定义一个类，拥有读写属性 var
class Car2(var color: String)//

//2.3 创建实例
fun generateCar() {
    val car = Car1(2019)
//    car.yearOfMake = 2021
    val car2 = Car2("red")
    car2.color = "white"
    val car3 = Car3(2077, "")
//    car3.color = ""
//    Car5().color = "" //private 修饰color的set方法使其变成私有
    Car6(2021, "Red")
    Person("1", "2", true)
    Student("Dog", "Joker").fullName()
    receiveSSN(SSN("123456789"))
}//kotlin 没有new关键字创建类使用 类名()

//2.4 控制属性修改 getter 和 setter
class Car3(val yearOfMake: Int, var theColor: String) {
    val fullLevel = 100 // get和set是可选的
    var color = theColor
        set(value) {
            if (value.isBlank()) {
                throw RuntimeException("not empty")
            }
            field = value
        }
}

//2.5 field幕后字段
class Car4() {
    var color: String = ""
        set(value) {
            if (value.isBlank()) {
                throw RuntimeException("not empty")
            }
            field = value
        }
}//field 标识符只能用在属性的访问器内。kotlin会给属性自动提供幕后字段，但是当同时使用自定义的getter和setter，并且没有使用filed字段那么kotlin就不会创建幕后字段。

//2.6 访问修饰符 public protected private internal
class Car5(private val theColor: String) {
    var color: String = theColor //1. 小技巧，private 修饰 theColor 后外部访问不了，在这里只做传值用
        private set(value) {//2. private 修饰set 使其变成私有，只能在类内部访问,外部car5.color = "" 无法赋值
            if (value.isBlank()) {//3.internal修饰符允许同一模块中的任何代码访问属性或方法,限制跨模块访问
                throw RuntimeException("not empty")
            }
            field = value
        }
}

//2.7 初始化代码 init{}
class Car6(private val yearOfMake: Int, private var theColor: String) {
    var fullLevel = 100 //
    var color = theColor
        set(value) {
            if (value.isBlank()) {
                throw RuntimeException("not empty")
            }
            field = value
        }

    init {
        println("init1 $fullLevel")
    }//1.init作为主构造函数的一部分，一个类可以有0个或多个,执行顺序自上而下

    init {
        println("init2")
    }//2.init可以使用类中定义的属性，但是必须在它之上定义好

    init {
        fullLevel = if (yearOfMake > 2019) 99 else 100
        println(fullLevel)
    }//3.尽量避免1个以上的init
}

//2.8 二级构造函数
class Person(private val first: String, private val last: String) {
    //1.如果不编写住构造函数，kotlin将创建一个无参数的默认构造函数
    var fulltime = true;
    var location: String = ""

    constructor(first: String, last: String, fte: Boolean) : this(first, last) {
        fulltime = fte
    }//2. 二级构造函数用 constructor 声明

    constructor(first: String, last: String, loc: String) : this(first, last) {
        location = loc
    }//3. 二级构造函数的参数不能用val或var修饰，它们不定义任何属性。只有主构造函数和类中的声明可以定义属性。

    override fun toString(): String {
        return "$first $last $fulltime $location"
    }
}

//2.9 定义方法
class Student(private val first: String, private val last: String) {
    internal fun fullName() = println("$first $last")
    private fun yearOfMake(): Int = throw RuntimeException("not implement yet")
}

//2.10 内连类   一些内容例如ssn号想清晰的传递又不想浪费开销，编译阶段是个类，到运行阶段被拆开
inline class SSN(val id: String) //1. 内连类主构造函数只能有一个参数 2. 内连类可以有属性和方法也可以实现接口，内连类必须被
fun receiveSSN(ssn: SSN) {//kotlin检测到 方法接收到的参数是内联类，会直接释内连类的内容，直接把String传递给函数
    println(ssn.id)
}



package com.kotlincode

import java.lang.RuntimeException

/**
 * 面向对象
 * 1. 创建类
 */
fun main() {

    println("-----创建类-----")
    //1. 创建类
    generateCar()
}

//1.1 最小的类
class Car  //class 关键字后面跟着类名  class 类名

//1.2 定义一个类，拥有只读属性 val
class Car1(val yearOfMake: Int)// 为Car1类创建了一个构造函数，构造函数接收一个参数，类型为Int，同时kotlin还为该属性提供了get方法

//1.2.1 定义一个类，拥有读写属性 var
class Car2(var color: String)//

//1.2.2 定义一个类，只是参数
class Car21(price: Int) { //price没有被val和var修饰，只相当于构造函数的参数，没有在类中创建属性，所以方法中无法调用
    val i: Int = price
    fun sd() {
        //price  //方法中无法调用price ，因为它是构造函数的参数
    }
}

//1.3 创建实例
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

//1.4 控制属性修改 getter 和 setter
class Car3(val yearOfMake: Int, var theColor: String) {
    val fullLevel = 100 // val 属性只有get方法 ，var拥有get和set方法
    var color = theColor
        set(value) {
            if (value.isBlank()) {
                throw RuntimeException("not empty")
            }
            field = value
        }
}

//1.5 field幕后字段
class Car4() {
    var color: String = ""
        set(value) {
            if (value.isBlank()) {
                throw RuntimeException("not empty")
            }
            field = value
        }
}//field 标识符只能用在属性的访问器内。kotlin会给属性自动提供幕后字段，但是当同时使用自定义的getter和setter，并且没有使用filed字段那么kotlin就不会创建幕后字段。

//1.6 访问修饰符 public protected private internal
class Car5(private val theColor: String) {
    var color: String = theColor //1. 小技巧，private 修饰 theColor 后外部访问不了，在这里只做传值用
        private set(value) {//2. private 修饰set 使其变成私有，只能在类内部访问,外部car5.color = "" 无法赋值
            if (value.isBlank()) {//3.internal修饰符允许同一模块中的任何代码访问属性或方法,限制跨模块访问
                throw RuntimeException("not empty")
            }
            field = value
        }
}


//1.7 初始化代码 init{}
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

//1.8 二级构造函数
//class Person @Inject private  constructor(){}//当主构造方法没有任何注解或者可见性修饰符时，可以省略
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

    fun sa(ff: String): String {
        return ""
    }
}

//1.9 定义方法
class Student(private val first: String, private val last: String) {
    @JvmName(" fullName")//使用internal和JvmName实现kotlin独占 https://blog.csdn.net/qq_23626713/article/details/90698534
    internal fun fullName() =
        println("$first $last")//internal 修饰类的方法，表示这个类方法只适合当前module使用，如果其他module使用的话，会找不到这个internal方法或者报错

    private fun yearOfMake(): Int = throw RuntimeException("not implement yet")
}

//1.10 内连类   一些内容例如ssn号想清晰的传递又不想浪费开销，编译阶段是个类，到运行阶段被拆开
inline class SSN(val id: String) //1. 内联类必须含有唯一的一个属性在主构造函数中初始化

// 2. 内连类可以有属性和方法也可以实现接口，内连类必须是final类不能被扩展
// 3. 内联类不能含有 init 代码块
// 4. 内联类不能含有幕后字段
fun receiveSSN(ssn: SSN) {//kotlin检测到 方法接收到的参数是内联类，会直接释内连类的内容，直接把String传递给函数
    println(ssn.id)
}

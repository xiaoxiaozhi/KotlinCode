package com.kotlincode.oop

import kotlin.String
import kotlin.properties.Delegates.observable
import kotlin.properties.Delegates.vetoable
import kotlin.reflect.KProperty

/**
 * Kotlin Vocabulary | Kotlin 委托代理 - 谷歌开发者的文章 - 知乎 https://zhuanlan.zhihu.com/p/339765203
 * kotlin 的委托本质是 java23种设计模式的 代理模式，委托比继承更加灵活，建议使用委托
 * 委托模式已被证明是实现继承的一个很好的替代方案
 * 1. 类委托
 *    1.1 委托给类:要实现的接口 by 实例
 *    1.2 委托给一个参数：B(c:C):A by C {一些覆写自A的方法} B和C都实现了 接口A，参数委托就是 B的实例 调用的方法都是C的实现。
 *    1.3 处理方法冲突
 *    1.4 处理多个委托之间的方法冲突
 * 2. 属性委托
 *    委托会负责处理对应属性 get与 set 函数的调用
 *    2.1 惰性属性 lazy
 *        执行传递给的 lambda 的第一个调用并记住结果。后续调用只返回记住的结果
 *    2.2 可观察属性  Observable
 *        每次给属性赋值都会调用Delegates.observable()，该方法有三个个参数，初始值、旧值、新值
 *    2.3 条件委托 vetoable
 *        lambda表达式返回一个boolean类型，通过表达式才能被赋值
 *    2.4 委托给其它属性
 *        顶级属性、同一个类的其它属性、另一个类的属性
 *    2.5 在map中映射属性
 *    2.6 提供委托
 */
fun main() {

    //1.1 委托给类
    val manager = Manager()
    manager.work()
    //1.2 委托给一个参数
    Manager1(JavaProgrammer()).meeting()   //方法中调用委托实例
    Manager1(JavaProgrammer()).staff.work()//通过属性调用委托实例
    Manager1(JavaProgrammer()).work()      //调用委托方法 同 1
    //1.3 处理方法冲突:委托与类中方法出现冲突
    println("---处理方法冲突1---")
    Manager2(JavaProgrammer()).staff.tackVacation()//通过参数调用委托的方法
    Manager2(JavaProgrammer()).tackVacation()//调用重写的方法
    //1.4 处理方法冲突：多个委托之间方法冲突
    println("---处理方法冲突2---")
    val dol = Manager3(Employee(), LifeAssistant())
    dol.fileTimeSheet()
    //1. note 委托注意事项
    dol.work()
    dol.staff = Engineer()
    dol.work()// 打印的还是 Employee的 work()
    // 让我们再来仔细看看声明class Manager2(val staff: Worker) : Worker by staff 最右边的委托是参数， 不是属性。
    // 声明实际上是接受一个名为staff的参数并将其分配给一个名为staff的成员，
    // 就像这样：this.staff=staff。因此，对给定对象有两个引用：一个是在类内部作为幕后字段保存的，另一个是为委托保存的。
    // 但是，当我们将属性变为CSharpProgrammer的一个实例时，只修改了字段，而没有修改对委托的引用。

    //2.1 惰性属性 lazy
    println("---惰性属性lazy---")
    val showTemperature: Boolean = true;//
    val temperature by lazy { getTemperature("SanYa") }//第二次调用temperature 将不再打印 fetch temperature from website
    if (showTemperature && temperature > 30) {
        println("warm")
    } else {
        println("nothing")
    }
    println(temperature)// 一旦对lambda中的表达式求值，委托将记住结果，以后对该值的请求将接收保存的值。不会重新计算lambda表达式。
    //2.2 可观察属性  Observable
    println("---可观察属性Observable---")
    var count by observable(0) { property, oldValue, newValue -> println("property:${property.name} oldValue:$oldValue newValue:$newValue") }
//    count++  //报错  未知原因
    count = 2

    //2.3 vetoable委托
    var num by vetoable(1) { _, oldValue, newValue -> oldValue < newValue }
    num = 0
    println("num = $num")

    //2.4 委托给其它属性
    //代码最下面

    //2.5 在map中映射属性
    println("---在map中映射属性---")
    val data = listOf(
        mutableMapOf<String, Any>(
            "title" to "Using delegation",
            "likes" to 2,
            "comment" to "keep it simple stupid"
        ),
        mutableMapOf<String, Any>(
            "title" to "Using Inheritance",
            "likes" to 2,
            "comment" to "keep it simple stupid"
        )
    )
    val forPost1 = PostComment(data[0])
    val forPost2 = PostComment(data[1])
    forPost1.likes++
    forPost1.comment = "123"
    println(forPost1)
    println(forPost2)

    //2.6 提供委托
    println("---提供委托---")
    var comment: String by PoliteString("something")//属性可以将其 getter 和 setter 委托给另一个属性，
    println(comment)
    comment = "stupid"
    println(comment)
    //2.6 为只读属性提供委托
    val comment1: String by PoliteString2("read_only")
    println(comment1)

}

interface Worker {
    fun work()
    fun tackVacation()
}

class JavaProgrammer : Worker {
    override fun work() {
        println("write java ")
    }

    override fun tackVacation() {
        println("...code at the beach...")
    }
}

class CSharpProgrammer : Worker {
    override fun work() {
        println("write C# ")
    }

    override fun tackVacation() {
        println("...branch at the ranch...")
    }
}

//1. 委托给类    要实现的接口 by 实例
class Manager : Worker by JavaProgrammer()//Manager不用自己去实现Worker接口，委托给JavaProgrammer实现。无法访问委托实例，委托给参数很好的解决了这个问题

//2. 委托给参数
class Manager1(val staff: Worker) : Worker by staff {
    fun meeting() = println("organizing meeting with ${staff.javaClass.simpleName}")
    override fun tackVacation() {
        println("do this")
    }
}

//3.1 方法冲突:委托与类中方法冲突
class Manager2(val staff: Worker) : Worker by staff {
    fun meeting() = println("organizing meeting with ${staff.javaClass.simpleName}")
    override fun tackVacation() {
        println("do this")
    }
}

//3.2 方法冲突：多个委托之间冲突
interface Worker1 {
    fun work()
    fun tackVacation()
    fun fileTimeSheet() = println("why? Really?")
}

interface Assistant {
    fun doChores()
    fun fileTimeSheet() = println("no escape from that")
}

class Employee : Worker1 {
    override fun work() {
        println("study")
    }

    override fun tackVacation() {
        println("every day")
    }
}

class Engineer : Worker1 {
    override fun work() {
        println("设计")
    }

    override fun tackVacation() {
        println("团建")
    }

}

class LifeAssistant : Assistant {
    override fun doChores() {
        println("睡觉")
    }

}

class Manager3(var staff: Worker1, val assistant: Assistant) : Worker1 by staff,
    Assistant by assistant {
    override fun fileTimeSheet() {
        println("选择用assistant的fileTimeSh方法")
        assistant.fileTimeSheet()
    }
}//当出现委托之间的冲突时，kotlin会 让类重写该方法

//2.6 提供委托
class PoliteString(var content: String) {
    operator fun getValue(thisRef: Any?, property: KProperty<*>) =
        content.replace("stupid", "s****")
    //thisRef 包含该属性的对象

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: String) {
        content = value
    }
}//使用注释operator进行标记，因为它们代表用于get和set的赋值运算符=

//2.6 只读属性提供委托
class PoliteString2(var content: String) {
    operator fun getValue(thisRef: Any?, property: KProperty<*>) =
        content.replace("stupid", "s****")

}

//4.2 委托属性
class PoliteString1(var dataSource: MutableMap<String, Any>) {
    operator fun getValue(thisRef: Any?, property: KProperty<*>) =
        (dataSource[property.name] as? String)?.replace("stupid", "s****") ?: ""

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: String) {
        println("包含该属性的对象是${thisRef?.javaClass?.simpleName}")
        dataSource[property.name] = value
    }
}

class PostComment(dataSource: MutableMap<String, Any>) {
    val title: String by dataSource
    var likes: Int by dataSource
    var comment: String by PoliteString1(dataSource)
    override fun toString(): String {
        return "Title $title Like $likes Comment $comment"
    }
}

//5. lazy 委托
fun getTemperature(city: String): Double {
    println("fetch temperature from website $city")
    return 30.3
}

//2.4
var topLevelInt: Int = 0

class ClassWithDelegate(val anotherClassInt: Int)

class MyClass(var memberInt: Int, val anotherClassInstance: ClassWithDelegate) {
    var delegatedToMember: Int by this::memberInt
    var delegatedToTopLevel: Int by ::topLevelInt

    val delegatedToAnotherClass: Int by anotherClassInstance::anotherClassInt
}

var MyClass.extDelegated: Int by ::topLevelInt

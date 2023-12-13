package com.kotlincode.jvm

import kotlin.String

/**
 * 1. 加载器
 *    类加载过程 加载器加载--->连接--->初始化(变量赋值和执行静态代码块)
 *    初始化阶段就是执行类构造器方法<clinit>()的过程此方法不需定义，是javac编译器自动收集类中的所有类变量的赋值动作和静态代码块中的语句合并而来
 * 2. 双亲委派机制
 *    Java虚拟机对class文件采用的是按需加载的方式，也就是说当需要使用该类时才会将它的class文件加载到内存生成class对象。
 *    而且加载某个类的class文件时，Java虚拟机采用的是双亲委派模式，即把请求交由父类处理它是一种任务委派模式。
 *    如果一个类加载器收到了类加载请求，它并不会自己先去加载，而是把这个请求委托给父类的加载器去执行;
 *    如果父类加载器还存在其父类加载器，则进一步向上委托，依次递归,o请求最终将到达顶层的启动类加载器
 *    如果父类加载器可以完成类加载任务，就成功返回，倘若父类加载器无法完成此加载任务，子加载器才会尝试自已去加载，这就是双亲委派模式。
 *    每个加载器都有要加载的路径，当你要加载的类不属于路径就会把加载请求丢给其它加载器
 *    在JVM中表示两个class对象是否为同一个类存在两个必要条件:类的完整类名必须一致，包括包名。Y
 *    加载这个类的classLoader(指classLoader实例对象)必须相同
 *    换句话说，在JVM中，即使这两个类对象(class对象)来源同一个class文件，被同一个虚拟机所加载，但只要加载它们的classLoader实例对象不同，那么这两个类对象也是不相等的。
 * 3.
 *
 */
fun main() {
    println("系统类加载器---${String::class.java.classLoader}")//由于是bootstart 所以返回null
    //线程上下文加载器，也就是当前类的加载器
    println("当前类加载器---${Thread.currentThread().contextClassLoader}")
    //2.
//  kotlin.String()
    val str = String()//比如你也建立一个kotlin包,下面建立一个String， 此时你加载前先要让父加载器加载，最终加载到核心库，而不是你创建的
    //这个点进去怎么是自己创建的与宋老师讲的不一样？？？java/kotlin/String  并且现在项目所有String都自动指向这个，暂时删掉java/kotlin/String否则项目运行不起来
}
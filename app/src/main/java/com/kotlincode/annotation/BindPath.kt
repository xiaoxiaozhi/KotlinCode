package com.kotlincode.annotation

import java.lang.annotation.ElementType
import java.lang.annotation.RetentionPolicy

/**
 * [kotlin 注解与注解处理器](https://juejin.cn/post/7071631726694367268)
 * 详情查看 jetpack项目的注解
 */
@Target(AnnotationTarget.CLASS)   //声明注解的作用域  放在什么上面
@Retention(AnnotationRetention.BINARY)   //源码期  <  编
annotation class BindPath(val value: String)

package com.kotlincode.annotation

import kotlin.String

/**
 * [kotlin的注解处理器和java不一样，霍丙乾老师的课程](https://www.bilibili.com/video/BV1MM411w7fR/?vd_source=9cc1c08c51cf20bda524430137dc77bb)
 */
@Target(AnnotationTarget.CLASS)   //声明注解的作用域  放在什么上面
@Retention(AnnotationRetention.BINARY)   //源码期  <  编
annotation class BindPath(val value: String)

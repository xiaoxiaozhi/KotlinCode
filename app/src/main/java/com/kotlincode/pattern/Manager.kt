package com.kotlincode.pattern

import android.content.Context

class Manager private constructor(context: Context) {
    companion object : SingletonHolder<Manager, Context>(::Manager)
}
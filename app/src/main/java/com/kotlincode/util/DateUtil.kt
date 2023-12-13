package com.kotlincode.util

import kotlin.String
import java.util.*

class DateUtil(val number: Int, val tense: Tense) {
    enum class Tense {
        ago, from_now
    }

    override fun toString(): String {
        val today = Calendar.getInstance()
        when (tense) {
            Tense.ago -> today.add(Calendar.DAY_OF_MONTH, -number)
            Tense.from_now -> today.add(Calendar.DAY_OF_MONTH, number)
        }
        return today.time.toString()
    }

    fun toString1(): String {
        val today = Calendar.getInstance()
        return when (tense) {
            Tense.ago -> today.apply { add(Calendar.DAY_OF_MONTH, -number) }
            else -> today.apply { add(Calendar.DAY_OF_MONTH, number) }
        }.time.toString()

    }
}
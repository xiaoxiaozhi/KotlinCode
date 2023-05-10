package com.kotlincode

fun main(){
   repeat(5){
       val byteArray4 = byteArrayOf(0, 0, 0, 1)
       val byteArray3 = byteArrayOf(0, 0, 1)
       println("byteArray4----${byteArray4.hashCode()}")
       println("byteArray3----${byteArray3.hashCode()}")
   }
}
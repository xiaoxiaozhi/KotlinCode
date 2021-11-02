package com.kotlincode.asynccode

import com.beust.klaxon.Json  //json解析库 ，纯kotlin代码
import com.beust.klaxon.Klaxon
import java.net.URL
import kotlin.system.measureTimeMillis

import kotlinx.coroutines.*

/**
 * 异步编程
 */
fun main() {
    val time = measureTimeMillis {
        val airportCode = listOf("LAX", "SFO", "PDX", "SEA")
        val format = "%-10s%-20s%-10s"
        //1. 普通调用方式，阻塞 网络请求完成一个请求一个
//        val airportData: List<Airport> = airportCode.mapNotNull {
//            Airport.getAirportData(it)
//        }
        //2. 异步调用
//        val airportData: List<Deferred<Airport>> = airportCode.map {
//           runBlocking {
//               async {
//                   Airport.getAirportData(it)
//               }
//           }
//        }
//        val airportData = airportCode.map {
//
//                async {
//                    Airport.getAirportData(it)
//                }
//
//        }
//        println(airportData.javaClass.name)
//        airportData.forEach {
//            println(
//                String.format(
//                    format,
//                    it.code,
//                    it.weather.temperature[0],
//                    it.daley
//                )
//            )
//        }
    }

}

class Weather(@Json(name = "Temp") val temperature: Array<String>)
class Airport(
    @Json(name = "IATA") val code: String,
    @Json(name = "Name") val name: String,
    @Json(name = "Delay") val daley: Boolean,
    @Json(name = "Weather") val weather: Weather
) {
    companion object {
        const val url = "https://soa.smext.faa.gov/asws/api/airport/status/"
        fun getAirportData(code: String): Airport? =
            Klaxon().parse<Airport>(URL(url + "$code").readText())
    }

}
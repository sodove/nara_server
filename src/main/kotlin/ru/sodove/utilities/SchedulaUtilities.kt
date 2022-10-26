package ru.sodove.utilities

import java.text.SimpleDateFormat
import java.util.*

class SchedulaUtilities {
    companion object {
        fun printer(input: String){
            val sdf = SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS")
            val currentDate = sdf.format(Date())
            println("$currentDate [Schedula-Printer] INFO  $input")
        }
    }
}
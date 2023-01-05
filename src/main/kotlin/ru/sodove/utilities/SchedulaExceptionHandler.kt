package ru.sodove.utilities

import io.ktor.server.application.*
import io.ktor.server.response.*

class RoutingException(message: String) : Exception(message)

suspend fun schedulaExceptionHandler(e: Exception, call: ApplicationCall) {
    when (e) {
        is RoutingException -> call.respondText(e.message!!)
        is NumberFormatException -> call.respondText("id must be a number")
        is NullPointerException -> call.respondText("NullPointerException")
        else -> call.respondText("Error: ${e.message}")
    }
}
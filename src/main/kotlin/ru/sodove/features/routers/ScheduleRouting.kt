package ru.sodove.features.routers

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import ru.sodove.database.dataclasses.schedule_json
import ru.sodove.database.dto.ScheduleDTO
import ru.sodove.features.controllers.ScheduleController
import ru.sodove.utilities.RoutingException
import ru.sodove.utilities.SchedulaUtilities.Companion.printer
import ru.sodove.utilities.schedulaExceptionHandler
import java.time.Instant

fun Application.configureScheduleRouting() {
    routing {
        get("/api/v1/schedule/") {
            var callString = "GET /api/v1/schedule/"
            val queryParams = call.request.queryParameters
            val schedulaStyle = queryParams["schedulaStyle"]?.toBoolean() ?: false
            try {
                if (queryParams["type"] == null && queryParams["id"] != null)
                    throw RoutingException("Wrong request: type is null")

                if (queryParams["type"] != null) {
                    val type = queryParams["type"]!!
                    if (queryParams["id"] != null) {
                        val id = queryParams["id"]!!.toInt()
                        if (queryParams["ical"] == null) {
                            val schedule = ScheduleController().getScheduleByTypeAndId(id = id, type = type, schedulaStyle = schedulaStyle)
                            call.respond(schedule)
                            callString += "?type=$type&id=$id"
                        } else {
                            val schedule = ScheduleController().getScheduleByTypeAndId(id = id, type = type, schedulaStyle = false)
                            val ical = ScheduleController().getIcal(schedule as ScheduleDTO)
                            call.respondText(ical, contentType = io.ktor.http.ContentType.Text.Plain)
                            callString += "?type=$type&id=$id&ical=true"
                        }
                    } else {
                        val schedules = ScheduleController().getSchedulesByType(type, schedulaStyle = schedulaStyle)
                        call.respond(schedules)
                        callString += "?type=$type"
                    }
                } else {
                    val schedules = ScheduleController().getSchedules(schedulaStyle = schedulaStyle)
                    call.respond(schedules)
                }
            }
            catch (e: Exception) {
                if (e.message?.contains("empty") == true)
                    if (queryParams["type"] != null && queryParams["id"] != null)
                        queryParams["id"]?.let { it1 -> ScheduleDTO(id_ = it1.toInt(), type_ = queryParams["type"]!!, last_update_ = Instant.EPOCH, data_ = schedule_json()) }
                            ?.let { it2 -> call.respond(it2) }
                schedulaExceptionHandler(e, call)
            }
            printer(callString)
        }
    }
}
package ru.sodove.features.routers

import io.ktor.http.*
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
    val scheduleController = ScheduleController()
    routing {
        get("/api/v1/schedule/") {
            var callString = "GET /api/v1/schedule/?"
            val queryParams = call.request.queryParameters
            callString += queryParams.formUrlEncode()

            val schedulaStyle = queryParams["schedulaStyle"]?.toBoolean() ?: false
            try {
                if (queryParams["type"] == null && queryParams["id"] != null)
                    throw RoutingException("Wrong request: type is null")

                if (queryParams["type"] != null) {
                    val type = queryParams["type"]!!
                    if (queryParams["id"] != null) {
                        val id = queryParams["id"]!!.toInt()
                        if (queryParams["ical"] == null) {
                            val dateStart = queryParams["dateStart"]
                            val dateEnd = queryParams["dateEnd"]
                            if (dateStart != null && dateEnd != null) {
                                val schedule = scheduleController.getScheduleByTypeAndIdAndDate(id, type,
                                    schedulaStyle, dateStart, dateEnd)
                                call.respond(schedule)
                            } else {
                                val schedule = scheduleController.getScheduleByTypeAndId(
                                    id = id,
                                    type = type,
                                    schedulaStyle = schedulaStyle,
                                    allowCache = queryParams["allowCache"] != null
                                )
                                call.respond(schedule)
                            }
                        } else {
                            val schedule = scheduleController.getScheduleByTypeAndId(
                                id = id,
                                type = type,
                                schedulaStyle = false,
                                allowCache = queryParams["allowCache"] != null
                            )
                            val ical = scheduleController.getIcal(schedule as ScheduleDTO)
                            call.respondText(ical, contentType = ContentType.Text.Plain)
                        }
                    } else {
                        val schedules = scheduleController.getSchedulesByType(type, schedulaStyle = schedulaStyle)
                        call.respond(schedules)
                    }
                } else {
                    val schedules = scheduleController.getSchedules(
                        schedulaStyle = schedulaStyle,
                        allowCache = queryParams["allowCache"] != null
                    )
                    call.respond(schedules)
                }
            } catch (e: Exception) {
                if (e.message?.contains("empty") == true)
                    if (queryParams["type"] != null && queryParams["id"] != null) {
                        callString += " (Sending empty schedule because not found in cache and db)"
                    }
                queryParams["id"]?.let { it1 ->
                    ScheduleDTO(
                        id_ = it1.toInt(),
                        type_ = queryParams["type"]!!,
                        last_update_ = Instant.EPOCH,
                        data_ = schedule_json(),
                        start_date_ = "01.01.1970"
                    )
                }
                    ?.let {
                            it2 -> call.respond(it2)
                            printer(callString)
                        }
                schedulaExceptionHandler(e, call)
            }
        }
    }
}
package ru.sodove.features.routers

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import ru.sodove.features.controllers.AudsController
import ru.sodove.features.controllers.GroupsController
import ru.sodove.features.controllers.ListsController
import ru.sodove.features.controllers.PrepsController
import ru.sodove.utilities.schedulaExceptionHandler

fun Application.configureListsRouting() {
    routing {
        get("/api/v1/groups/") {
            try {
                if (call.request.queryParameters["id"] != null) {
                    val id = call.request.queryParameters["id"]!!.toInt()
                    call.respond(GroupsController().getFiltered(id))
                } else if (call.request.queryParameters["data"] != null) {
                    val name = call.request.queryParameters["data"]!!
                    call.respond(GroupsController().getFiltered(name))
                } else {
                    call.respond(GroupsController().getAll())
                }
            }
            catch (e: Exception) {
                schedulaExceptionHandler(e, call)
            }
        }
    }

    routing {
        get ( "/api/v1/preps" ) {
            try {
                if (call.request.queryParameters["id"] != null) {
                    val id = call.request.queryParameters["id"]!!.toInt()
                    call.respond(PrepsController().getFiltered(id))
                } else if (call.request.queryParameters["data"] != null) {
                    val name = call.request.queryParameters["data"]!!
                    call.respond(PrepsController().getFiltered(name))
                } else {
                    call.respond(PrepsController().getAll())
                }
            }
            catch (e: Exception) {
                schedulaExceptionHandler(e, call)
            }
        }
    }

    routing {
        get("/api/v1/auds") {
            try {
                if (call.request.queryParameters["id"] != null) {
                    val id = call.request.queryParameters["id"]!!.toInt()
                    call.respond(AudsController().getFiltered(id))
                } else if (call.request.queryParameters["data"] != null) {
                    val name = call.request.queryParameters["data"]!!
                    call.respond(AudsController().getFiltered(name))
                } else {
                    call.respond(AudsController().getAll())
                }
            }
            catch (e: Exception) {
                schedulaExceptionHandler(e, call)
            }
        }
    }

    routing {
        get ( "/api/v1/lists" ) {
            try {
                call.respond(ListsController().getAll())
            }
            catch (e: Exception) {
                schedulaExceptionHandler(e, call)
            }
        }
    }
}
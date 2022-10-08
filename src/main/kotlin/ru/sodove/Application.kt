package ru.sodove

import io.ktor.server.cio.*
import io.ktor.server.engine.*
import org.jetbrains.exposed.sql.Database
import ru.sodove.features.routers.configureListsRouting
import ru.sodove.features.routers.configureScheduleRouting
import ru.sodove.plugins.configureHTTP
import ru.sodove.plugins.configureRouting
import ru.sodove.plugins.configureSerialization
import ru.sodove.plugins.startPeriodicTask
import ru.sodove.utilities.prepareInMemoryCache
import ru.sodove.utilities.updateScheduleLists
import ru.sodove.utilities.updateSchedulesJSON

fun main() {
    Database.connect("jdbc:postgresql://localhost:5432/schedula", driver = "org.postgresql.Driver",
        user = "postgres", password = "password")

    embeddedServer(CIO, port = 8080, host = "0.0.0.0") {
        configureHTTP()
        configureSerialization()
        configureRouting()
        configureListsRouting()
        configureScheduleRouting()
        prepareInMemoryCache()
        startPeriodicTask(func = ::updateScheduleLists, delay = 1 * 60 * 60000, delay_start = false)
        startPeriodicTask(func = ::updateSchedulesJSON, delay = 10 * 60000, delay_start = false)
    }.start(wait = true)
}



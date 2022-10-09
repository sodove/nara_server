package ru.sodove

import io.github.cdimascio.dotenv.dotenv
import io.ktor.server.engine.*
import io.ktor.server.netty.*
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
    val dotenv = dotenv()

    val db_username = dotenv.get("DB_USERNAME")
    val db_password = dotenv.get("DB_PASSWORD")
    val db_host = dotenv.get("DB_HOST")
    val db_name = dotenv.get("DB_NAME")
    val db_port = dotenv.get("DB_PORT")
    val server_port = dotenv.get("SERVER_PORT")
    val server_host = dotenv.get("SERVER_HOST")

    Database.connect("jdbc:postgresql://$db_host:$db_port/$db_name", driver = "org.postgresql.Driver",
        user = db_username, password = db_password)

    embeddedServer(Netty, port = server_port.toInt(), host = server_host, watchPaths = listOf("classes")) {
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



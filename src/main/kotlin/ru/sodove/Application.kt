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
import ru.sodove.utilities.FirebaseTools
import ru.sodove.utilities.prepareInMemoryCache
import ru.sodove.utilities.updateScheduleLists
import ru.sodove.utilities.updateSchedulesJSON

fun main() {
    val dotenv = dotenv()

    val dbUsername = dotenv.get("DB_USERNAME")
    val dbPassword = dotenv.get("DB_PASSWORD")
    val dbHost = dotenv.get("DB_HOST")
    val dbName = dotenv.get("DB_NAME")
    val dbPort = dotenv.get("DB_PORT")
    val serverPort = dotenv.get("SERVER_PORT")
    val serverHost = dotenv.get("SERVER_HOST")

    FirebaseTools.initializeFirebase()
//    FirebaseTools.createFirebaseRequest(4405, "v_gru")
//    FirebaseTools.createFirebaseRequest(message = "Hello World", topic = "updates")

    Database.connect(
        "jdbc:postgresql://$dbHost:$dbPort/$dbName", driver = "org.postgresql.Driver",
        user = dbUsername, password = dbPassword
    )

    embeddedServer(Netty, port = serverPort.toInt(), host = serverHost, watchPaths = listOf("classes")) {
        configureHTTP()
        configureSerialization()
        configureRouting()
        configureListsRouting()
        configureScheduleRouting()
        prepareInMemoryCache()
        startPeriodicTask(func = ::updateScheduleLists, delay = 2 * 60 * 60000, delay_start = false)
        startPeriodicTask(func = ::updateSchedulesJSON, delay = 30 * 60000, delay_start = false)
    }.start(wait = true)


}



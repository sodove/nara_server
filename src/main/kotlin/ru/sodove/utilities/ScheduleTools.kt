package ru.sodove.utilities

import com.google.gson.Gson
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.server.application.*
import kotlinx.coroutines.delay
import org.jsoup.Jsoup
import ru.sodove.cache.InMemoryCache
import ru.sodove.database.dataclasses.schedule_json
import ru.sodove.database.dto.ScheduleDTO
import ru.sodove.features.controllers.*
import java.time.Instant
import kotlin.random.Random

suspend fun Application.updateScheduleLists() {
    log.info("Removing old schedule lists")
    ListsController().removeOld()
    log.info("Removing old schedule lists... Done")

    log.info("Downloading schedule lists...")
    InMemoryCache.isListsUpdating = true
    val rsvpuURL = "https://rsvpu.ru/mobile/"
    val rsvpuZaochURL = "https://rsvpu.ru/mobile/?form=zaoch"

    val client = HttpClient()
    val response: HttpResponse = client.get(rsvpuURL)
    val response2: HttpResponse = client.get(rsvpuZaochURL)

    val body = response.body<String>()
    val body2 = response2.body<String>()
    val document = Jsoup.parse(body)
    val document2 = Jsoup.parse(body2)
    val selections = document.getElementsByClass("divSelection")
    val selections2 = document2.getElementsByClass("divSelection")
    val grus = LinkedHashMap<Int, String>()
    val preps = LinkedHashMap<Int, String>()
    val auds = LinkedHashMap<Int, String>()

    selections.forEach { selection ->
        selection.children().forEach { child ->
            when {
                child.attr("name") == "gr" -> if (child.attr("data") != "") {
                    grus[child.attr("data").toInt()] = child.text()
                }
                child.attr("name") == "prep" -> if (child.attr("data") != "") {
                    preps[child.attr("data").toInt()] = child.text()
                }
                child.attr("name") == "aud" -> if (child.attr("data") != "") {
                    auds[child.attr("data").toInt()] = child.text()
                }

                else -> log.warn("Unknown selection type: '${child.attr("name")}', ignoring...")
            }
        }
    }

    selections2.forEach { selection ->
        selection.children().forEach { child ->
            when {
                child.attr("name") == "gr" -> if (child.attr("data") != "") {
                    grus[child.attr("data").toInt()] = child.text()
                }
                child.attr("name") == "prep" -> {}
                child.attr("name") == "aud" -> {}

                else -> log.warn("Unknown schedule type: '${child.attr("name")}', ignoring...")
            }
        }
    }

    log.info("Downloading schedule lists... Done")
    log.info("Schedule lists size (grus): ${grus.size}")
    log.info("Schedule lists size (preps): ${preps.size}")
    log.info("Schedule lists size (auds): ${auds.size}")
    prepareInMemoryCache()
    //if grus is not empty, then save to database
    if (grus.isNotEmpty() && preps.isNotEmpty() && auds.isNotEmpty()) {
        if (grus != InMemoryCache.grus || preps != InMemoryCache.preps || auds != InMemoryCache.auds) {
            log.info("Schedule lists changed, updating database...")
            ListsController().updateLists(grus, preps, auds)
            log.info("Schedule lists changed, updating database... Done")
            prepareInMemoryCache()
        }
        else {
            log.info("Schedule lists are up to date")
        }
    }
    client.close()

    InMemoryCache.isListsUpdating = false
}

val scheduleController = ScheduleController()
suspend fun Application.updateSchedulesJSON() {

    while (InMemoryCache.auds.isEmpty() || InMemoryCache.grus.isEmpty() || InMemoryCache.preps.isEmpty() || InMemoryCache.isListsUpdating) {
        log.info("Waiting for lists to be downloaded...")
        delay(1000 * 10)
        prepareInMemoryCache()
    }

    val apiLink = "https://rsvpu.ru/contents/api/rasp.php?"
    val client = HttpClient()
    log.info("Downloading schedules...")

    val grus = InMemoryCache.grus
    val preps = InMemoryCache.preps
    val auds = InMemoryCache.auds

    log.info("Removing old schedules")
    scheduleController.removeOld()
    log.info("Removing old schedules... Done")

    for (v_gru in grus) {
        updateScheduleInDB(apiLink, v_gru.key, "v_gru", client)
        delay(Random.nextLong(2500, 6000))
    }

    for (v_prep in preps) {
        updateScheduleInDB(apiLink, v_prep.key, "v_prep", client)
        delay(Random.nextLong(2500, 6000))
    }

    for (v_aud in auds) {
        updateScheduleInDB(apiLink, v_aud.key, "v_aud", client)
        delay(Random.nextLong(2500, 6000))
    }
    log.info("Downloading schedules... Done")
}

suspend fun Application.updateScheduleInDB(link: String, id: Int, type: String, client: HttpClient) {
    try {
        val response = client.get("$link$type=$id")
        val scheduleJson = response.body<String>()

        val gson = Gson()
        val schedule = gson.fromJson(scheduleJson, schedule_json::class.java)

        if (schedule.isNotEmpty()){
            val scheduleDTO = ScheduleDTO(id_ = id, type_ = type, data_ = schedule, last_update_ = Instant.now())
            scheduleController.update(scheduleDTO)
        }
    }
    catch (e: Exception) {
        log.error("Error while saving schedule to database: ${e.message}")
    }
}

fun Application.prepareInMemoryCache() {
    log.info("Preparing in-memory cache...")
    InMemoryCache.grus = GroupsController().getAllMap()
    InMemoryCache.preps = PrepsController().getAllMap()
    InMemoryCache.auds = AudsController().getAllMap()
    log.info("Preparing in-memory cache... Done")
}
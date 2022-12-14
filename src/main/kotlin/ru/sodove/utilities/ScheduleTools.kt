package ru.sodove.utilities

import com.google.gson.Gson
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.server.application.*
import io.ktor.utils.io.*
import kotlinx.coroutines.delay
import org.jsoup.Jsoup
import ru.sodove.cache.InMemoryCache
import ru.sodove.database.dataclasses.schedule_json
import ru.sodove.database.dto.SchedulaStyleDTO
import ru.sodove.database.dto.ScheduleDTO
import ru.sodove.features.controllers.*
import ru.sodove.utilities.SchedulaUtilities.Companion.printer
import java.time.Instant

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

    if (grus.isNotEmpty() && preps.isNotEmpty() && auds.isNotEmpty()) {
        val downloadedSortedGrus = grus.toList().sortedBy { (key, _) -> key }.toMap()
        val downloadedSortedPreps = preps.toList().sortedBy { (key, _) -> key }.toMap()
        val downloadedSortedAuds = auds.toList().sortedBy { (key, _) -> key }.toMap()
        val inMemorySortedGrus = InMemoryCache.grus.toList().sortedBy { (key, _) -> key }.toMap()
        val inMemorySortedPreps = InMemoryCache.preps.toList().sortedBy { (key, _) -> key }.toMap()
        val inMemorySortedAuds = InMemoryCache.auds.toList().sortedBy { (key, _) -> key }.toMap()

        // show differences between downloaded and in-memory lists
        if (downloadedSortedGrus != inMemorySortedGrus) {
            log.info("Grus lists are different")
            log.info("Downloaded grus: $downloadedSortedGrus")
            log.info("In-memory grus: $inMemorySortedGrus")
        }
        if (downloadedSortedPreps != inMemorySortedPreps) {
            log.info("Preps lists are different")
            log.info("Downloaded preps: $downloadedSortedPreps")
            log.info("In-memory preps: $inMemorySortedPreps")
        }
        if (downloadedSortedAuds != inMemorySortedAuds) {
            log.info("Auds lists are different")
            log.info("Downloaded auds: $downloadedSortedAuds")
            log.info("In-memory auds: $inMemorySortedAuds")
        }


        log.info("Updating database...")
        ListsController().updateLists(grus, preps, auds)
        log.info("Updating database... Done")
        prepareInMemoryCache()
    }
    client.close()

    InMemoryCache.isListsUpdating = false
}

val scheduleController = ScheduleController()
var startDateFormatted = ""
var endDay = ""

suspend fun Application.updateSchedulesJSON() {

    while (InMemoryCache.auds.isEmpty() || InMemoryCache.grus.isEmpty() || InMemoryCache.preps.isEmpty() || InMemoryCache.isListsUpdating) {
        log.info("Waiting for lists to be downloaded...")
        delay(1000 * 10)
        prepareInMemoryCache()
    }

    val apiLink = "http://forum.rsvpu.ru/contents/api/rasp.php?"
    val client = HttpClient()
    log.info("Downloading schedules...")

//    val parity = CalendarUtils().getParity()

    // if parity get start of current week, else get start of previous week
    val startDate = //if (!parity) {
        CalendarUtils().firstDayOfWeek()
//    } else {
//        CalendarUtils().firstDayOfPreviousWeek()
//    }


    startDateFormatted = startDate.format(CalendarUtils.formatter)
    endDay = startDate.plusDays(27).format(CalendarUtils.formatter)
    printer("Using $startDateFormatted as start day")
    printer("Using $endDay as end day")

    val grus = InMemoryCache.grus
    val preps = InMemoryCache.preps
    val auds = InMemoryCache.auds

    log.info("Removing old schedules")
    scheduleController.removeOld()
    log.info("Removing old schedules... Done")

    for (v_gru in grus) {
        updateScheduleInDB(apiLink, v_gru.key, "v_gru", client)
        //delay(Random.nextLong(2500, 6000))
    }

    for (v_prep in preps) {
        updateScheduleInDB(apiLink, v_prep.key, "v_prep", client)
        //delay(Random.nextLong(2500, 6000))
    }

    for (v_aud in auds) {
        updateScheduleInDB(apiLink, v_aud.key, "v_aud", client)
        //delay(Random.nextLong(2500, 6000))
    }
    log.info("Downloading schedules... Done")

    prepareInMemoryCache()
}


suspend fun Application.updateScheduleInDB(link: String, id: Int, type: String, client: HttpClient) {
    try {
        printer("Downloading schedule for $type $id, start date: $startDateFormatted, end date: $endDay")
        val response = client.get("$link$type=$id&v_date_start=$startDateFormatted&v_date_end=$endDay")
        val scheduleJson = response.body<String>()

        val gson = Gson()
        val schedule = gson.fromJson(scheduleJson, schedule_json::class.java)

        // rsvpu I hate you, why are you printing extra spaces and new lines in json?
        schedule.forEach { data ->
            data.timetable = data.timetable?.trim()
            data.content.aud = data.content.aud?.trim()
            data.content.note = data.content.note?.trim()
            data.content.disciplina = data.content.disciplina?.trim()
            data.content.lecturer = data.content.lecturer?.trim()
            data.content.note = data.content.note?.trim()
            data.content.subgroupname = data.content.subgroupname?.trim()
            data.content.typeDisciplina = data.content.typeDisciplina?.trim()
        }

        if (schedule.isNotEmpty()) {
            val scheduleDTO = ScheduleDTO(
                id_ = id,
                type_ = type,
                data_ = schedule,
                last_update_ = Instant.now(),
                start_date_ = startDateFormatted
            )
            scheduleController.update(scheduleDTO)
        }
    } catch (e: Exception) {
        log.error("Error while saving schedule to database: ${e.message}")
        e.printStack()
    }
}

fun Application.prepareInMemoryCache() {
    log.info("Preparing in-memory cache...")
    InMemoryCache.grus = GroupsController().getAllMap()
    InMemoryCache.preps = PrepsController().getAllMap()
    InMemoryCache.auds = AudsController().getAllMap()
    InMemoryCache.scheduleMap = ScheduleController().getSchedules(true) as MutableList<SchedulaStyleDTO>
    log.info("Preparing in-memory cache... Done")
}


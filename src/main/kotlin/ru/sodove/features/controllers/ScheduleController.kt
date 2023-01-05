package ru.sodove.features.controllers

import ru.sodove.cache.InMemoryCache
import ru.sodove.database.dataclasses.schedule_jsonItem
import ru.sodove.database.dto.SchedulaStyleDTO
import ru.sodove.database.dto.ScheduleDTO
import ru.sodove.database.models.ScheduleModel
import ru.sodove.utilities.SchedulaUtilities.Companion.getFromOriginalApi
import ru.sodove.utilities.SchedulaUtilities.Companion.printer
import java.text.SimpleDateFormat
import java.time.Instant
import java.util.*

class ScheduleController {
    fun getSchedules(schedulaStyle: Boolean, allowCache: Boolean = false): List<Any> { //List<ScheduleDTO> or List<SchedulaStyleDTO>
        if (schedulaStyle) {
            if (allowCache) {
                printer("Getting schedules from cache")
                return InMemoryCache.scheduleMap
            }

            val schedules = ScheduleModel.getSchedule()
            val schedulaList = ArrayList<SchedulaStyleDTO>()

            schedules.forEach{ scheduleDTO ->
                schedulaList.add(convertDTO(scheduleDTO))
            }
            return schedulaList
        } else {
            return ScheduleModel.getSchedule()
        }
    }

    fun getScheduleByTypeAndId(id: Int, type: String, schedulaStyle: Boolean, allowCache: Boolean = false): Any { //ScheduleDTO or SchedulaStyleDTO
        return if (schedulaStyle) {
            val fromCache = if (allowCache) checkIsAvailableInCache(id, type) else null
            if (fromCache != null) {
                printer("Schedule for $id $type found in cache")
                fromCache
            } else {
                val scheduleDTO = ScheduleModel.getScheduleByTypeAndId(id, type)
                convertDTO(scheduleDTO)
            }
        } else {
            ScheduleModel.getScheduleByTypeAndId(id, type)
        }
    }

    suspend fun getScheduleByTypeAndIdAndDate(id: Int, type: String, schedulaStyle: Boolean, dateStart: String, dateEnd: String): Any {
        val schedule = getFromOriginalApi(id = id.toString(), type = type, dateStart = dateStart, dateEnd = dateEnd)
        return if (schedulaStyle) convertDTO(schedule)
        else schedule
    }

    private fun checkIsAvailableInCache(id: Int, type: String) : SchedulaStyleDTO? {
        val cacheList = InMemoryCache.scheduleMap
        return try {
            cacheList.first { it.id_ == id && it.type_ == type }
        } catch (e: NoSuchElementException) {
            null
        }
    }

    fun getSchedulesByType(type: String, schedulaStyle: Boolean): List<Any> { //List<ScheduleDTO> or List<SchedulaStyleDTO>
        return if (schedulaStyle) {
            val schedules = ScheduleModel.getSchedule().filter { it.type_ == type }
            val schedulaList = ArrayList<SchedulaStyleDTO>()

            schedules.forEach{ scheduleDTO ->
                schedulaList.add(convertDTO(scheduleDTO))
            }
            schedulaList
        } else {
            ScheduleModel.getSchedule().filter { it.type_ == type }
        }
    }

    //remove lists where last_update < 2 weeks
    fun removeOld() {
        ScheduleModel.removeOld(Instant.now().minusSeconds(1209600))
    }

//    not used rn
//    fun truncate() {
//        ScheduleModel.truncate()
//      }

//    not used rn
//    fun insert(scheduleDTO: ScheduleDTO) {
//        ScheduleModel.insert(scheduleDTO)
//    }

    fun update(scheduleDTO: ScheduleDTO) {
        ScheduleModel.update(scheduleDTO)
    }

    fun getIcal(schedule: ScheduleDTO): String {
        val ical = StringBuilder()
        ical.append("BEGIN:VCALENDAR\r\n" +
                "VERSION:2.0\r\n" +
                "PRODID:-//sodove//NaraAPI//schedula one love!//EN\r\n" +
                "CALSCALE:GREGORIAN\r\n" +
                "METHOD:PUBLISH\r\n" +
                "X-WR-CALNAME:Schedula\r\n" +
                "X-WR-TIMEZONE:Asia/Yekaterinburg\r\n" +
                "X-WR-CALDESC:Расписание занятий\r\n" +
                "BEGIN:VTIMEZONE\r\n" +
                "TZID:Asia/Yekaterinburg\r\n" +
                "X-LIC-LOCATION:Asia/Yekaterinburg\r\n" +
                "BEGIN:DAYLIGHT\r\n" +
                "TZOFFSETFROM:+0300\r\n" +
                "TZOFFSETTO:+0400\r\n" +
                "TZNAME:YEKT\r\n" +
                "DTSTART:19700329T020000\r\n" +
                "RRULE:FREQ=YEARLY;BYMONTH=3;BYDAY=-1SU\r\n" +
                "END:DAYLIGHT\r\n" +
                "BEGIN:STANDARD\r\n" +
                "TZOFFSETFROM:+0500\r\n" +
                "TZOFFSETTO:+0500\r\n" +
                "TZNAME:YEKT\r\n" +
                "DTSTART:19701025T030000\r\n" +
                "RRULE:FREQ=YEARLY;BYMONTH=10;BYDAY=-1SU\r\n" +
                "END:STANDARD\r\n" +
                "END:VTIMEZONE\r\n")

        schedule.data_.forEach { lesson ->
            //parse lesson.time as calendar and after add 95 minutes to it
            val sdf = SimpleDateFormat("HH:mm", Locale("ru"))
            val lessonTime = sdf.parse(lesson.time)
            val calendar = Calendar.getInstance()
            calendar.time = lessonTime
            calendar.add(Calendar.MINUTE, 95)
            val time_end = sdf.format(calendar.time).toString().replace(":", "") + "00"
            val time_start = lesson.time?.replace(":", "") + "00"
            val dateArray = lesson.date?.split(".")
            val date = dateArray?.get(2) + dateArray?.get(1) + dateArray?.get(0)
            val uid = date + time_start + (lesson.content?.aud ?: "") + (lesson.content?.disciplina
                ?: "") + (lesson.content?.lecturer ?: "")

            ical.append("BEGIN:VEVENT\r\n" +
                    "UID:$uid\r\n" +
                    "DTSTAMP:${date}T${time_start}Z\r\n" +
                    "DTSTART;TZID=Asia/Yekaterinburg:${date}T${time_start}\r\n" +
                    "DTEND;TZID=Asia/Yekaterinburg:${date}T${time_end}\r\n" +
                    "SUMMARY:${lesson.content?.disciplina}\r\n" +
                    "DESCRIPTION:${lesson.content?.lecturer}\r\n" +
                    "LOCATION:${lesson.content?.aud}\r\n" +
                    "ORGANIZER;CN=rsvpu via nara api:mailto:mail@rsvpu.ru\r\n" +
                    "END:VEVENT\r\n")

        }

        ical.append("END:VCALENDAR")
        return ical.toString()
    }

    companion object {
        fun convertDTO(scheduleDTO: ScheduleDTO): SchedulaStyleDTO {
            val scheduleDateTable: MutableList<List<schedule_jsonItem>> = ArrayList()

            var scheduleDate = scheduleDTO.data_[0].date
            var scheduleContent: MutableList<schedule_jsonItem> = ArrayList()

            for (lesson in scheduleDTO.data_) {
                val scheduleDateCycle = lesson.date

                if (scheduleDate != scheduleDateCycle) {
                    scheduleDateTable.add(scheduleContent)
                    scheduleContent = ArrayList()
                    scheduleDate = scheduleDateCycle
                }

                scheduleContent.add(lesson)
                if (lesson === scheduleDTO.data_[scheduleDTO.data_.size - 1])
                    scheduleDateTable.add(scheduleContent)
            }

            val schedulaStyleDTO = SchedulaStyleDTO()
            schedulaStyleDTO.id_ = scheduleDTO.id_
            schedulaStyleDTO.type_ = scheduleDTO.type_
            schedulaStyleDTO.last_update_ = scheduleDTO.last_update_
            schedulaStyleDTO.data_ = scheduleDateTable
            schedulaStyleDTO.start_date_ = scheduleDTO.start_date_

            return schedulaStyleDTO
        }
    }
}
package ru.sodove.utilities

import com.google.gson.Gson
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import org.slf4j.LoggerFactory
import ru.sodove.database.dataclasses.schedule_json
import ru.sodove.database.dto.ScheduleDTO
import java.time.Instant

class SchedulaUtilities {
    companion object {
        fun printer(input: Any){
            val logger = LoggerFactory.getLogger("Application")
            logger.info("$input")
        }

        suspend fun getFromOriginalApi(type: String, id: String, dateStart: String? = null, dateEnd: String? = null): ScheduleDTO {
            val url = "http://forum.rsvpu.ru/contents/api/rasp.php?$type=$id"
            val urlWithDate = if (dateStart != null && dateEnd != null) "$url&v_date_start=$dateStart&v_date_end=$dateEnd" else url
            val client = HttpClient()
            val response = client.get(urlWithDate)
            val body = response.bodyAsText()
            printer(body)

            val gson = Gson()
            val schedule = gson.fromJson(body, schedule_json::class.java)
            printer(schedule)
            return if (schedule.isNotEmpty()) ScheduleDTO(
                id_ = Integer.parseInt(id),
                type_ = type,
                data_ = schedule,
                last_update_ = Instant.now(),
                start_date_ = dateStart
            )
            else ScheduleDTO(
                id_ = Integer.parseInt(id),
                type_ = type,
                last_update_ = Instant.EPOCH,
                data_ = schedule_json(),
                start_date_ = "01.01.1970"
            )
        }
    }
}
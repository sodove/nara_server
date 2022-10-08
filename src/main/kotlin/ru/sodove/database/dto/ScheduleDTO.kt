package ru.sodove.database.dto

import com.google.gson.annotations.SerializedName
import ru.sodove.database.dataclasses.schedule_json
import java.time.Instant

class ScheduleDTO(
    @SerializedName("id")   val id_: Int,
    @SerializedName("type") val type_: String,
    @SerializedName("last_update") val last_update_: Instant,
    @SerializedName("data") val data_: schedule_json
)
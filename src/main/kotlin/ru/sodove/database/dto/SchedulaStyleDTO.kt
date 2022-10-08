package ru.sodove.database.dto

import com.google.gson.annotations.SerializedName
import ru.sodove.database.dataclasses.schedule_jsonItem
import java.time.Instant

class SchedulaStyleDTO {
    @SerializedName("id")   var id_: Int = 0
    @SerializedName("type") var type_: String = ""
    @SerializedName("last_update") var last_update_: Instant = Instant.now()
    @SerializedName("data") var data_: List<List<schedule_jsonItem>> = mutableListOf()
}
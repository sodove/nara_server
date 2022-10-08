package ru.sodove.database.dataclasses

import com.google.gson.annotations.SerializedName

@kotlinx.serialization.Serializable
data class schedule_jsonItem(
    @SerializedName("date"      ) var date      : String?  = null,
    @SerializedName("time"      ) var time      : String?  = null,
    @SerializedName("lesson"    ) var lesson    : Int?     = null,
    @SerializedName("timetable" ) var timetable : String?  = null,
    @SerializedName("content"   ) var content   : Content? = Content()
)
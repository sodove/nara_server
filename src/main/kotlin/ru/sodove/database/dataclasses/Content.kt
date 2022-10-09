package ru.sodove.database.dataclasses

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Contextual

@kotlinx.serialization.Serializable
data class Content (
    @SerializedName("disciplina"      ) var disciplina     : String?  = null,
    @SerializedName("type_disciplina" ) var typeDisciplina : String?  = null,
    @SerializedName("gru") @Contextual  var gru            : Any? = null,
    @SerializedName("aud"             ) var aud            : String?  = null,
    @SerializedName("lecturer"        ) var lecturer       : String?  = null,
    @SerializedName("note"            ) var note           : String?  = null,
    @SerializedName("subgroupname"    ) var subgroupname   : String?  = null
)
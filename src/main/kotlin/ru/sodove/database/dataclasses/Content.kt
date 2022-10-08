package ru.sodove.database.dataclasses

import com.google.gson.annotations.SerializedName

@kotlinx.serialization.Serializable
data class Content (
    @SerializedName("disciplina"      ) var disciplina     : String?  = null,
    @SerializedName("type_disciplina" ) var typeDisciplina : String?  = null,
    @SerializedName("gru"             ) var gru            : Boolean? = null,
    @SerializedName("aud"             ) var aud            : String?  = null,
    @SerializedName("lecturer"        ) var lecturer       : String?  = null,
    @SerializedName("note"            ) var note           : String?  = null,
    @SerializedName("subgroupname"    ) var subgroupname   : String?  = null
)
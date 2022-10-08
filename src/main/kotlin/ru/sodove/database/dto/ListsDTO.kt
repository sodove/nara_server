package ru.sodove.database.dto

import java.time.Instant

class ListsDTO(
    val id: Int,
    val type: String,
    val last_update: Instant,
    val data: String,
)
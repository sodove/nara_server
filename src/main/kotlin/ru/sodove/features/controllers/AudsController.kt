package ru.sodove.features.controllers

import ru.sodove.database.dto.ListsDTO
import ru.sodove.database.models.ListsModel

class AudsController {
    fun getAll(): List<ListsDTO> {
        return ListsModel.getAll("v_aud")
    }

    fun getAllMap(): LinkedHashMap<Int, String> {
        return getAll().associate { it.id to it.data } as LinkedHashMap<Int, String>
    }

    fun getFiltered(id: Int): Any {
        return ListsModel.getFiltered(id, "v_aud")
    }

    fun getFiltered(data: String): Any {
        return ListsModel.getFiltered(data, "v_aud")
    }
}
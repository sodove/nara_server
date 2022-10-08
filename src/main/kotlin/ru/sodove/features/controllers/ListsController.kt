package ru.sodove.features.controllers

import ru.sodove.database.dto.ListsDTO
import ru.sodove.database.models.ListsModel
import java.time.Instant

class ListsController {
//    not used rn
//    fun insert(listsDTO: ListsDTO) {
//        ListsModel.insert(listsDTO)
//    }

    fun getAll(): List<ListsDTO> {
        return ListsModel.getAll()
    }
    fun update(listsDTO: ListsDTO) {
        ListsModel.update(listsDTO)
    }

    //remove lists where last_update < 2 weeks
    fun removeOld() {
        ListsModel.removeOld(Instant.now().minusSeconds(1209600))
    }

    fun updateLists(grus: LinkedHashMap<Int, String>, preps: LinkedHashMap<Int, String>, auds: LinkedHashMap<Int, String>) {
        val last_update = Instant.now()
        grus.forEach { update(ListsDTO(id = it.key, data = it.value, type = "v_gru", last_update = last_update)) }
        preps.forEach { update(ListsDTO(id = it.key, data = it.value, type = "v_prep", last_update = last_update)) }
        auds.forEach { update(ListsDTO(id = it.key, data = it.value, type = "v_aud", last_update = last_update)) }
    }

//    not used rn
//    fun getAll(): List<ListsDTO> {
//        return ListsModel.getAll()
//    }
}
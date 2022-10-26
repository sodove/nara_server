package ru.sodove.database.models

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.javatime.timestamp
import org.jetbrains.exposed.sql.transactions.transaction
import ru.sodove.database.dto.ListsDTO
import ru.sodove.utilities.SchedulaUtilities.Companion.printer
import java.time.Instant

object ListsModel : Table("lists") {
    private val id_ = ListsModel.integer("id")
    private val data_ = ListsModel.varchar("data", 60)
    private val last_update_ = ListsModel.timestamp("last_update")
    private val type_ = ListsModel.varchar("type", 10)

    fun insert(listsDTO: ListsDTO) {
        transaction {
            ListsModel.insert {
                it[id_] = listsDTO.id
                it[data_] = listsDTO.data
                it[type_] = listsDTO.type
                it[last_update_] = listsDTO.last_update
            }
        }
        printer("List for ${listsDTO.id} ${listsDTO.type} (${listsDTO.data}) inserted")
    }

//    not used rn
//    fun truncate() {
//        transaction {
//            ListsModel.deleteAll()
//        }
//    }

//    not used rn
//    fun getAll(): List<ListsDTO> {
//        return transaction {
//            ListsModel.selectAll().map {
//                ListsDTO(
//                    id = it[id_],
//                    data = it[data_],
//                    type = it[type_],
//                    last_update = it[last_update_]
//                )
//            }
//        }
//    }

    fun getAll() : List<ListsDTO> {
        return transaction {
            ListsModel.selectAll().orderBy(type_).orderBy(data_).map {
                ListsDTO(
                    id = it[id_],
                    data = it[data_],
                    type = it[type_],
                    last_update = it[last_update_]
                )
            }
        }
    }

    fun getAll(type__: String): List<ListsDTO> {
        return transaction {
            ListsModel.select { type_ like "%$type__%"}.orderBy(data_).map {
                ListsDTO(
                    id = it[id_],
                    data = it[data_],
                    type = it[type_],
                    last_update = it[last_update_]
                )
            }
        }
    }

    fun getFiltered(id: Int, type__: String): ListsDTO {
        return transaction {
            ListsModel.select { (id_ eq id) and (type_ like "%$type__%") }.map {
                ListsDTO(
                    id = it[id_],
                    data = it[data_],
                    type = it[type_],
                    last_update = it[last_update_]
                )
            }
        }.first()
    }

    fun getFiltered(data__: String, type__: String): List<ListsDTO> {
        return transaction {
            ListsModel.select { (data_ like "%$data__%") and (type_ like "%$type__%") }.map {
                ListsDTO(
                    id = it[id_],
                    data = it[data_],
                    type = it[type_],
                    last_update = it[last_update_]
                )
            }
        }
    }

//    not used rn
//    fun addAll(arrayList: ArrayList<ListsDTO>) {
//        transaction {
//            for (item in arrayList) {
//                ListsModel.insert {
//                    it[id_] = item.id
//                    it[data_] = item.data
//                    it[type_] = item.type
//                    it[last_update_] = item.last_update
//                }
//            }
//        }
//    }

    fun removeOld(minusSeconds: Instant) {
        transaction {
            ListsModel.deleteWhere { last_update_ less minusSeconds }
        }
    }

    fun update(listsDTO: ListsDTO) {
        try {
            val schedule = getFiltered(listsDTO.id, listsDTO.type)
            if (schedule.data != listsDTO.data && schedule.id == listsDTO.id && schedule.type == listsDTO.type) {
                transaction {
                    ListsModel.update({ (id_ eq schedule.id) and (type_ eq schedule.type) }) {
                        it[data_] = listsDTO.data
                        it[last_update_] = listsDTO.last_update
                    }
                }
                printer("Data for ${listsDTO.type} with id ${listsDTO.id} updated")
            }
            else if (schedule.id == listsDTO.id && schedule.type == listsDTO.type) {
                transaction {
                    ListsModel.update({ (id_ eq schedule.id) and (type_ eq schedule.type) }) {
                        it[last_update_] = listsDTO.last_update
                    }
                }
                printer("List for ${listsDTO.type} with id ${listsDTO.id} is up to date, last_update updated")
            }
        }
        catch (e: NoSuchElementException) {
            insert(listsDTO)
        }
    }
}
package ru.sodove.database.models

import com.google.gson.Gson
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.javatime.timestamp
import org.jetbrains.exposed.sql.transactions.transaction
import ru.sodove.database.dataclasses.schedule_json
import ru.sodove.database.dto.ScheduleDTO
import ru.sodove.utilities.jsonb
import java.time.Instant

object ScheduleModel : Table("schedule")  {
    private val id_ = ScheduleModel.integer("id")
    private val type_ = ScheduleModel.varchar("type", 10)
    private val last_update_ = ScheduleModel.timestamp("last_update")
    private val data_ = ScheduleModel.jsonb("data", schedule_json::class.java, Gson(), true)

    fun insert(scheduleDTO: ScheduleDTO) {
        transaction {
            ScheduleModel.insert {
                it[id_] = scheduleDTO.id_
                it[type_] = scheduleDTO.type_
                it[data_] = scheduleDTO.data_
                it[last_update_] = scheduleDTO.last_update_
            }
        }
    }

//    not used rn
//    fun truncate() {
//        transaction {
//            ScheduleModel.deleteAll()
//        }
//    }

    fun getScheduleByTypeAndId(id: Int, type: String): ScheduleDTO {
        return transaction {
            ScheduleModel.select {
                (id_ eq id) and (type_ eq type)
            }.map {
                ScheduleDTO(
                    id_ = it[id_],
                    data_ = it[data_],
                    type_ = it[type_],
                    last_update_ = it[last_update_]
                )
            }.first()
        }
    }

    fun getSchedule() : List<ScheduleDTO> {
        return transaction {
            ScheduleModel.selectAll().orderBy(type_).orderBy(id_).map {
                ScheduleDTO(
                    id_ = it[id_],
                    data_ = it[data_],
                    type_ = it[type_],
                    last_update_ = it[last_update_]
                )
            }
        }
    }

    fun update(scheduleDTO: ScheduleDTO) {
        //get schedule by id and type, if it exists, update it, else insert
        try {
            val schedule = getScheduleByTypeAndId(scheduleDTO.id_, scheduleDTO.type_)
            // if last_update_ less than 30 minutes, do not update
            if (schedule.data_ != scheduleDTO.data_) {
                transaction {
                    ScheduleModel.update({ (id_ eq scheduleDTO.id_) and (type_ eq scheduleDTO.type_) }) {
                        it[data_] = scheduleDTO.data_
                        it[last_update_] = scheduleDTO.last_update_
                    }
                }
            }
        }
        catch (e: NoSuchElementException) {
            insert(scheduleDTO)
        }
    }

    fun removeOld(minusSeconds: Instant) {
        transaction {
            ScheduleModel.deleteWhere { last_update_ less minusSeconds }
        }
    }
}
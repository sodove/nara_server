package ru.sodove.features.controllers

import ru.sodove.database.dataclasses.schedule_jsonItem
import ru.sodove.database.dto.SchedulaStyleDTO
import ru.sodove.database.dto.ScheduleDTO
import ru.sodove.database.models.ScheduleModel
import java.time.Instant

class ScheduleController {
    fun getSchedules(schedulaStyle: Boolean): List<Any> { //List<ScheduleDTO> or List<SchedulaStyleDTO>
        return if (schedulaStyle) {
            val schedules = ScheduleModel.getSchedule()
            val schedulaList = ArrayList<SchedulaStyleDTO>()

            schedules.forEach{ scheduleDTO ->
                schedulaList.add(convertDTO(scheduleDTO))
            }
            schedulaList
        } else {
            ScheduleModel.getSchedule()
        }
    }

    fun getScheduleByTypeAndId(id: Int, type: String, schedulaStyle: Boolean): Any { //ScheduleDTO or SchedulaStyleDTO
        return if (schedulaStyle) {
            val scheduleLessons = ScheduleModel.getScheduleByTypeAndId(id, type)
            convertDTO(scheduleLessons)
        } else {
            ScheduleModel.getScheduleByTypeAndId(id, type)
        }
    }

    fun getSchedulesByType(type: String, schedulaStyle: Boolean): List<Any> { //List<ScheduleDTO> or List<SchedulaStyleDTO>
        return if (schedulaStyle) {
            val schedules = ScheduleModel.getSchedule().filter { it.type_ == type }
            val schedulaList = ArrayList<SchedulaStyleDTO>()

            schedules.forEach{ scheduleDTO ->
                schedulaList.add(convertDTO(scheduleDTO))
            }
            schedulaList
        } else {
            ScheduleModel.getSchedule().filter { it.type_ == type }
        }
    }

    private fun convertDTO(scheduleDTO: ScheduleDTO) : SchedulaStyleDTO {
        val scheduleDateTable: MutableList<List<schedule_jsonItem>> = ArrayList()

        var scheduleDate = scheduleDTO.data_[0].date
        var scheduleContent: MutableList<schedule_jsonItem> = ArrayList()

        for (lesson in scheduleDTO.data_) {
            val scheduleDateCycle = lesson.date

            if (scheduleDate != scheduleDateCycle) {
                scheduleDateTable.add(scheduleContent)
                scheduleContent = ArrayList()
                scheduleDate = scheduleDateCycle
            }

            scheduleContent.add(lesson)
            if (lesson === scheduleDTO.data_[scheduleDTO.data_.size - 1])
                scheduleDateTable.add(scheduleContent)
        }

        val schedulaStyleDTO = SchedulaStyleDTO()
        schedulaStyleDTO.id_ = scheduleDTO.id_
        schedulaStyleDTO.type_ = scheduleDTO.type_
        schedulaStyleDTO.last_update_ = scheduleDTO.last_update_
        schedulaStyleDTO.data_ = scheduleDateTable

        return schedulaStyleDTO
    }

    //remove lists where last_update < 2 weeks
    fun removeOld() {
        ScheduleModel.removeOld(Instant.now().minusSeconds(1209600))
    }

//    not used rn
//    fun truncate() {
//        ScheduleModel.truncate()
//      }

//    not used rn
//    fun insert(scheduleDTO: ScheduleDTO) {
//        ScheduleModel.insert(scheduleDTO)
//    }

    fun update(scheduleDTO: ScheduleDTO) {
        ScheduleModel.update(scheduleDTO)
    }

}
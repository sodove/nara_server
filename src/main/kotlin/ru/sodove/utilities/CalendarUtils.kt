package ru.sodove.utilities

import ru.sodove.utilities.SchedulaUtilities.Companion.printer
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import java.util.*


class CalendarUtils {
    val firstDayOfWeek: DayOfWeek = DayOfWeek.valueOf("MONDAY")
    val lastDayOfWeek: DayOfWeek = DayOfWeek.of((firstDayOfWeek.value + 5) % DayOfWeek.values().size + 1)

    fun firstDayOfWeek(): LocalDate {
        return LocalDate.now(TZ).with(TemporalAdjusters.previousOrSame(firstDayOfWeek))
    }
    fun lastDayOfWeek(): LocalDate {
        return LocalDate.now(TZ).with(TemporalAdjusters.nextOrSame(lastDayOfWeek))
    }



    //get first day of previous week
    fun firstDayOfPreviousWeek(): LocalDate {
        return LocalDate.now(TZ).with(
            TemporalAdjusters.previousOrSame(firstDayOfWeek)
        ).minusWeeks(1)
    }

    fun getParity(): Boolean {
        val currentMonth = Calendar.getInstance().get(Calendar.MONTH) + 1
        val year = if (currentMonth < 9) {
            Calendar.getInstance().get(Calendar.YEAR) - 1
        } else {
            Calendar.getInstance().get(Calendar.YEAR)
        }
        val weekFromFirstSeptember = CalendarUtils().getWeekDifference(year)
        printer("Weeks from first september: $weekFromFirstSeptember")
        val parity = weekFromFirstSeptember % 2 == 0
        printer("Parity: $parity")
        return parity
    }

    fun getWeekDifference(year: Int): Int {
        val firstSeptember = Calendar.getInstance()
        firstSeptember.set(Calendar.YEAR, year)
        firstSeptember.set(Calendar.MONTH, Calendar.SEPTEMBER)
        firstSeptember.set(Calendar.DAY_OF_MONTH, 1)
        firstSeptember.firstDayOfWeek = Calendar.MONDAY

        val currentCalendar = Calendar.getInstance()
        currentCalendar.firstDayOfWeek = Calendar.MONDAY
        currentCalendar.set(Calendar.DAY_OF_WEEK, Calendar.THURSDAY)

        val currentWeek = currentCalendar.get(Calendar.WEEK_OF_YEAR)
        val firstSeptemberWeek = firstSeptember.get(Calendar.WEEK_OF_YEAR)

        val weeks = if (currentWeek < firstSeptemberWeek) {
            53 - firstSeptemberWeek + currentWeek
        } else {
            currentWeek - firstSeptemberWeek
        }
//
//        printer("Current date: ${currentCalendar.time}")
//        printer("Weeks from first september: $weeks")
        return weeks
    }

    override fun toString(): String {
        return String.format(
            "The %s week starts on %s and ends on %s",
            locale.displayName,
            firstDayOfWeek,
            lastDayOfWeek
        )
    }

    companion object {
        fun dayHigherOrEqualThanToday(day: String?): Boolean {
            val today = LocalDate.now(TZ)
            return try {
                val dayDate = LocalDate.parse(day, formatter)
                dayDate.isAfter(today) || dayDate.isEqual(today)
            } catch (e: Exception) {
                false
            }
        }
        val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
        private val locale = Locale("ru-RU")
        // Try and always specify the time zone you're working with
        private val TZ = ZoneId.of("Asia/Yekaterinburg")
    }
}

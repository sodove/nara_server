package ru.sodove.cache

import ru.sodove.database.dto.SchedulaStyleDTO

object InMemoryCache {
    var grus = LinkedHashMap<Int, String>()
    var preps = LinkedHashMap<Int, String>()
    var auds = LinkedHashMap<Int, String>()
    var scheduleMap = MutableList(0) { SchedulaStyleDTO() }
    var isListsUpdating = true
}
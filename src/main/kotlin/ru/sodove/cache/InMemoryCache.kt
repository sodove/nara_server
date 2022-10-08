package ru.sodove.cache

object InMemoryCache {
    var grus = LinkedHashMap<Int, String>()
    var preps = LinkedHashMap<Int, String>()
    var auds = LinkedHashMap<Int, String>()
    var isListsUpdating = true
}
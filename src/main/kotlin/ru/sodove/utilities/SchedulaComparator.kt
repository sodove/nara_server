package ru.sodove.utilities

import ru.sodove.database.dto.ListsDTO

val schedulaComparator = Comparator<ListsDTO> { o1, o2 ->
    extractInt(o1.data) - extractInt(o2.data)
}

fun extractInt(s: String): Int {
    val num = s.split("-")[0].replace("\\D".toRegex(), "")
    return if (num.isEmpty()) Int.MAX_VALUE else Integer.parseInt(num)
}
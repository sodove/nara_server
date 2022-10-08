package ru.sodove.utilities

import com.google.gson.Gson
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.Function
import org.jetbrains.exposed.sql.statements.api.PreparedStatementApi
import org.postgresql.util.PGobject

/**
 * I don't understand what is written below, but it works, and I don't give a fuck
 * Thanks to the guys at Jetbrains/Exposed#127 for the tip.
 * edited by @sodove
 */

fun <T : Any> Table.jsonb(name: String, class_: Class<T>, gson: Gson, nullable: Boolean): Column<T> {
    return registerColumn(name, JsonColumnType(class_, gson, nullable))
}

class JsonColumnType<out T : Any>(private val klass: Class<T>, private val gson: Gson, override var nullable: Boolean) :
    IColumnType {

    override fun setParameter(stmt: PreparedStatementApi, index: Int, value: Any?) {
        val obj = PGobject()
        obj.type = "jsonb"
        if (value != null)
            obj.value = value as String
        stmt[index] = obj
    }

    override fun valueFromDB(value: Any): Any = when (value) {
        is HashMap<*, *> -> value
        is Map<*, *> -> value
        else -> {
            value as PGobject
            try {
                val json = value.value
                gson.fromJson(json, klass)
            } catch (e: Exception) {
                e.printStackTrace()
                throw RuntimeException("Can't parse JSON: $value")
            }
        }
    }

    override fun notNullValueToDB(value: Any): Any = gson.toJson(value)
    override fun nonNullValueToString(value: Any): String = "'${gson.toJson(value)}'"
    override fun sqlType() = "jsonb"
}

class JsonKey(val key: String) : Expression<String>() {
    init {
        if (!key.matches("[a-zA-Z]+".toRegex())) throw IllegalArgumentException("Only simple json key allowed.")
    }

    override fun toQueryBuilder(queryBuilder: QueryBuilder) = queryBuilder { append(key) }

}

inline fun <reified T> Column<Map<*, *>>.json(jsonKey: JsonKey): Function<T> {
    val columnType = when (T::class) {
        Int::class -> IntegerColumnType()
        String::class -> VarCharColumnType()
        Boolean::class -> BooleanColumnType()
        else -> throw java.lang.RuntimeException("Column type ${T::class} not supported for json field.")
    }

    return json(jsonKey, columnType)
}

fun <T> Column<Map<*, *>>.json(jsonKey: JsonKey, columnType: IColumnType): Function<T> {
    return JsonVal(expr = this, jsonKey = jsonKey, columnType = columnType)
}

private class JsonVal<T>(val expr: Expression<*>, val jsonKey: JsonKey, override val columnType: IColumnType) : Function<T>(columnType) {
    override fun toQueryBuilder(queryBuilder: QueryBuilder) =
        queryBuilder { append("CAST((${expr.toQueryBuilder(queryBuilder)} ->> '${jsonKey.key}') AS ${columnType.sqlType()})") }
}
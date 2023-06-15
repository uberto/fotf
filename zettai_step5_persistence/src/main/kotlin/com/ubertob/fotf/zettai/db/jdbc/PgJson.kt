package com.ubertob.fotf.zettai.db.jdbc

import com.beust.klaxon.Converter
import com.beust.klaxon.JsonValue
import com.ubertob.fotf.zettai.domain.ListName
import com.ubertob.fotf.zettai.domain.User
import com.ubertob.fotf.zettai.eventsourcing.EntityId
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.statements.api.PreparedStatementApi
import org.postgresql.util.PGobject
import java.time.Instant
import java.time.LocalDate
import java.util.*
import org.jetbrains.exposed.sql.Function as ExposedFunction


fun Table.jsonb(name: String): Column<String> =
    jsonb(name, { it }, { it })


fun <T : Any> Table.jsonb(name: String, fromJson: (String) -> T, toJson: (T) -> String): Column<T> =
    registerColumn(name, JsonBColumn(fromJson = fromJson, toJson = toJson))

class JsonBColumn<T : Any>(
    private val fromJson: (String) -> T,
    private val toJson: (T) -> String
) : ColumnType() {

    override fun sqlType() = "jsonb"

    private fun valueToPGobject(paramValue: String): PGobject =
        PGobject().apply {
            type = "jsonb"
            value = paramValue
        }

    override fun setParameter(stmt: PreparedStatementApi, index: Int, value: Any?) {
        super.setParameter(
            stmt,
            index,
            valueToPGobject(value as? String ?: "NULL")
        )
    }

    override fun valueFromDB(value: Any): T =
        when (value) {
            is PGobject -> fromJson(value.value ?: error("Unexpected null value on valueFromDB"))
            else -> error("Unexpected value type ${value::class.qualifiedName} for JsonBColumn")
        }

    @Suppress("UNCHECKED_CAST")
    override fun notNullValueToDB(value: Any): String = toJson(value as T)
    override fun nonNullValueToString(value: Any): String = "'${notNullValueToDB(value)}'"
}


class JsonBValue<T>(
    val expr: Expression<*>,
    override val columnType: ColumnType,
    val jsonPath: List<String>
) : ExposedFunction<T>(columnType) {
    override fun toQueryBuilder(queryBuilder: QueryBuilder) = queryBuilder {
        val castJson = columnType.sqlType() != "JSONB"
        if (castJson) append("(")
        append(expr)
        append(" #>")
        if (castJson) append(">")
        append(" '{${jsonPath.joinToString { escapeFieldName(it) }}}'")
        if (castJson) append(")::${columnType.sqlType()}")
    }

    companion object {

        private fun escapeFieldName(value: String) = value.map {
            fieldNameCharactersToEscape[it] ?: it
        }.joinToString("").let { "\"$it\"" }

        private val fieldNameCharactersToEscape = mapOf(
            // '\"' to "\'\'", // no need to escape single quote as we put string in double quotes
            '\"' to "\\\"",
            '\r' to "\\r",
            '\n' to "\\n"
        )
    }
}

inline fun <reified T> Column<*>.json(vararg jsonPath: String): JsonBValue<T> {
    val columnType = when (T::class) {
        Boolean::class -> BooleanColumnType()
        Int::class -> IntegerColumnType()
        Float::class -> FloatColumnType()
        String::class -> TextColumnType()
        else -> JsonBColumn({ error("Unexpected call") }, { error("Unexpected call") })
    }
    return JsonBValue(this, columnType, jsonPath.toList())
}


class JsonContainsOp(expr1: Expression<*>, expr2: Expression<*>) : ComparisonOp(expr1, expr2, "??")

/** Checks if this expression contains some [t] value. */
infix fun <T> JsonBValue<Any>.contains(t: T): JsonContainsOp =
    JsonContainsOp(this, SqlExpressionBuilder.run { this@contains.wrap(t) })

/** Checks if this expression contains some [other] expression. */
infix fun <T> JsonBValue<Any>.contains(other: Expression<T>): JsonContainsOp =
    JsonContainsOp(this, other)


object EntityIdConverter : Converter {
    override fun canConvert(cls: Class<*>): Boolean = cls == EntityId::class.java

    override fun fromJson(jv: JsonValue): EntityId =
        EntityId(UUID.fromString(jv.string))

    override fun toJson(value: Any): String =
        """"${(value as EntityId).raw}""""

}

object UserConverter : Converter {
    override fun canConvert(cls: Class<*>): Boolean = cls == User::class.java

    override fun fromJson(jv: JsonValue): User? =
        jv.string?.let {
            User.fromUntrusted(it)
        }


    override fun toJson(value: Any): String =
        """"${(value as? User)?.name}""""

}


object ListNameConverter : Converter {
    override fun canConvert(cls: Class<*>): Boolean = cls == ListName::class.java

    override fun fromJson(jv: JsonValue): ListName? =
        jv.string?.let {
            ListName.fromUntrusted(it)
        }

    override fun toJson(value: Any): String =
        """"${(value as? ListName)?.name}""""

}

object LocalDateConverter : Converter {
    override fun canConvert(cls: Class<*>) = cls == LocalDate::class.java

    override fun fromJson(jv: JsonValue): LocalDate? =
        jv.string?.let {
            LocalDate.parse(it)
        }

    override fun toJson(value: Any) = """"${(value as? LocalDate)}""""

}

object InstantConverter : Converter {
    override fun canConvert(cls: Class<*>) = cls == Instant::class.java

    override fun fromJson(jv: JsonValue): Instant? =
        jv.string?.let {
            Instant.parse(it)
        }

    override fun toJson(value: Any) = """"${(value as? Instant)}""""

}





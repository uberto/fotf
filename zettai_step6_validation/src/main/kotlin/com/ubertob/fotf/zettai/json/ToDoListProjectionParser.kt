package com.ubertob.fotf.zettai.json

import com.beust.klaxon.Klaxon
import com.ubertob.fotf.zettai.db.jdbc.*
import com.ubertob.fotf.zettai.fp.Outcome
import com.ubertob.fotf.zettai.fp.Parser
import com.ubertob.fotf.zettai.fp.ThrowableError
import com.ubertob.fotf.zettai.queries.ToDoListProjectionRow


val toDoListProjectionParser = Parser(
    parse = ::readProjectionRow,
    render = ::writeProjectionRow
)

fun writeProjectionRow(row: ToDoListProjectionRow): String = klaxon.toJsonString(row)

fun readProjectionRow(json: String): Outcome<ThrowableError, ToDoListProjectionRow> =
    Outcome.tryOrFail { klaxon.parse(json) ?: error("Empty row $json") }

private val klaxon = Klaxon()
    .converter(EntityIdConverter)
    .converter(UserConverter)
    .converter(ListNameConverter)
    .converter(LocalDateConverter)
    .converter(InstantConverter)
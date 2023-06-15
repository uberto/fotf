package com.ubertob.fotf.zettai.json

import com.beust.klaxon.Klaxon
import com.ubertob.fotf.zettai.db.eventsourcing.PgEvent
import com.ubertob.fotf.zettai.db.jdbc.*
import com.ubertob.fotf.zettai.domain.ZettaiOutcome
import com.ubertob.fotf.zettai.domain.ZettaiParsingError
import com.ubertob.fotf.zettai.events.*
import com.ubertob.fotf.zettai.fp.Outcome
import com.ubertob.fotf.zettai.fp.Parser


fun toDoListEventParser(): Parser<ToDoListEvent, PgEvent> = Parser(::toPgEvent, ::toToDoListEvent)


fun toPgEvent(event: ToDoListEvent): PgEvent =
    PgEvent(
        entityId = event.id,
        eventType = event::class.simpleName.orEmpty(),
        version = 1,
        source = "event store",
        jsonString = event.toJsonString()
    )


private val klaxon = Klaxon()
    .converter(EntityIdConverter)
    .converter(UserConverter)
    .converter(ListNameConverter)
    .converter(LocalDateConverter)
    .converter(InstantConverter)


fun toToDoListEvent(pgEvent: PgEvent): ZettaiOutcome<ToDoListEvent> =
    Outcome.tryOrFail {
        when (pgEvent.eventType) {
            ListCreated::class.simpleName -> klaxon.parse<ListCreated>(pgEvent.jsonString)
            ItemAdded::class.simpleName -> klaxon.parse<ItemAdded>(pgEvent.jsonString)
            ItemRemoved::class.simpleName -> klaxon.parse<ItemRemoved>(pgEvent.jsonString)
            ItemModified::class.simpleName -> klaxon.parse<ItemModified>(pgEvent.jsonString)
            ListPutOnHold::class.simpleName -> klaxon.parse<ListPutOnHold>(pgEvent.jsonString)
            ListClosed::class.simpleName -> klaxon.parse<ListClosed>(pgEvent.jsonString)
            ListReleased::class.simpleName -> klaxon.parse<ListReleased>(pgEvent.jsonString)
            else -> null
        } ?: error("type not known ${pgEvent.eventType}")
    }.transformFailure { ZettaiParsingError("Error parsing ToDoListEvent: ${pgEvent} with error: $it ") }


fun ToDoListEvent.toJsonString() = when (this) {
    is ListCreated -> klaxon.toJsonString(this)
    is ItemAdded -> klaxon.toJsonString(this)
    is ItemRemoved -> klaxon.toJsonString(this)
    is ItemModified -> klaxon.toJsonString(this)
    is ListPutOnHold -> klaxon.toJsonString(this)
    is ListReleased -> klaxon.toJsonString(this)
    is ListClosed -> klaxon.toJsonString(this)
}






package com.ubertob.fotf.zettai.db.eventsourcing

import com.ubertob.fotf.zettai.db.jdbc.TxReader
import com.ubertob.fotf.zettai.eventsourcing.*
import com.ubertob.fotf.zettai.fp.Parser
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.greater
import org.jetbrains.exposed.sql.Transaction

class EventStreamerTx<E : EntityEvent, NK : Any>(
    private val table: PgEventTable,
    private val eventParser: Parser<E, PgEvent>,
    private val naturalKeySql: (NK) -> String
) : EventStreamer<Transaction, E, NK> {

    override fun fetchByEntity(entityId: EntityId): TxReader<List<E>> =
        table.queryEvents(table.entity_id eq entityId.raw)
            .transform { pgEvents ->
                pgEventsToEvents(pgEvents).map(StoredEvent<E>::event)
            }

    private fun pgEventsToEvents(pgEvents: List<StoredEvent<PgEvent>>) =
        pgEvents.map {
            StoredEvent(it.eventSeq, it.storedAt, eventParser.parseOrThrow(it.event))
        }

    override fun fetchAfter(eventSeq: EventSeq): TxReader<List<StoredEvent<E>>> =
        table.queryEvents(table.id greater eventSeq.progressive)
            .transform(this::pgEventsToEvents)


    override fun retrieveIdFromNaturalKey(key: NK): TxReader<EntityId?> =
        table.getEntityIdBySql(naturalKeySql(key))


    override fun store(newEvents: Iterable<E>): TxReader<List<StoredEvent<E>>> =
        table
            .insertEvents(newEvents.map { eventParser.render(it) })
            .transform(this::pgEventsToEvents)

}
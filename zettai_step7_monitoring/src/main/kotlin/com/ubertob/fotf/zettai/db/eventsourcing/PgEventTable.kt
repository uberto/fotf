package com.ubertob.fotf.zettai.db.eventsourcing

import com.ubertob.fotf.zettai.db.jdbc.*
import com.ubertob.fotf.zettai.eventsourcing.EntityId
import com.ubertob.fotf.zettai.eventsourcing.EventSeq
import com.ubertob.fotf.zettai.eventsourcing.StoredEvent
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.`java-time`.CurrentTimestamp
import org.jetbrains.exposed.sql.`java-time`.timestamp
import org.jetbrains.exposed.sql.statements.InsertStatement

data class PgEventTable(override val tableName: String) : Table(tableName) {
    val id = long("id").autoIncrement()
    override val primaryKey: PrimaryKey = PrimaryKey(id, name = "${tableName}_pkey")
    val recorded_at = timestamp("recorded_at").defaultExpression(CurrentTimestamp())

    val entity_id = uuid("entity_id")
    val event_type = varchar("event_type", 100)
    val event_version = integer("event_version")
    val event_source = varchar("event_source", 100)
    val json_data = jsonb("json_data")

    fun rowToPgEvent(it: ResultRow) = StoredEvent(
        eventSeq = EventSeq(it[id]),
        storedAt = it[recorded_at],
        PgEvent(
            entityId = EntityId(it[entity_id]),
            jsonString = it[json_data],
            eventType = it[event_type],
            version = it[event_version],
            source = it[event_source]
        )
    )

    fun insertEvents(events: Iterable<PgEvent>): TxReader<List<StoredEvent<PgEvent>>> =
        TxReader { tx ->
            events.map { event ->
                insertIntoWithReturn(tx, toStoredEvent(event))
                { insert ->
                    insert[entity_id] = event.entityId.raw
                    insert[event_source] = event.source
                    insert[event_type] = event.eventType
                    insert[json_data] = event.jsonString
                    insert[event_version] = event.version
                }
            }
        }

    private fun toStoredEvent(event: PgEvent): InsertStatement<Number>.() -> StoredEvent<PgEvent> =
        { StoredEvent(EventSeq(get(id)), get(recorded_at), event) }

    fun queryEvents(condition: Op<Boolean>): TxReader<List<StoredEvent<PgEvent>>> =
        TxReader { tx ->
            selectWhere(tx, condition, id).map(::rowToPgEvent)
        }

    fun getEntityIdBySql(sql: String): TxReader<EntityId?> =
        TxReader { tx ->
            queryBySql(tx, listOf(entity_id), sql)
                .singleOrNull()
                ?.get(entity_id)
                ?.let { EntityId(it) }
        }
}
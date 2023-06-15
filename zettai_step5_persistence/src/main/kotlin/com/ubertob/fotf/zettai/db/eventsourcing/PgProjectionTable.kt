package com.ubertob.fotf.zettai.db.eventsourcing

import com.ubertob.fotf.zettai.db.fp.ContextReader
import com.ubertob.fotf.zettai.db.fp.TxReader
import com.ubertob.fotf.zettai.db.jdbc.*
import com.ubertob.fotf.zettai.eventsourcing.EventSeq
import com.ubertob.fotf.zettai.eventsourcing.RowId
import com.ubertob.fotf.zettai.fp.Parser
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.`java-time`.CurrentTimestamp
import org.jetbrains.exposed.sql.`java-time`.timestamp
import org.jetbrains.exposed.sql.statements.DeleteStatement
import java.time.Instant


data class PgProjectionTable<ROW : Any>(override val tableName: String, val parser: Parser<ROW, String>) :
    Table(tableName) {

    val id = varchar("id", 50)
    override val primaryKey: PrimaryKey = PrimaryKey(id, name = "${tableName}_pkey")
    val updated_at = timestamp("recorded_at").defaultExpression(CurrentTimestamp())
    val row_data = jsonb("row_data", parser::parseOrThrow, parser::render.get())


    fun insertRow(rowId: RowId, row: ROW): TxReader<Unit> =
        TxReader { tx ->
            insertInto(tx) { newRow ->
                newRow[id] = rowId.id
                newRow[row_data] = row
            }
        }

    fun updateRow(rowId: RowId, row: ROW): TxReader<Unit> =
        TxReader { tx ->
            updateWhere(tx, id eq rowId.id) { updated ->
                updated[id] = rowId.id
                updated[row_data] = row
            }
        }

    fun selectRows(condition: Op<Boolean>): TxReader<List<ROW>> =
        TxReader { tx ->
            selectWhere(tx, condition, id).map { it[row_data] }
        }

    fun deleteRows(condition: Op<Boolean>): TxReader<Int> =
        TxReader { tx ->
            DeleteStatement.where(tx, this, condition)

        }

    fun selectRowsByJson(jsonWhere: String): TxReader<List<ROW>> =
        TxReader { tx ->
            queryBySql(tx, fields, jsonWhere).map { it[row_data] }
        }
}


data class PgLastEventTable(override val tableName: String) : Table(tableName) {
    val last_event_id = long("last_event_id")
    val updated_at = timestamp("recorded_at").defaultExpression(CurrentTimestamp())

    fun readLastEvent(): ContextReader<Transaction, EventSeq> =
        TxReader { tx ->
            selectWhere(tx, condition = null, orderByCond = null)
                .first()[last_event_id]
                .let(::EventSeq)
        }

    fun updateLastEvent(newEventSeq: EventSeq): ContextReader<Transaction, Unit> =
        TxReader { tx ->
            updateWhere(tx) {
                it[last_event_id] = newEventSeq.progressive
                it[updated_at] = Instant.now()
            }
        }

    fun insertLastEventIfMissing(eventSeq: EventSeq): ContextReader<Transaction, Unit> =
        TxReader { tx ->
            selectWhere(tx, condition = null, orderByCond = null).firstOrNull()
                ?: insertInto(tx) {
                    it[last_event_id] = eventSeq.progressive
                }
        }

}



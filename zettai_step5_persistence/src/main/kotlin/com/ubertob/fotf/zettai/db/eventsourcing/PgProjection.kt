package com.ubertob.fotf.zettai.db.eventsourcing

import com.ubertob.fotf.zettai.db.fp.ContextProvider
import com.ubertob.fotf.zettai.db.fp.ContextReader
import com.ubertob.fotf.zettai.eventsourcing.*
import com.ubertob.fotf.zettai.fp.Outcome
import com.ubertob.fotf.zettai.fp.OutcomeError
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.Transaction

data class PgProjection<R : Any, E : EntityEvent>(
    override val contextProvider: ContextProvider<Transaction>,
    override val eventFetcher: (EventSeq) -> Outcome<OutcomeError, List<StoredEvent<E>>>,
    override val eventProjector: ProjectEvents<R, E>,
    val projectionTable: PgProjectionTable<R>,
    val lastEventTable: PgLastEventTable
) : Projection<Transaction, R, E> {

    override fun readRow(rowId: RowId): ContextReader<Transaction, R?> =
        projectionTable.selectRows(projectionTable.id eq rowId.id).transform { it.firstOrNull() }

    override fun saveRow(rowId: RowId, row: R): ContextReader<Transaction, Unit> =
        projectionTable.insertRow(rowId, row)

    override fun deleteRow(rowId: RowId): ContextReader<Transaction, Unit> =
        projectionTable.deleteRows(projectionTable.id eq rowId.id).transform {}

    override fun updateRow(rowId: RowId, updateFn: (R) -> R): ContextReader<Transaction, Unit> =
        readRow(rowId).transform { row ->
            row?.let { updateFn(it) } ?: error("Row not present! $rowId")
        }.bind { row ->
            projectionTable.updateRow(rowId, row)
        }.transform {}

    override fun lastProjectedEvent(): ContextReader<Transaction, EventSeq> =
        lastEventTable.readLastEvent()


    override fun updateLastProjectedEvent(eventSeq: EventSeq): ContextReader<Transaction, Unit> =
        lastEventTable.updateLastEvent(eventSeq)

}



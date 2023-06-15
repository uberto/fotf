package com.ubertob.fotf.zettai.queries

import com.ubertob.fotf.zettai.db.fp.ContextError
import com.ubertob.fotf.zettai.db.fp.ContextProvider
import com.ubertob.fotf.zettai.db.fp.ContextReader
import com.ubertob.fotf.zettai.domain.ToDoItem
import com.ubertob.fotf.zettai.domain.ToDoStatus
import com.ubertob.fotf.zettai.events.*
import com.ubertob.fotf.zettai.eventsourcing.*
import com.ubertob.fotf.zettai.fp.Outcome
import com.ubertob.fotf.zettai.fp.OutcomeError
import com.ubertob.fotf.zettai.fp.asFailure
import com.ubertob.fotf.zettai.fp.asSuccess
import java.util.concurrent.atomic.AtomicReference

data class ItemProjectionRow(val item: ToDoItem, val listId: EntityId)

data class InMemoryProjectionError(override val msg: String, override val exception: Throwable?) : ContextError

class InMemoryProjectionProvider<R : Any> : ContextProvider<InMemoryRefContext<R>> {

    val rows = AtomicReference<Map<RowId, R>>(emptyMap())

    override fun <T> tryRun(reader: ContextReader<InMemoryRefContext<R>, T>): Outcome<ContextError, T> =
        try {
            reader.runWith(rows).asSuccess()
        } catch (t: Throwable) {
            InMemoryProjectionError("Projection query failed ${t.message}", t).asFailure()
        }
}

class ToDoItemProjection(eventFetcher: FetchStoredEvents<ToDoListEvent>) :
    InMemoryProjection<ItemProjectionRow, ToDoListEvent> by ConcurrentMapProjection(
        InMemoryProjectionProvider(),
        eventFetcher,
        ::eventProjector
    ) {


    fun findWhatsNext(maxRows: Int, lists: List<EntityId>): Outcome<OutcomeError, List<ItemProjectionRow>> =
        allRows()
            .transform {
                it.values
                    .filter { it.listId in lists }
                    .filter { it.item.dueDate != null && it.item.status == ToDoStatus.Todo }
                    .sortedBy { it.item.dueDate }
                    .take(maxRows)
            }

    companion object {
        fun eventProjector(e: ToDoListEvent): List<DeltaRow<ItemProjectionRow>> =
            when (e) {
                is ListCreated -> emptyList()
                is ItemAdded -> CreateRow(e.itemRowId(e.item), ItemProjectionRow(e.item, e.id)).toSingle()
                is ItemRemoved -> DeleteRow<ItemProjectionRow>(e.itemRowId(e.item)).toSingle()
                is ItemModified -> listOf(
                    CreateRow(e.itemRowId(e.item), ItemProjectionRow(e.item, e.id)),
                    DeleteRow(e.itemRowId(e.prevItem))
                )

                is ListPutOnHold -> emptyList()
                is ListReleased -> emptyList()
                is ListClosed -> emptyList()
            }
    }
}

private fun ToDoListEvent.itemRowId(item: ToDoItem): RowId = RowId("${id}_${item.description}")

package com.ubertob.fotf.zettai.fp

import com.ubertob.fotf.zettai.events.EventSeq
import java.util.concurrent.atomic.AtomicReference

interface InMemoryProjection<R : Any, E : EntityEvent> : Projection<R, E> {
    fun allRows(): Map<RowId, R>
}

data class ConcurrentMapProjection<R : Any, E : EntityEvent>(
    override val eventFetcher: FetchStoredEvents<E>,
    override val eventProjector: ProjectEvents<R, E>
) : InMemoryProjection<R, E> {

    private val rowsReference: AtomicReference<Map<RowId, R>> = AtomicReference(emptyMap())

    private val lastEventRef: AtomicReference<EventSeq> = AtomicReference(EventSeq(-1))

    override fun allRows(): Map<RowId, R> = rowsReference.get()

    override fun lastProjectedEvent(): EventSeq = lastEventRef.get()

    override fun applyDelta(eventSeq: EventSeq, deltas: List<DeltaRow<R>>) {
        deltas.forEach { delta ->
            rowsReference.getAndUpdate { rows ->
                when (delta) {
                    is CreateRow -> rows.createRow(delta)
                    is DeleteRow -> rows.deleteRow(delta)
                    is UpdateRow -> rows.updateRow(delta)
                }
            }
        }.also { lastEventRef.getAndSet(eventSeq) }
    }

    private fun Map<RowId, R>.createRow(delta: CreateRow<R>) = this + (delta.rowId to delta.row)
    private fun Map<RowId, R>.deleteRow(delta: DeleteRow<R>) = this - delta.rowId
    private fun Map<RowId, R>.updateRow(delta: UpdateRow<R>) =
        this[delta.rowId]
            ?.let { oldRow ->
                this - delta.rowId + (delta.rowId to delta.updateRow(oldRow))
            }
}

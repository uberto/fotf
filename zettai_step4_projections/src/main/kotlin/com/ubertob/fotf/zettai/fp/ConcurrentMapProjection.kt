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

    override fun applyDelta(eventSeq: EventSeq, deltas: List<DeltaRow<R>>) {
        deltas.forEach { delta ->
            rowsReference.updateAndGet { rows ->
                when (delta) {
                    is CreateRow -> rows + (delta.rowId to delta.row)
                    is DeleteRow -> rows - delta.rowId
                    is UpdateRow ->
                        rows[delta.rowId]?.let { oldRow ->
                            rows - delta.rowId + (delta.rowId to delta.updateRow(oldRow))
                        }
                }
            }
        }.also { lastEventRef.getAndSet(eventSeq) }
    }

    override fun lastProjectedEvent(): EventSeq = lastEventRef.get()

}
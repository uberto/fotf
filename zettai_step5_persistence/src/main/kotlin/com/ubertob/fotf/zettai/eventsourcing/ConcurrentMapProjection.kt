package com.ubertob.fotf.zettai.eventsourcing

import com.ubertob.fotf.zettai.db.fp.ContextProvider
import com.ubertob.fotf.zettai.db.fp.ContextReader
import com.ubertob.fotf.zettai.fp.Outcome
import com.ubertob.fotf.zettai.fp.OutcomeError
import java.util.concurrent.atomic.AtomicReference

typealias InMemoryRefContext<R> = AtomicReference<Map<RowId, R>>

interface InMemoryProjection<R : Any, E : EntityEvent> : Projection<InMemoryRefContext<R>, R, E> {
    fun allRows(): Outcome<OutcomeError, Map<RowId, R>>
}


data class ConcurrentMapProjection<R : Any, E : EntityEvent>(
    override val contextProvider: ContextProvider<InMemoryRefContext<R>>,
    override val eventFetcher: FetchStoredEvents<E>,
    override val eventProjector: ProjectEvents<R, E>
) : InMemoryProjection<R, E> {

    private val lastEventRef: AtomicReference<EventSeq> = AtomicReference(EventSeq(-1))

    override fun allRows(): Outcome<OutcomeError, Map<RowId, R>> =
        contextProvider.doRun { context.get() }

    override fun saveRow(rowId: RowId, row: R): ContextReader<InMemoryRefContext<R>, Unit> =
        ContextReader {
            it.updateAndGet { rows ->
                rows + (rowId to row)
            }
        }

    override fun deleteRow(rowId: RowId): ContextReader<InMemoryRefContext<R>, Unit> =
        ContextReader {
            it.updateAndGet { rows ->
                rows - rowId
            }
        }


    override fun readRow(rowId: RowId): ContextReader<InMemoryRefContext<R>, R?> =
        ContextReader {
            it.get()[rowId]
        }

    override fun updateRow(rowId: RowId, updateFn: (R) -> R): ContextReader<InMemoryRefContext<R>, Unit> =
        ContextReader {
            it.updateAndGet { rows ->
                rows[rowId]?.let { oldRow ->
                    rows - rowId + (rowId to updateFn(oldRow))
                }
            }
        }

    override fun lastProjectedEvent(): ContextReader<InMemoryRefContext<R>, EventSeq> =
        ContextReader {
            lastEventRef.get()
        }

    override fun updateLastProjectedEvent(eventSeq: EventSeq): ContextReader<InMemoryRefContext<R>, Unit> =
        ContextReader {
            lastEventRef.getAndSet(eventSeq)
        }


}
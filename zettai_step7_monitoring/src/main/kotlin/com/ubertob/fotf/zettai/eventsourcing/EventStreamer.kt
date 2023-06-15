package com.ubertob.fotf.zettai.eventsourcing

import com.ubertob.fotf.zettai.fp.ContextReader


interface EventStreamer<CTX, E : EntityEvent, NK : Any> {
    fun fetchByEntity(entityId: EntityId): ContextReader<CTX, List<E>>
    fun fetchAfter(eventSeq: EventSeq): ContextReader<CTX, List<StoredEvent<E>>>
    fun retrieveIdFromNaturalKey(key: NK): ContextReader<CTX, EntityId?>
    fun store(newEvents: Iterable<E>): ContextReader<CTX, List<StoredEvent<E>>>
}


typealias EventPersister<CTX, E> = (List<E>) -> ContextReader<CTX, List<StoredEvent<E>>>


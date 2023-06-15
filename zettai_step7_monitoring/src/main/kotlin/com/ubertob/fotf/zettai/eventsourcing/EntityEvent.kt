package com.ubertob.fotf.zettai.eventsourcing

import com.ubertob.fotf.zettai.fp.ContextReader
import java.util.*


data class EntityId(val raw: UUID) {
    companion object {
        fun mint() = EntityId(UUID.randomUUID())
        fun fromRowId(rowId: RowId) = EntityId(UUID.fromString(rowId.id))
    }
}

interface EntityEvent {
    val id: EntityId
}

interface EntityState<in E : EntityEvent> {
    fun combine(event: E): EntityState<E>
}

interface EntityRetriever<RES, out S : EntityState<E>, in E : EntityEvent, NK> {
    fun retrieveByNaturalKey(key: NK): ContextReader<RES, S?>
    fun retrieveById(id: EntityId): ContextReader<RES, S?>
}




package com.ubertob.fotf.zettai.events

import com.ubertob.fotf.zettai.db.fp.ContextReader
import com.ubertob.fotf.zettai.db.fp.bindNullable
import com.ubertob.fotf.zettai.domain.ListName
import com.ubertob.fotf.zettai.domain.User
import com.ubertob.fotf.zettai.eventsourcing.EventStore
import com.ubertob.fotf.zettai.eventsourcing.StoredEvent


data class UserListName(val user: User, val listName: ListName)

class ToDoListEventStore<CTX>(private val eventStreamer: ToDoListEventStreamer<CTX>) :
    EventStore<CTX, ToDoListEvent, ToDoListState, UserListName> {

    override fun retrieveById(id: ToDoListId): ContextReader<CTX, ToDoListState> =
        eventStreamer.fetchByEntity(id)
            .transform(List<ToDoListEvent>::fold)

    override fun invoke(events: List<ToDoListEvent>): ContextReader<CTX, List<StoredEvent<ToDoListEvent>>> =
        eventStreamer.store(events)

    override fun retrieveByNaturalKey(key: UserListName): ContextReader<CTX, ToDoListState?> =
        eventStreamer.retrieveIdFromNaturalKey(key)
            .bindNullable { entityId -> retrieveById(entityId) }

}


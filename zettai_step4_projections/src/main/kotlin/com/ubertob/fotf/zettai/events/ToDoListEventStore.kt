package com.ubertob.fotf.zettai.events

import com.ubertob.fotf.zettai.domain.ListName
import com.ubertob.fotf.zettai.domain.ToDoListRetriever
import com.ubertob.fotf.zettai.domain.User
import com.ubertob.fotf.zettai.fp.EventPersister


class ToDoListEventStore(private val eventStreamer: ToDoListEventStreamer) : ToDoListRetriever,
    EventPersister<ToDoListEvent> {

    private fun retrieveById(id: ToDoListId): ToDoListState? =
        eventStreamer(id)
            ?.fold()

    override fun retrieveByName(user: User, listName: ListName): ToDoListState? =
        eventStreamer.retrieveIdFromName(user, listName)
            ?.let(::retrieveById)

    override fun invoke(events: List<ToDoListEvent>): List<ToDoListEvent> =
        eventStreamer.store(events)

}


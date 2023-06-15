package com.ubertob.fotf.zettai.events

import com.ubertob.fotf.zettai.domain.ListName
import com.ubertob.fotf.zettai.domain.User
import com.ubertob.fotf.zettai.fp.EventStreamer
import java.util.concurrent.atomic.AtomicReference

interface ToDoListEventStreamer : EventStreamer<ToDoListEvent> {
    fun retrieveIdFromName(user: User, listName: ListName): ToDoListId?
    fun store(newEvents: Iterable<ToDoListEvent>): List<ToDoListEvent>
}

class ToDoListEventStreamerInMemory : ToDoListEventStreamer {

    val events = AtomicReference<List<ToDoListEvent>>(listOf())

    override fun retrieveIdFromName(user: User, listName: ListName): ToDoListId? =
        events.get()
            .firstOrNull { it == ListCreated(it.id, user, listName) }
            ?.id

    override fun store(newEvents: Iterable<ToDoListEvent>): List<ToDoListEvent> =
        newEvents.toList().also { ne -> events.updateAndGet { it + ne } }

    override fun invoke(id: ToDoListId): List<ToDoListEvent> =
        events.get()
            .filter { it.id == id }

}
package com.ubertob.fotf.zettai.events

import com.ubertob.fotf.zettai.domain.ListName
import com.ubertob.fotf.zettai.domain.ToDoItem
import com.ubertob.fotf.zettai.domain.User
import com.ubertob.fotf.zettai.eventsourcing.EntityEvent
import com.ubertob.fotf.zettai.eventsourcing.EntityId
import com.ubertob.fotf.zettai.eventsourcing.EntityState
import java.time.Instant

typealias ToDoListId = EntityId

sealed class ToDoListEvent : EntityEvent
data class ListCreated(override val id: ToDoListId, val owner: User, val name: ListName) : ToDoListEvent()
data class ListRenamed(override val id: ToDoListId, val owner: User, val newName: ListName) : ToDoListEvent()
data class ListPutOnHold(override val id: ToDoListId, val reason: String) : ToDoListEvent()
data class ListReleased(override val id: ToDoListId) : ToDoListEvent()
data class ListClosed(override val id: ToDoListId, val closedOn: Instant) : ToDoListEvent()
data class ItemAdded(override val id: ToDoListId, val item: ToDoItem) : ToDoListEvent()
data class ItemRemoved(override val id: ToDoListId, val item: ToDoItem) : ToDoListEvent()
data class ItemModified(override val id: ToDoListId, val prevItem: ToDoItem, val item: ToDoItem) : ToDoListEvent()


fun Iterable<ToDoListEvent>.fold(): ToDoListState =
    fold(InitialState as ToDoListState) { acc, e -> acc.combine(e) }


sealed class ToDoListState : EntityState<ToDoListEvent> {
    abstract override fun combine(event: ToDoListEvent): ToDoListState

}

object InitialState : ToDoListState() {
    override fun combine(event: ToDoListEvent): ToDoListState =
        when (event) {
            is ListCreated -> create(event.id, event.owner, event.name, emptyList())
            else -> this //ignore other events
        }
}

data class ActiveToDoList internal constructor(
    val id: ToDoListId,
    val owner: User,
    val name: ListName,
    val items: List<ToDoItem>
) :
    ToDoListState() {
    override fun combine(event: ToDoListEvent): ToDoListState =
        when (event) {
            is ItemAdded -> copy(items = items + event.item)
            is ItemRemoved -> copy(items = items - event.item)
            is ItemModified -> copy(items = items - event.prevItem + event.item)
            is ListPutOnHold -> onHold(event.reason)
            is ListClosed -> close(event.closedOn)
            is ListRenamed -> rename(event.newName)
            else -> this //ignore other events
        }
}

data class OnHoldToDoList internal constructor(
    val id: ToDoListId,
    val owner: User,
    val name: ListName,
    val items: List<ToDoItem>,
    val reason: String
) :
    ToDoListState() {
    override fun combine(event: ToDoListEvent): ToDoListState =
        when (event) {
            is ListReleased -> release()
            else -> this //ignore other events
        }
}


data class ClosedToDoList internal constructor(val id: ToDoListId, val closedOn: Instant) : ToDoListState() {
    override fun combine(event: ToDoListEvent): ToDoListState = this //ignore other events

}


fun InitialState.create(id: ToDoListId, owner: User, name: ListName, items: List<ToDoItem>) =
    ActiveToDoList(id, owner, name, items)

fun ActiveToDoList.onHold(reason: String) = OnHoldToDoList(id, owner, name, items, reason)

fun OnHoldToDoList.release() = ActiveToDoList(id, owner, name, items)

fun ActiveToDoList.close(closedOn: Instant) = ClosedToDoList(id, closedOn)
fun ActiveToDoList.rename(newName: ListName) = copy(name = newName)


package com.ubertob.fotf.zettai.queries

import com.ubertob.fotf.zettai.domain.ListName
import com.ubertob.fotf.zettai.domain.ToDoItem
import com.ubertob.fotf.zettai.domain.ToDoList
import com.ubertob.fotf.zettai.domain.User
import com.ubertob.fotf.zettai.events.*
import com.ubertob.fotf.zettai.fp.*

data class ToDoListProjectionRow(val user: User, val active: Boolean, val list: ToDoList) {
    fun addItem(item: ToDoItem): ToDoListProjectionRow =
        copy(list = list.copy(items = list.items + item))

    fun removeItem(item: ToDoItem): ToDoListProjectionRow =
        copy(list = list.copy(items = list.items - item))

    fun replaceItem(prevItem: ToDoItem, item: ToDoItem): ToDoListProjectionRow =
        copy(list = list.copy(items = list.items - prevItem + item))

    fun putOnHold(): ToDoListProjectionRow = copy(active = false)
    fun release(): ToDoListProjectionRow = copy(active = true)
}

class ToDoListProjection(eventFetcher: FetchStoredEvents<ToDoListEvent>) :
    InMemoryProjection<ToDoListProjectionRow, ToDoListEvent> by ConcurrentMapProjection(
        eventFetcher,
        ::eventProjector
    ) {

    fun findAll(user: User): Sequence<ListName> =
        allRows().values
            .asSequence()
            .filter { it.user == user }
            .map { it.list.listName }


    fun findList(user: User, name: ListName): ToDoList? =
        allRows().values
            .firstOrNull { it.user == user && it.list.listName == name }
            ?.list

    fun findAllActiveListId(user: User): List<EntityId> =
        allRows()
            .filter { it.value.user == user && it.value.active }
            .map { ToDoListId.fromRowId(it.key) }


    companion object {
        fun eventProjector(e: ToDoListEvent): List<DeltaRow<ToDoListProjectionRow>> =
            when (e) {
                is ListCreated -> CreateRow(
                    e.rowId(),
                    ToDoListProjectionRow(e.owner, true, ToDoList(e.name, emptyList()))
                )

                is ItemAdded -> UpdateRow<ToDoListProjectionRow>(e.rowId()) { addItem(e.item) }
                is ItemRemoved -> UpdateRow(e.rowId()) { removeItem(e.item) }
                is ItemModified -> UpdateRow(e.rowId()) { replaceItem(e.prevItem, e.item) }
                is ListPutOnHold -> UpdateRow(e.rowId()) { putOnHold() }
                is ListReleased -> UpdateRow(e.rowId()) { release() }
                is ListClosed -> DeleteRow(e.rowId())
            }.toSingle()
    }
}

private fun ToDoListEvent.rowId(): RowId = RowId(id.raw.toString())


package com.ubertob.fotf.zettai.events

import com.ubertob.fotf.zettai.domain.randomItem
import com.ubertob.fotf.zettai.domain.randomListName
import com.ubertob.fotf.zettai.domain.randomUser
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo

internal class ToDoListEventTest {

    val id = ToDoListId.mint()
    val name = randomListName()
    val user = randomUser()
    val item1 = randomItem()
    val item2 = randomItem()
    val item3 = randomItem()

    @Test
    fun `the first event create a list`() {

        val events: List<ToDoListEvent> = listOf(
            ListCreated(id, user, name)
        )

        val list = events.fold()

        expectThat(list).isEqualTo(ActiveToDoList(id, user, name, emptyList()))
    }


    @Test
    fun `adding and removingitems to active list`() {
        val events: List<ToDoListEvent> = listOf(
            ListCreated(id, user, name),
            ItemAdded(id, item1),
            ItemAdded(id, item2),
            ItemAdded(id, item3),
            ItemRemoved(id, item2)
        )

        val list = events.fold()

        expectThat(list).isEqualTo(ActiveToDoList(id, user, name, listOf(item1, item3)))
    }

    @Test
    fun `putting the list on hold`() {
        val reason = "not urgent anymore"
        val events: List<ToDoListEvent> = listOf(
            ListCreated(id, user, name),
            ItemAdded(id, item1),
            ItemAdded(id, item2),
            ItemAdded(id, item3),
            ListPutOnHold(id, reason)
        )

        val list = events.fold()

        expectThat(list).isEqualTo(OnHoldToDoList(id, user, name, listOf(item1, item2, item3), reason))
    }

    @Test
    fun `renaming the list`() {
        val newName = randomListName()
        val events: List<ToDoListEvent> = listOf(
            ListCreated(id, user, name),
            ItemAdded(id, item1),
            ListRenamed(id, user, newName)
        )

        val list = events.fold()

        expectThat(list).isEqualTo(ActiveToDoList(id, user, newName, listOf(item1)))
    }

}
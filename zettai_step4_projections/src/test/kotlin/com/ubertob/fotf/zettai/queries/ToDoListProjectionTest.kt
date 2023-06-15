package com.ubertob.fotf.zettai.queries

import com.ubertob.fotf.zettai.domain.ToDoList
import com.ubertob.fotf.zettai.domain.randomItem
import com.ubertob.fotf.zettai.domain.randomListName
import com.ubertob.fotf.zettai.domain.randomUser
import com.ubertob.fotf.zettai.events.*
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isNotNull

internal class ToDoListProjectionTest {


    @Test
    fun `findAll returns all the lists of a user`() {

        val user = randomUser()
        val listName1 = randomListName()
        val listName2 = randomListName()
        val events = listOf(
            ListCreated(ToDoListId.mint(), user, listName1),
            ListCreated(ToDoListId.mint(), user, listName2),
            ListCreated(ToDoListId.mint(), randomUser(), randomListName())
        )

        val projection = events.buildListProjection()

        expectThat(projection.findAll(user).toList()).isEqualTo(listOf(listName1, listName2))
    }

    @Test
    fun `findList get list with correct items`() {

        val user = randomUser()
        val listName = randomListName()
        val id = ToDoListId.mint()
        val item1 = randomItem()
        val item2 = randomItem()
        val item3 = randomItem()
        val events = listOf(
            ListCreated(id, user, listName),
            ItemAdded(id, item1),
            ItemAdded(id, item2),
            ItemModified(id, item2, item3),
            ItemRemoved(id, item1)
        )

        val projection = events.buildListProjection()

        expectThat(projection.findList(user, listName)).isNotNull()
            .isEqualTo(ToDoList(listName, listOf(item3)))

    }
}

private fun List<ToDoListEvent>.buildListProjection(): ToDoListProjection =
    ToDoListProjection { after ->
        mapIndexed { i, e -> StoredEvent(EventSeq(after.progressive + i + 1), e) }
            .asSequence()
    }.also(ToDoListProjection::update)

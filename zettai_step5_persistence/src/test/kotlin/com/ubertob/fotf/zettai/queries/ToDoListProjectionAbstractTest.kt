package com.ubertob.fotf.zettai.queries

import com.ubertob.fotf.zettai.domain.ToDoList
import com.ubertob.fotf.zettai.domain.randomItem
import com.ubertob.fotf.zettai.domain.randomListName
import com.ubertob.fotf.zettai.domain.randomUser
import com.ubertob.fotf.zettai.domain.tooling.expectSuccess
import com.ubertob.fotf.zettai.events.*
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isNotNull

abstract class ToDoListProjectionAbstractTest {

    abstract fun buildListProjection(events: List<ToDoListEvent>): ToDoListProjection
    val user = randomUser()

    @Test
    fun `findAll returns all the lists of a user`() {

        val listName1 = randomListName()
        val listName2 = randomListName()

        val projection = buildListProjection(
            listOf(
                ListCreated(ToDoListId.mint(), user, listName1),
                ListCreated(ToDoListId.mint(), user, listName2),
                ListCreated(ToDoListId.mint(), randomUser(), randomListName())
            )
        )

        expectThat(projection.findAll(user).expectSuccess()).isEqualTo(listOf(listName1, listName2))
    }

    @Test
    fun `findList get list with correct items`() {

        val listName = randomListName()
        val id = ToDoListId.mint()
        val item1 = randomItem()
        val item2 = randomItem()
        val item3 = randomItem()

        val projection: ToDoListProjection = buildListProjection(
            listOf(
                ListCreated(id, user, listName),
                ItemAdded(id, item1),
                ItemAdded(id, item2),
                ItemModified(id, item2, item3),
                ItemRemoved(id, item1)
            )
        )

        expectThat(projection.findList(user, listName).expectSuccess()).isNotNull()
            .isEqualTo(ToDoList(listName, listOf(item3)))

    }
}
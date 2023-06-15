package com.ubertob.fotf.zettai.domain

import com.ubertob.fotf.zettai.commands.AddToDoItem
import com.ubertob.fotf.zettai.commands.CreateToDoList
import com.ubertob.fotf.zettai.domain.tooling.expectFailure
import com.ubertob.fotf.zettai.domain.tooling.expectSuccess
import com.ubertob.fotf.zettai.events.of
import org.junit.jupiter.api.Test
import strikt.api.expect
import strikt.api.expectThat
import strikt.assertions.isEqualTo

class ToDoListHubTest {

    val hub = prepareToDoListHubInMemory()

    @Test
    fun `get list by user and name`() {
        usersGenerator().take(10).forEach { user ->
            val lists = toDoListsGenerator().take(100).toList()
            lists.forEach { list ->
                hub.handle(CreateToDoList(user, list.listName)).expectSuccess()
                list.items.forEach {
                    hub.handle(AddToDoItem(user, list.listName, it)).expectSuccess()
                }
            }

            lists.forEach { list ->
                val myList = hub.getList(list.listName of user).expectSuccess()
                expectThat(myList).isEqualTo(list)
            }
        }
    }

    @Test
    fun `don't get list from other users`() {
        repeat(10) {
            val firstList = randomToDoList()
            val secondList = randomToDoList()
            val firstUser = randomUser()
            val secondUser = randomUser()

            expect {
                that(hub.getList(secondList.listName of firstUser).expectFailure())
                that(hub.getList(firstList.listName of secondUser).expectFailure())
            }
        }
    }
}


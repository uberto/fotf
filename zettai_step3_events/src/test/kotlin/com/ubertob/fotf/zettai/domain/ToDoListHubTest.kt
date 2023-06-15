package com.ubertob.fotf.zettai.domain

import org.junit.jupiter.api.Test
import strikt.api.expect
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isNull

class ToDoListHubTest {

    val fetcher = ToDoListFetcherFromMap(emptyStore())

    val hub = prepareToDoListHubForTests(fetcher)

    @Test
    fun `get list by user and name`() {
        usersGenerator().take(10).forEach { user ->
            toDoListsGenerator().take(100).forEach { list ->
                fetcher.assignListToUser(user, list)

                val myList = hub.getList(user, list.listName)

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

            fetcher.assignListToUser(firstUser, firstList)
            fetcher.assignListToUser(secondUser, secondList)

            expect {
                that(hub.getList(firstUser, secondList.listName)).isNull()
                that(hub.getList(secondUser, firstList.listName)).isNull()
            }
        }
    }
}


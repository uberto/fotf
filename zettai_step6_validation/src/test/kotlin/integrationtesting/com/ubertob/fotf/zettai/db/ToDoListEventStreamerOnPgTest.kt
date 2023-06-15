package integrationtesting.com.ubertob.fotf.zettai.db

import com.ubertob.fotf.zettai.db.createToDoListEventStreamerOnPg
import com.ubertob.fotf.zettai.domain.randomItem
import com.ubertob.fotf.zettai.domain.randomListName
import com.ubertob.fotf.zettai.domain.randomUser
import com.ubertob.fotf.zettai.domain.tooling.expectSuccess
import com.ubertob.fotf.zettai.events.*
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.hasSize
import strikt.assertions.isEqualTo
import strikt.assertions.isNull
import java.time.LocalDate


class ToDoListEventStreamerOnPgTest {

    val user = randomUser()

    val streamer = createToDoListEventStreamerOnPg()

    private val transactionProvider = transactionProviderForTest()

    @Test
    fun `store some events and then fetch them by entity`() {

        val newList1 = randomListName()
        val newList2 = randomListName()
        val newList3 = randomListName()
        val listId1 = ToDoListId.mint()
        val listId2 = ToDoListId.mint()
        val listId3 = ToDoListId.mint()
        val item1 = randomItem()
        val item2 = randomItem().copy(dueDate = LocalDate.now())

        val eventsToStore = listOf(
            ListCreated(listId1, user, newList1),
            ListCreated(listId2, user, newList2),
            ItemAdded(listId2, item1),
            ItemAdded(listId2, item2),
            ListCreated(listId3, user, newList3)
        )

        val storeAndFetch = streamer.store(
            eventsToStore
        ).bind {
            streamer.fetchByEntity(listId2)
        }

        val events = transactionProvider.tryRun(storeAndFetch).expectSuccess()

        expectThat(events).isEqualTo(
            listOf(
                ListCreated(listId2, user, newList2),
                ItemAdded(listId2, item1),
                ItemAdded(listId2, item2)
            )
        )
    }


    @Test
    fun `store some events and then fetch them by eventId`() {

        val newList1 = randomListName()
        val newList2 = randomListName()
        val newList3 = randomListName()
        val listId1 = ToDoListId.mint()
        val listId2 = ToDoListId.mint()
        val listId3 = ToDoListId.mint()

        val newEvents = listOf(
            ListCreated(listId1, user, newList1),
            ListCreated(listId2, user, newList2),
            ListCreated(listId3, user, newList3)
        )
        val events = transactionProvider.tryRun(
            streamer.store(
                newEvents
            ).bind { ids ->
                streamer.fetchAfter(ids.first().eventSeq)
            }
        ).expectSuccess()

        expectThat(events).hasSize(2)
        expectThat(events[0].event).isEqualTo(newEvents[1])
        expectThat(events[1].event).isEqualTo(newEvents[2])

    }


    @Test
    fun `store some events and then fetch them by user and list name`() {

        val newList1 = randomListName()
        val newList2 = randomListName()
        val newList3 = randomListName()
        val listId1 = ToDoListId.mint()
        val listId2 = ToDoListId.mint()
        val listId3 = ToDoListId.mint()

        val eventsToStore = listOf(
            ListCreated(listId1, user, newList1),
            ListCreated(listId2, user, newList2),
            ListCreated(listId3, user, newList3)
        )
        val listId = transactionProvider.tryRun(
            streamer.store(
                eventsToStore
            ).bind {
                streamer.retrieveIdFromNaturalKey(UserListName(user, newList3))
            }
        ).expectSuccess()

        expectThat(listId).isEqualTo(listId3)

        val listNotPresent =
            transactionProvider.tryRun(
                streamer.retrieveIdFromNaturalKey(UserListName(randomUser(), randomListName()))
            ).expectSuccess()

        expectThat(listNotPresent).isNull()

    }

    @Test
    fun `RetrieveIdFromNaturalKey considers only the most recent name of a list`() {

        val name = randomListName()
        val newName = randomListName()
        val newNewName = randomListName()
        val listId = ToDoListId.mint()

        val eventsToStore = listOf(
            ListCreated(listId, user, name),
            ListRenamed(listId, user, newName),
            ListRenamed(listId, user, newNewName)
        )

        transactionProvider.doRun {
            +streamer.store(eventsToStore)

            expectThat(
                +streamer.retrieveIdFromNaturalKey(UserListName(user, name))
            ).isNull()

            expectThat(
                +streamer.retrieveIdFromNaturalKey(UserListName(user, newName))
            ).isNull()

            expectThat(
                +streamer.retrieveIdFromNaturalKey(UserListName(user, newNewName))
            ).isEqualTo(listId)
        }.expectSuccess()
    }

    @Test
    fun `we can rename a list using an old name of another list`() {

        val nameList1 = randomListName()
        val nameList2 = randomListName()
        val nameList3 = randomListName()
        val listId1 = ToDoListId.mint()
        val listId2 = ToDoListId.mint()

        val eventsToStore = listOf(
            ListCreated(listId1, user, nameList1),
            ListCreated(listId2, user, nameList2),
            ListRenamed(listId1, user, nameList3), //now list1 is renamed list3
            ListRenamed(listId2, user, nameList1), //so I can rename list2 as list1
            ListRenamed(listId1, user, nameList2) //and finally list1 as list2
        )

        transactionProvider.doRun {
            +streamer.store(eventsToStore)

            expectThat(
                +streamer.retrieveIdFromNaturalKey(UserListName(user, nameList3))
            ).isNull()

            expectThat(
                +streamer.retrieveIdFromNaturalKey(UserListName(user, nameList2))
            ).isEqualTo(listId1)

            expectThat(
                +streamer.retrieveIdFromNaturalKey(UserListName(user, nameList1))
            ).isEqualTo(listId2)
        }.expectSuccess()
    }


}


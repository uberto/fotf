package ddt.com.ubertob.fotf.zettai.tooling

import com.ubertob.fotf.zettai.domain.*
import com.ubertob.fotf.zettai.domain.tooling.expectSuccess
import com.ubertob.pesticide.core.DdtActor
import strikt.api.Assertion
import strikt.api.expectThat
import strikt.assertions.*
import java.time.LocalDate

data class ToDoListOwner(override val name: String) : DdtActor<ZettaiActions>() {

    val user = User(name)

    fun `can see #listname with #itemnames`(listName: String, expectedItems: List<String>) =
        step(listName, expectedItems) {

            val list = getToDoList(user, ListName.fromUntrustedOrThrow(listName)).expectSuccess()
            expectThat(list)
                .itemNames
                .containsExactlyInAnyOrder(expectedItems)
        }

    fun `can see #itemName of #listName with status #expectedStatus`(
        itemName: String,
        listName: String,
        newStatus: ToDoStatus
    ) =
        step(itemName, listName, newStatus.name) {

            val list = getToDoList(user, ListName.fromUntrustedOrThrow(listName)).expectSuccess()
            val status = list.items.single { it.description == itemName }.status
            expectThat(status)
                .isEqualTo(newStatus)
        }

    fun `cannot see #listname`(listName: String) = step(listName) {
        val lists = allUserLists(user).expectSuccess()
        expectThat(lists.map { it.name }).doesNotContain(listName)
    }

    fun `cannot see any list`() = step {
        val lists = allUserLists(user).expectSuccess()
        expectThat(lists)
            .isEmpty()
    }

    fun `can see the lists #listNames`(expectedLists: Set<String>) = step(expectedLists) {
        val lists = allUserLists(user).expectSuccess()
        expectThat(lists)
            .map(ListName::name)
            .containsExactlyInAnyOrder(expectedLists)
    }

    fun `can create a new list called #listname`(listName: String) = step(listName) {
        with(this) {
            `starts with a list`(listName, emptyList())
        }
    }

    fun `can add #item to the #listname`(itemName: String, listName: String) = step(itemName, listName) {
        val item = ToDoItem(itemName)
        addListItem(user, ListName.fromUntrustedOrThrow(listName), item)
    }

    fun `can see that #itemname is the next task to do`(itemName: String) = step(itemName) {

        val items = whatsNext(user).expectSuccess()

        expectThat(items.firstOrNull()?.description.orEmpty()).isEqualTo(itemName)
    }

    fun `can add #itemname to the #listname due to #duedate`(itemName: String, listName: String, dueDate: LocalDate) =
        step(itemName, listName, dueDate) {
            val item = ToDoItem(itemName, dueDate)
            addListItem(user, ListName.fromUntrustedOrThrow(listName), item)
        }

    fun `can rename the list #oldname as #newname`(origListName: String, newListName: String) =
        step(origListName, newListName) {
            renameList(user, ListName.fromUntrustedOrThrow(origListName), ListName.fromUntrustedOrThrow(newListName))
        }

    fun `can change #item from #listname to #newname`(origItemName: String, listName: String, newItemName: String) =
        step(origItemName, listName, newItemName) {
            val list = getToDoList(user, ListName.fromUntrustedOrThrow(listName)).expectSuccess()
            val oldItem = list.items.single { it.description == origItemName }
            val newItem = oldItem.copy(description = newItemName)
            updateListItem(user, list.listName, origItemName, newItem)
        }

    fun `can change #item from #listname to #newStatus`(itemName: String, listName: String, newItemStatus: ToDoStatus) =
        step(itemName, listName, newItemStatus.name) {
            val list = getToDoList(user, ListName.fromUntrustedOrThrow(listName)).expectSuccess()
            val oldItem = list.items.single { it.description == itemName }
            val newItem = oldItem.copy(status = newItemStatus)
            updateListItem(user, list.listName, itemName, newItem)
        }

    fun `can delete #item from #listname`(itemName: String, listName: String) =
        step(itemName, listName) {
            deleteListItem(user, ListName.fromUntrustedOrThrow(listName), itemName)
        }

    private val Assertion.Builder<ToDoList>.itemNames: Assertion.Builder<List<String>>
        get() = get { items.map { it.description } }


}

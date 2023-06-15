package ddt.com.ubertob.fotf.zettai.tooling

import com.ubertob.fotf.zettai.domain.ListName
import com.ubertob.fotf.zettai.domain.ToDoItem
import com.ubertob.fotf.zettai.domain.ToDoList
import com.ubertob.fotf.zettai.domain.User
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
            .containsExactly(expectedLists)
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

    private val Assertion.Builder<ToDoList>.itemNames: Assertion.Builder<List<String>>
        get() = get { items.map { it.description } }


}

/*
todo
 */
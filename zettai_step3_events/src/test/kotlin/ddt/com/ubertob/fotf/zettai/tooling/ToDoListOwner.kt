package ddt.com.ubertob.fotf.zettai.tooling

import com.ubertob.fotf.zettai.domain.ListName
import com.ubertob.fotf.zettai.domain.ToDoItem
import com.ubertob.fotf.zettai.domain.ToDoList
import com.ubertob.fotf.zettai.domain.User
import com.ubertob.pesticide.core.DdtActor
import strikt.api.Assertion
import strikt.api.expectThat
import strikt.assertions.*

data class ToDoListOwner(override val name: String) : DdtActor<ZettaiActions>() {

    val user = User(name)

    fun `can see #listname with #itemnames`(listName: String, expectedItems: List<String>) =
        step(listName, expectedItems) {
            val list = getToDoList(user, ListName.fromUntrustedOrThrow(listName))
            expectThat(list)
                .isNotNull()
                .itemNames
                .containsExactlyInAnyOrder(expectedItems)
        }

    fun `cannot see #listname`(listName: String) = step(listName) {
        val list = getToDoList(user, ListName.fromUntrustedOrThrow(listName))
        expectThat(list).isNull()
    }

    fun `cannot see any list`() = step {
        val lists = allUserLists(user)
        expectThat(lists)
            .isEmpty()
    }

    fun `can see the lists #listNames`(expectedLists: Set<String>) = step(expectedLists) {
        val lists = allUserLists(user)
        expectThat(lists)
            .map(ListName::name)
            .containsExactly(expectedLists)
    }

    fun `can create a new list called #listname`(listName: String) = step(listName) {
        createList(user, ListName.fromUntrustedOrThrow(listName))
    }

    fun `can add #item to the #listname`(itemName: String, listName: String) = step(itemName, listName) {
        val item = ToDoItem(itemName)
        addListItem(user, ListName.fromUntrustedOrThrow(listName), item)
    }

    private val Assertion.Builder<ToDoList>.itemNames: Assertion.Builder<List<String>>
        get() = get { items.map { it.description } }


}

/*
todo
 */
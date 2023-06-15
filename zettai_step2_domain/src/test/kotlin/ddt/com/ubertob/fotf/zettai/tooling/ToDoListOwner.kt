package ddt.com.ubertob.fotf.zettai.tooling

import com.ubertob.fotf.zettai.domain.ListName
import com.ubertob.fotf.zettai.domain.ToDoItem
import com.ubertob.fotf.zettai.domain.User
import com.ubertob.pesticide.core.DdtActor
import strikt.api.expectThat
import strikt.assertions.containsExactlyInAnyOrder
import strikt.assertions.isNotNull
import strikt.assertions.isNull

data class ToDoListOwner(override val name: String) : DdtActor<ZettaiActions>() {

    val user = User(name)

    fun `can add #item to #listname`(itemName: String, listName: String) = step(itemName, listName) {
        val item = ToDoItem(itemName)
        addListItem(user, ListName.fromUntrustedOrThrow(listName), item)
    }

    fun `can see #listname with #itemnames`(listName: String, expectedItems: List<String>) =
        step(listName, expectedItems) {
            val list = getToDoList(user, ListName.fromUntrustedOrThrow(listName))
            expectThat(list)
                .isNotNull()
                .get { items.map { it.description } }
                .containsExactlyInAnyOrder(expectedItems)
        }

    fun `cannot see #listname`(listName: String) = step(listName) {
        val list = getToDoList(user, ListName.fromUntrustedOrThrow(listName))
        expectThat(list).isNull()
    }


}
package ddt.com.ubertob.fotf.zettai.tooling

import com.ubertob.fotf.zettai.commands.AddToDoItem
import com.ubertob.fotf.zettai.commands.CreateToDoList
import com.ubertob.fotf.zettai.domain.*
import com.ubertob.fotf.zettai.domain.tooling.expectSuccess
import com.ubertob.pesticide.core.DdtProtocol
import com.ubertob.pesticide.core.DomainOnly
import com.ubertob.pesticide.core.Ready
import strikt.api.expectThat
import strikt.assertions.hasSize

class DomainOnlyActions : ZettaiActions {
    override val protocol: DdtProtocol = DomainOnly
    override fun prepare() = Ready


    private val hub = prepareToDoListHubForTests()


    override fun getToDoList(user: User, listName: ListName): ZettaiOutcome<ToDoList> =
        hub.getList(user, listName)


    override fun addListItem(user: User, listName: ListName, item: ToDoItem) {
        hub.handle(AddToDoItem(user, listName, item))
    }

    override fun allUserLists(user: User): ZettaiOutcome<List<ListName>> =
        hub.getLists(user)

//    override fun createList(user: User, listName: ListName, items: List<String>) {
//        hub.handle(CreateToDoList(user, listName))
//    }

    override fun whatsNext(user: User): ZettaiOutcome<List<ToDoItem>> =
        hub.whatsNext(user)

    override fun ToDoListOwner.`starts with a list`(listName: String, items: List<String>) {
        val list = ListName.fromTrusted(listName)
        hub.handle(
            CreateToDoList(
                user,
                list
            )
        ).expectSuccess()

        val events = items.map {
            hub.handle(
                AddToDoItem(
                    user,
                    list,
                    ToDoItem(it)
                )
            ).expectSuccess()
        }

        expectThat(events).hasSize(items.size)
    }


}



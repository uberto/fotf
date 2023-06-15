package ddt.com.ubertob.fotf.zettai.tooling

import com.ubertob.fotf.zettai.domain.ListName
import com.ubertob.fotf.zettai.domain.ToDoItem
import com.ubertob.fotf.zettai.domain.ToDoList
import com.ubertob.fotf.zettai.domain.User
import com.ubertob.pesticide.core.DdtActions
import com.ubertob.pesticide.core.DdtProtocol
import com.ubertob.pesticide.core.DomainDrivenTest


interface ZettaiActions : DdtActions<DdtProtocol> {
    fun ToDoListOwner.`starts with a list`(listName: String, items: List<String>)
    fun ToDoListOwner.`starts with some lists`(lists: Map<String, List<String>>) =
        lists.forEach { (listName, items) ->
            `starts with a list`(listName, items)
        }


    fun getToDoList(user: User, listName: ListName): ToDoList?
    fun addListItem(user: User, listName: ListName, item: ToDoItem)
    fun allUserLists(user: User): List<ListName>
    fun createList(user: User, listName: ListName)
}

typealias ZettaiDDT = DomainDrivenTest<ZettaiActions>

fun allActions() = setOf(
    DomainOnlyActions(),
    HttpActions()
)




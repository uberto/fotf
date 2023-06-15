package ddt.com.ubertob.fotf.zettai.stories

import com.ubertob.pesticide.core.DDT
import ddt.com.ubertob.fotf.zettai.tooling.ToDoListOwner
import ddt.com.ubertob.fotf.zettai.tooling.ZettaiDDT
import ddt.com.ubertob.fotf.zettai.tooling.allActions

class SeeATodoListDDT : ZettaiDDT(allActions()) {

    val frank by NamedActor(::ToDoListOwner)
    val bob by NamedActor(::ToDoListOwner)

    val shoppingListName = "shopping"
    val shoppingItems = listOf("carrots", "apples", "milk")

    val gardenListName = "gardening"
    val gardenItems = listOf("fix the fence", "mowing the lawn")

    @DDT
    fun `List owners can see their lists`() = ddtScenario {

        setUp {
            frank.`starts with a list`(shoppingListName, shoppingItems)
            bob.`starts with a list`(gardenListName, gardenItems)
        }.thenPlay(
            frank.`can see #listname with #itemnames`(shoppingListName, shoppingItems),
            bob.`can see #listname with #itemnames`(gardenListName, gardenItems)
        )

    }

    @DDT
    fun `Only owners can see their lists`() = ddtScenario {

        setUp {
            frank.`starts with a list`(shoppingListName, shoppingItems)
            bob.`starts with a list`(gardenListName, gardenItems)
        }.thenPlay(
            frank.`cannot see #listname`(gardenListName),
            bob.`cannot see #listname`(shoppingListName)
        )

    }

}

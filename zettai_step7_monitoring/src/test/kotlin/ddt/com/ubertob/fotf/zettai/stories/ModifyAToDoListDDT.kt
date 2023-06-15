package ddt.com.ubertob.fotf.zettai.stories

import com.ubertob.fotf.zettai.domain.ToDoStatus
import com.ubertob.pesticide.core.DDT
import ddt.com.ubertob.fotf.zettai.tooling.ToDoListOwner
import ddt.com.ubertob.fotf.zettai.tooling.ZettaiDDT
import ddt.com.ubertob.fotf.zettai.tooling.allActions

class ModifyAToDoListDDT : ZettaiDDT(allActions()) {

    val ann by NamedActor(::ToDoListOwner)
    val ben by NamedActor(::ToDoListOwner)

    @DDT
    fun `users can create a new list`() = ddtScenario {
        play(
            ann.`can create a new list called #listname`("myfirstlist"),
            ann.`can see #listname with #itemnames`(
                "myfirstlist", emptyList()
            )
        )
    }

    @DDT
    fun `the list owner can add new items`() = ddtScenario {
        setUp {
            ann.`starts with a list`("diy", emptyList())
        }.thenPlay(
            ann.`can add #item to the #listname`("paint the shelf", "diy"),
            ann.`can add #item to the #listname`("fix the gate", "diy"),
            ann.`can add #item to the #listname`("change the lock", "diy"),
            ann.`can see #listname with #itemnames`(
                "diy", listOf(
                    "fix the gate", "paint the shelf", "change the lock"
                )
            )
        )
    }

    @DDT
    fun `the list owner can rename a list`() = ddtScenario {
        setUp {
            ben.`starts with a list`("shopping", emptyList())
        }.thenPlay(
            ben.`can add #item to the #listname`("carrots", "shopping"),
            ben.`can rename the list #oldname as #newname`(
                origListName = "shopping",
                newListName = "grocery"
            ),
            ben.`can add #item to the #listname`("potatoes", "grocery"),
            ben.`can see #listname with #itemnames`(
                "grocery", listOf("carrots", "potatoes")
            )
        )
    }

    @DDT
    fun `the list owner can delete items`() = ddtScenario {
        setUp {
            ben.`starts with a list`("fitness", listOf("check gym prices", "subscribe to the gym", "going to the gym"))
        }.thenPlay(
            ben.`can delete #item from #listname`("going to the gym", "fitness"),
            ben.`can delete #item from #listname`("check gym prices", "fitness"),
            ben.`can see #listname with #itemnames`(
                "fitness", listOf(
                    "subscribe to the gym"
                )
            )
        )
    }


    @DDT
    fun `the list owner can modify item descriptions`() = ddtScenario {
        setUp {
            ben.`starts with a list`("sports", listOf("tennis", "squash"))
        }.thenPlay(
            ben.`can change #item from #listname to #newname`("squash", "sports", "skiing"),
            ben.`can see #listname with #itemnames`(
                "sports", listOf(
                    "tennis", "skiing"
                )
            )
        )
    }

    @DDT
    fun `the list owner can mark an item as done`() = ddtScenario {
        setUp {
            ann.`starts with a list`("travels", listOf("Italy", "Spain"))
        }.thenPlay(
            ann.`can see #itemName of #listName with status #expectedStatus`("Spain", "travels", ToDoStatus.Todo),
            ann.`can see #itemName of #listName with status #expectedStatus`("Italy", "travels", ToDoStatus.Todo),
            ann.`can change #item from #listname to #newStatus`("Italy", "travels", ToDoStatus.InProgress),
            ann.`can see #itemName of #listName with status #expectedStatus`("Italy", "travels", ToDoStatus.InProgress),
            ann.`can change #item from #listname to #newStatus`("Italy", "travels", ToDoStatus.Done),
            ann.`can see #itemName of #listName with status #expectedStatus`("Italy", "travels", ToDoStatus.Done)
        )
    }

}
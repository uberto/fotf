package com.ubertob.fotf.zettai.domain

import com.ubertob.fotf.zettai.commands.ToDoListCommand
import com.ubertob.fotf.zettai.db.fp.ContextError
import com.ubertob.fotf.zettai.events.ToDoListEvent
import com.ubertob.fotf.zettai.events.ToDoListState
import com.ubertob.fotf.zettai.fp.CommandHandler
import com.ubertob.fotf.zettai.fp.Outcome
import com.ubertob.fotf.zettai.fp.OutcomeError
import com.ubertob.fotf.zettai.fp.bind
import com.ubertob.fotf.zettai.queries.ItemProjectionRow
import com.ubertob.fotf.zettai.queries.ToDoListQueryRunner


sealed class ZettaiError : OutcomeError
data class InvalidRequestError(override val msg: String) : ZettaiError()
data class ToDoListCommandError(override val msg: String) : ZettaiError()
data class InconsistentStateError(val command: ToDoListCommand, val state: ToDoListState) : ZettaiError() {
    override val msg = "Command $command cannot be applied to state $state"
}

data class QueryError(override val msg: String, override val exception: Throwable? = null) : ZettaiError(),
    ContextError


data class ZettaiParsingError(override val msg: String) : ZettaiError()

typealias ZettaiOutcome<T> = Outcome<ZettaiError, T>

interface ZettaiHub {
    fun handle(command: ToDoListCommand): ZettaiOutcome<ToDoListCommand>
    fun getList(user: User, listName: ListName): ZettaiOutcome<ToDoList?>
    fun getLists(user: User): ZettaiOutcome<List<ListName>>
    fun whatsNext(user: User): Outcome<ZettaiError, List<ToDoItem>>
}


class ToDoListHub(
    val queryRunner: ToDoListQueryRunner,
    val commandHandler: CommandHandler<ToDoListCommand, ToDoListEvent, ZettaiError>
) : ZettaiHub {

    override fun handle(command: ToDoListCommand): ZettaiOutcome<ToDoListCommand> =
        commandHandler(command).transform { command }

    override fun getList(user: User, listName: ListName): ZettaiOutcome<ToDoList?> =
        queryRunner {
            listProjection.findList(user, listName)
        }

    override fun getLists(user: User): ZettaiOutcome<List<ListName>> =
        queryRunner {
            listProjection.findAll(user)
        }

    override fun whatsNext(user: User): Outcome<ZettaiError, List<ToDoItem>> =
        queryRunner {
            listProjection.findAllActiveListId(user)
                .bind { itemProjection.findWhatsNext(10, it) }
                .transform { it.map(ItemProjectionRow::item) }
        }
}


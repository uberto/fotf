package com.ubertob.fotf.zettai.logger

import com.ubertob.fotf.zettai.commands.ToDoListCommand
import com.ubertob.fotf.zettai.domain.*
import com.ubertob.fotf.zettai.events.UserListName
import com.ubertob.fotf.zettai.fp.Outcome
import com.ubertob.fotf.zettai.fp.asFailure
import com.ubertob.fotf.zettai.fp.asSuccess
import com.ubertob.fotf.zettai.webserver.Zettai
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.body.form
import org.junit.jupiter.api.Test
import strikt.api.expect
import strikt.assertions.isEqualTo
import strikt.assertions.startsWith
import java.io.ByteArrayOutputStream
import kotlin.random.Random


internal class LoggerTest {

    val failingHub = object : ZettaiHub {
        override fun <C : ToDoListCommand> handle(command: C): ZettaiOutcome<C> =
            InvalidRequestError("failing test").asFailure()

        override fun getList(userListName: UserListName): ZettaiOutcome<ToDoList> =
            InvalidRequestError("failing test").asFailure()

        override fun getLists(user: User): ZettaiOutcome<List<ListName>> =
            InvalidRequestError("failing test").asFailure()

        override fun whatsNext(user: User): Outcome<ZettaiError, List<ToDoItem>> =
            InvalidRequestError("failing test").asFailure()

    }

    val happyHub = object : ZettaiHub {
        override fun <C : ToDoListCommand> handle(command: C): ZettaiOutcome<C> =
            command.asSuccess()

        override fun getList(userListName: UserListName): ZettaiOutcome<ToDoList> =
            randomToDoList().asSuccess()

        override fun getLists(user: User): ZettaiOutcome<List<ListName>> =
            listOf(randomListName()).asSuccess()

        override fun whatsNext(user: User): Outcome<ZettaiError, List<ToDoItem>> =
            randomToDoList().items.asSuccess()

    }

    val outputStream = ByteArrayOutputStream()
    val logger = SimpleLogger(outputStream)
    val logs by lazy { outputStream.toString().trim().split('\n') }

    @Test
    fun `commands log when there is a failure`() {
        Zettai(failingHub, logger)
            .httpHandler(Request(Method.POST, "/todo/AUser"))

        expect {
            that(logs.size).isEqualTo(1)
            that(logs[0]).startsWith("error!")
        }
    }


    @Test
    fun `queries log when there is a failure`() {
        Zettai(failingHub, logger)
            .httpHandler(Request(Method.GET, "/todo/AUser"))

        expect {
            that(logs.size).isEqualTo(1)
            that(logs[0]).startsWith("error!")
        }
    }

    @Test
    fun `commands always log successful calls`() {
        val times = Random.nextInt(5, 20)
        val zettai = Zettai(happyHub, logger)
        repeat(times) {
            zettai.httpHandler(
                Request(Method.POST, "/todo/AUser")
                    .form("listname", "newList")
            )
        }
        expect {
            that(logs.size).isEqualTo(times)
            that(logs[0]).startsWith("success!")
        }
    }


    @Test
    fun `queries always log successful calls`() {
        val times = Random.nextInt(5, 20)
        val zettai = Zettai(happyHub, logger)
        repeat(times) {
            zettai
                .httpHandler(Request(Method.GET, "/todo/AUser"))
        }
        expect {
            that(logs.size).isEqualTo(times)
            logs.forEach {
                that(it).startsWith("success!")
            }
        }
    }

}
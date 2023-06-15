package com.ubertob.fotf.zettai.webserver

import com.ubertob.fotf.zettai.commands.AddToDoItem
import com.ubertob.fotf.zettai.commands.CreateToDoList
import com.ubertob.fotf.zettai.domain.*
import com.ubertob.fotf.zettai.fp.*
import com.ubertob.fotf.zettai.ui.HtmlPage
import com.ubertob.fotf.zettai.ui.renderListPage
import com.ubertob.fotf.zettai.ui.renderListsPage
import com.ubertob.fotf.zettai.ui.renderWhatsNextPage
import org.http4k.core.*
import org.http4k.core.body.form
import org.http4k.routing.bind
import org.http4k.routing.path
import org.http4k.routing.routes
import java.time.LocalDate


class Zettai(val hub: ZettaiHub) : HttpHandler {
    override fun invoke(request: Request): Response = httpHandler(request)

    val httpHandler = routes(
        "/ping" bind Method.GET to { Response(Status.OK).body("pong") },
        "/todo/{user}/{listname}" bind Method.GET to ::getToDoList,
        "/todo/{user}/{listname}" bind Method.POST to ::addNewItem,
        "/todo/{user}" bind Method.GET to ::getAllLists,
        "/todo/{user}" bind Method.POST to ::createNewList,
        "/whatsnext/{user}" bind Method.GET to ::whatsNext
    )

    private fun createNewList(request: Request): Response {
        val user = request.extractUser().recover { User("anonymous") }
        val listName = request.form("listname")
            ?.let(ListName.Companion::fromUntrusted)
            ?: return Response(Status.BAD_REQUEST).body("missing listname in form")

        return hub.handle(CreateToDoList(user, listName))
            .transform { Response(Status.SEE_OTHER).header("Location", "/todo/${user.name}") }
            .recover { Response(Status.UNPROCESSABLE_ENTITY).body(it.msg) }

    }

    private fun addNewItem(request: Request): Response {
        val user = request.extractUser().recover { User("anonymous") }
        val listName = request.extractListName().onFailure { return Response(Status.BAD_REQUEST).body(it.msg) }
        return request.extractItem()
            .transform { AddToDoItem(user, listName, it) }
            .bind(hub::handle)
            .transform { Response(Status.SEE_OTHER).header("Location", "/todo/${user.name}/${listName.name}") }
            .recover { Response(Status.UNPROCESSABLE_ENTITY).body(it.msg) }
    }

    private fun getToDoList(request: Request): Response {
        val user = request.extractUser().onFailure { return Response(Status.BAD_REQUEST).body(it.msg) }
        val listName = request.extractListName().onFailure { return Response(Status.BAD_REQUEST).body(it.msg) }

        return hub.getList(user, listName)
            .failIfNull(InvalidRequestError("List $listName not found!"))
            .transform { renderListPage(user, it) }
            .transform(::toResponse)
            .recover { Response(Status.NOT_FOUND).body(it.msg) }
    }

    fun toResponse(htmlPage: HtmlPage): Response =
        Response(Status.OK).body(htmlPage.raw)

    private fun getAllLists(req: Request): Response {
        val user = req.extractUser().onFailure { return Response(Status.BAD_REQUEST).body(it.msg) }

        return hub.getLists(user)
            .transform { renderListsPage(user, it) }
            .transform(::toResponse)
            .recover { Response(Status.NOT_FOUND).body(it.msg) }
    }

    private fun whatsNext(req: Request): Response {
        val user = req.extractUser().onFailure { return Response(Status.BAD_REQUEST).body(it.msg) }

        return hub.whatsNext(user)
            .transform { renderWhatsNextPage(user, it) }
            .transform(::toResponse)
            .recover { Response(Status.NOT_FOUND).body(it.msg) }
    }

    private fun Request.extractUser(): ZettaiOutcome<User> =
        path("user")
            .failIfNull(InvalidRequestError("User not present"))
            .transform(::User)

    private fun Request.extractListName(): ZettaiOutcome<ListName> =
        path("listname")
            .orEmpty()
            .let(ListName.Companion::fromUntrusted)
            .failIfNull(InvalidRequestError("Invalid list name in path: $this"))


    private fun Request.extractItem(): ZettaiOutcome<ToDoItem> {
        val duedate = tryOrNull { LocalDate.parse(form("itemdue")) }
        return form("itemname")
            .failIfNull(InvalidRequestError("User not present"))
            .transform { ToDoItem(it, duedate) }
    }

}





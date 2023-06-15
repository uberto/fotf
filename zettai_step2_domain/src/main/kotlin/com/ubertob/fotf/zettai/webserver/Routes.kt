package com.ubertob.fotf.zettai.webserver

import com.ubertob.fotf.zettai.domain.ListName
import com.ubertob.fotf.zettai.domain.ToDoItem
import com.ubertob.fotf.zettai.domain.User
import com.ubertob.fotf.zettai.domain.ZettaiHub
import com.ubertob.fotf.zettai.ui.HtmlPage
import com.ubertob.fotf.zettai.ui.renderPage
import org.http4k.core.*
import org.http4k.core.body.form
import org.http4k.routing.bind
import org.http4k.routing.path
import org.http4k.routing.routes


class Zettai(val hub: ZettaiHub) : HttpHandler {

    val httpHandler = routes(
        "/ping" bind Method.GET to { Response(Status.OK) },
        "/todo/{user}/{listname}" bind Method.GET to ::getToDoList,
        "/todo/{user}/{listname}" bind Method.POST to ::addNewItem
    )

    override fun invoke(request: Request): Response = httpHandler(request)

    fun toResponse(htmlPage: HtmlPage): Response =
        Response(Status.OK).body(htmlPage.raw)

    private fun getToDoList(request: Request): Response {
        val user = request.path("user").orEmpty().let(::User)
        val listName = request.path("listname").orEmpty().let(ListName.Companion::fromUntrusted)

        return listName
            ?.let { hub.getList(user, it) }
            ?.let(::renderPage)
            ?.let(::toResponse)
            ?: Response(Status.NOT_FOUND)
    }

    private fun addNewItem(request: Request): Response {
        val user = request.path("user")
            ?.let(::User)
            ?: return Response(Status.BAD_REQUEST)
        val listName = request.path("listname")
            ?.let(ListName.Companion::fromUntrusted)
            ?: return Response(Status.BAD_REQUEST)
        val item = request.form("itemname")
            ?.let { ToDoItem(it) }
            ?: return Response(Status.BAD_REQUEST)

        return hub.addItemToList(user, listName, item)
            ?.let { Response(Status.SEE_OTHER).header("Location", "/todo/${user.name}/${listName.name}") }
            ?: Response(Status.NOT_FOUND)
    }

}



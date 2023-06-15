package com.ubertob.fotf.zettai.webservice

import com.ubertob.fotf.zettai.domain.ListName
import com.ubertob.fotf.zettai.domain.ToDoList
import com.ubertob.fotf.zettai.domain.User
import com.ubertob.fotf.zettai.ui.HtmlPage
import com.ubertob.fotf.zettai.ui.renderHtml
import org.http4k.core.*
import org.http4k.routing.bind
import org.http4k.routing.path
import org.http4k.routing.routes

data class Zettai(val lists: Map<User, List<ToDoList>>) : HttpHandler {

    val routes = routes(
        "/todo/{user}/{list}" bind Method.GET to ::getToDoList
    )

    override fun invoke(request: Request): Response =
        routes(request)

    private fun getToDoList(request: Request): Response =
        request.let(::extractListData)
            .let(::fetchListContent)
            .let(::renderHtml)
            .let(::createResponse)


    fun extractListData(request: Request): Pair<User, ListName> {
        val user = request.path("user") ?: error("User missing")
        val list = request.path("list") ?: error("List missing")
        return User(user) to ListName(list)
    }

    fun fetchListContent(listId: Pair<User, ListName>): ToDoList =
        lists[listId.first]
            ?.firstOrNull { it.listName == listId.second }
            ?: error("List unknown")

    fun createResponse(html: HtmlPage): Response = Response(Status.OK).body(html.raw)

}
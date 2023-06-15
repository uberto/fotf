package com.ubertob.fotf.zettai.webserver

import com.ubertob.fotf.zettai.commands.AddToDoItem
import com.ubertob.fotf.zettai.commands.CreateToDoList
import com.ubertob.fotf.zettai.commands.RenameToDoList
import com.ubertob.fotf.zettai.commands.ToDoListCommand
import com.ubertob.fotf.zettai.domain.*
import com.ubertob.fotf.zettai.events.UserListName
import com.ubertob.fotf.zettai.fp.*
import com.ubertob.fotf.zettai.ui.*
import org.http4k.core.HttpHandler
import org.http4k.core.Method.GET
import org.http4k.core.Method.POST
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status.Companion.BAD_REQUEST
import org.http4k.core.Status.Companion.NOT_FOUND
import org.http4k.core.Status.Companion.OK
import org.http4k.core.Status.Companion.SEE_OTHER
import org.http4k.core.Status.Companion.UNPROCESSABLE_ENTITY
import org.http4k.core.body.form
import org.http4k.core.then
import org.http4k.filter.FlashAttributesFilter
import org.http4k.filter.flash
import org.http4k.filter.withFlash
import org.http4k.routing.ResourceLoader.Companion.Classpath
import org.http4k.routing.bind
import org.http4k.routing.path
import org.http4k.routing.routes
import org.http4k.routing.static
import java.time.LocalDate

private val Request.referer: String?
    get() = header("Referer")

class Zettai(val hub: ZettaiHub) : HttpHandler {
    override fun invoke(request: Request): Response = httpHandler(request)

    val httpHandler = FlashAttributesFilter.then(
        routes(
            "/" bind GET to ::homePage,
            "/ping" bind GET to { Response(OK).body("pong") },
            "/todo/{user}" bind GET to ::getAllLists,
            "/todo/{user}" bind POST to ::createNewList,
            "/todo/{user}/{listname}" bind GET to ::getToDoList,
            "/todo/{user}/{listname}/additem" bind POST to ::addNewItem,
            "/todo/{user}/{listname}/rename" bind POST to ::renameList,
            "/whatsnext/{user}" bind GET to ::whatsNext,
            "/static" bind static(Classpath("/static"))
        )
    )

    private fun homePage(request: Request) =
        emptyTagMap.renderHtml("/html/home.html")
            .transform { it.toOkResponse() }
            .recover(errorToResponse(request.referer))

    private fun createNewList(request: Request): Response =
        executeCommand(
            ::CreateToDoList
                    `!` request.extractUser()
                    `*` request.extractListNameFromForm("listname")
        ).transform { allListsPath(it.user) }
            .transform { Response(SEE_OTHER).header("Location", it) }
            .recover(errorToResponse(request.referer))

    private fun renameList(request: Request): Response =
        executeCommand(
            ::RenameToDoList
                    `!` request.extractUser()
                    `*` request.extractListName()
                    `*` request.extractListNameFromForm("newListName")
        )
            .transform { Response(SEE_OTHER).header("Location", todoListPath(it.user, it.newName)) }
            .recover(errorToResponse(request.referer))

    private fun addNewItem(request: Request): Response =
        executeCommand(
            ::AddToDoItem
                    `!` request.extractUser()
                    `*` request.extractListName()
                    `*` request.extractItem()
        )
            .transform { Response(SEE_OTHER).header("Location", todoListPath(it.user, it.name)) }
            .recover(errorToResponse(request.referer))


    private fun getToDoList(request: Request): Response =
        executeQuery(
            ::UserListName
                    `!` request.extractUser()
                    `*` request.extractListName(),
            hub::getList
        )
            .bind { (ul, list) -> renderListPage(ul.user, list, request.flash()) }
            .transform(HtmlPage::toOkResponse)
            .recover(errorToResponse(request.referer))


    private fun getAllLists(request: Request): Response =
        executeQuery(
            request.extractUser(),
            hub::getLists
        )
            .bind { (user, items) -> renderListsPage(user, items) }
            .transform(HtmlPage::toOkResponse)
            .recover(errorToResponse(request.referer))

    private fun whatsNext(request: Request): Response =
        executeQuery(
            request.extractUser(),
            hub::whatsNext
        )
            .bind { (user, items) -> renderWhatsNextPage(user, items) }
            .transform(HtmlPage::toOkResponse)
            .recover(errorToResponse(request.referer))


    private fun errorToResponse(referrer: String?): ZettaiError.() -> Response = {
        when (this) {
            is ValidationError -> Response(SEE_OTHER)
                .header("Location", referrer)
                .withFlash("Validation error: $msg")

            is InvalidRequestError -> Response(NOT_FOUND).body(msg)
            is ZettaiParsingError -> Response(BAD_REQUEST).body(msg)
            is QueryError, is ToDoListCommandError,
            is InconsistentStateError, is ZettaiRenderError ->
                Response(UNPROCESSABLE_ENTITY).body(msg)
        }
    }

    private fun Request.extractUser(): ZettaiOutcome<User> =
        path("user")
            .failIfNull(InvalidRequestError("User not present"))
            .transform(::User)

    private fun Request.extractListName(): ZettaiOutcome<ListName> =
        path("listname")
            .failIfNull(InvalidRequestError("Invalid list name in path: $this"))
            .bind { ListName.fromUntrusted(it) }

    private fun Request.extractListNameFromForm(formName: String) =
        form(formName)
            .failIfNull(InvalidRequestError("missing listname in form"))
            .bind { ListName.fromUntrusted(it) }

    private fun Request.extractItem(): ZettaiOutcome<ToDoItem> {
        val duedate = tryOrNull { LocalDate.parse(form("itemdue")) }
        return form("itemname")
            .failIfNull(InvalidRequestError("User not present"))
            .transform { ToDoItem(it, duedate) }
    }

    private fun allListsPath(user: User) = "/todo/${user.name}"

    private fun todoListPath(
        user: User,
        newListName: ListName
    ) = "/todo/${user.name}/${newListName.name}"


    private fun <C : ToDoListCommand> executeCommand(command: ZettaiOutcome<C>): ZettaiOutcome<C> =
        command.bind(hub::handle)

    private fun <QP, QR> executeQuery(
        queryParams: ZettaiOutcome<QP>,
        query: (QP) -> ZettaiOutcome<QR>
    ): ZettaiOutcome<Pair<QP, QR>> =
        queryParams.bind { qp -> query(qp).transform { qp to it } }
}




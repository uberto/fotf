package com.ubertob.fotf.zettai.webserver

import com.ubertob.fotf.zettai.commands.*
import com.ubertob.fotf.zettai.domain.*
import com.ubertob.fotf.zettai.events.UserListName
import com.ubertob.fotf.zettai.fp.*
import com.ubertob.fotf.zettai.fp.Outcome.Companion.tryOrFail
import com.ubertob.fotf.zettai.logger.LogContext
import com.ubertob.fotf.zettai.logger.LoggerError
import com.ubertob.fotf.zettai.logger.OperationKind
import com.ubertob.fotf.zettai.logger.OperationKind.Command
import com.ubertob.fotf.zettai.logger.OperationKind.Query
import com.ubertob.fotf.zettai.logger.ZettaiLogger
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

class Zettai(val hub: ZettaiHub, val logger: ZettaiLogger) : HttpHandler {

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
            "/todo/{user}/{listname}/deleteitem" bind POST to ::deleteItem,
            "/todo/{user}/{listname}/updateitem" bind POST to ::updateItem,
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
                .logIt(Command, "createNewList", request) { "List created as ${it.name}" }
        ).transform { allListsPath(it.user) }
            .transform { Response(SEE_OTHER).header("Location", it) }
            .recover(errorToResponse(request.referer))

    private fun renameList(request: Request): Response =
        executeCommand(
            ::RenameToDoList
                    `!` request.extractUser()
                    `*` request.extractListName()
                    `*` request.extractListNameFromForm("newListName")
        ).logIt(Command, "renameList", request) { "List renamed to ${it.newName}" }
            .transform { Response(SEE_OTHER).header("Location", todoListPath(it.user, it.newName)) }
            .recover(errorToResponse(request.referer))

    private fun addNewItem(request: Request): Response =
        executeCommand(
            ::AddToDoItem
                    `!` request.extractUser()
                    `*` request.extractListName()
                    `*` request.extractItem()
        ).logIt(Command, "addNewItem", request) { "Item added ${it.item}" }
            .transform { Response(SEE_OTHER).header("Location", todoListPath(it.user, it.name)) }
            .recover(errorToResponse(request.referer))

    private fun deleteItem(request: Request): Response =
        executeCommand(
            ::DeleteToDoItem
                    `!` request.extractUser()
                    `*` request.extractListName()
                    `*` request.extractItemName("olditemname")
        ).logIt(Command, "deleteItem", request) { "Item ${it.itemName} deleted" }
            .transform { Response(SEE_OTHER).header("Location", todoListPath(it.user, it.name)) }
            .recover(errorToResponse(request.referer))

    private fun updateItem(request: Request): Response =
        executeCommand(
            ::UpdateToDoItem
                    `!` request.extractUser()
                    `*` request.extractListName()
                    `*` request.extractItemName("olditemname")
                    `*` request.extractItem()
        )
            .logIt(Command, "updateItem", request) { "Item updated to ${it.newItem}" }
            .transform { Response(SEE_OTHER).header("Location", todoListPath(it.user, it.name)) }
            .recover(errorToResponse(request.referer))


    private fun getToDoList(request: Request): Response =
        executeQuery(
            ::UserListName
                    `!` request.extractUser()
                    `*` request.extractListName(),
            hub::getList
        )
            .logIt(Query, "getToDoList", request) { "Got list ${it.second.listName}" }
            .bind { (ul, list) -> renderListPage(ul.user, list, request.flash()) }
            .transform(HtmlPage::toOkResponse)
            .recover(errorToResponse(request.referer))


    private fun getAllLists(request: Request): Response =
        executeQuery(
            request.extractUser(),
            hub::getLists
        )
            .logIt(Query, "getAllLists", request) { "Found ${it.second.size} lists" }
            .bind { (user, items) -> renderListsPage(user, items) }
            .transform(HtmlPage::toOkResponse)
            .recover(errorToResponse(request.referer))

    private fun whatsNext(request: Request): Response =
        executeQuery(
            request.extractUser(),
            hub::whatsNext
        )
            .logIt(Query, "whatsNext", request) { "Found ${it.second.size} items to do" }
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

    private fun Request.extractItemName(formName: String) =
        form(formName)
            .failIfNull(InvalidRequestError("missing $formName in form"))

    private fun Request.extractItem(): ZettaiOutcome<ToDoItem> =
        ::ToDoItem `!`
                extractItemName("itemname") `*`
                extractDueDate("itemdue") `*`
                extractStatus("status")


    private fun Request.extractStatus(formName: String): ZettaiOutcome<ToDoStatus> =
        tryOrFail { ToDoStatus.valueOf(form(formName) ?: ToDoStatus.Todo.name) }
            .transformFailure { InvalidRequestError("$formName invalid: $it") }


    private fun Request.extractDueDate(formName: String): ZettaiOutcome<LocalDate?> =
        parseDate(form(formName))
            .transformFailure { InvalidRequestError("$formName invalid: $it") }

    private fun parseDate(dateStr: String?): Outcome<ThrowableError, LocalDate?> =
        tryOrFail {
            if (dateStr.isNullOrBlank())
                null
            else
                LocalDate.parse(dateStr)
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


    private fun <T> ZettaiOutcome<T>.logIt(
        kind: OperationKind,
        description: String,
        request: Request,
        describeSuccess: (T) -> String
    ): ZettaiOutcome<T> =
        also {
            logger(
                transform(describeSuccess)
                    .transformFailure {
                        LoggerError(it, request)
                    }, //we log request only for failures
                LogContext(
                    description,
                    kind,
                    request.extractUser().orNull(),
                    request.extractListName().orNull()
                )
            )
        }


}


import FetchListSpike_ws.*
import java.net.URI

typealias User = String
typealias ListName = String
typealias ToDoList = List<String>

data class Request(
    val method: String,
    val uri: URI,
    val body: String
)

data class Response(
    val status: Int,
    val body: String
)

typealias HttpHandler = (Request) -> Response

data class Html(val raw: String)

fun extractListData(request: Request): Pair<User, ListName> = TODO()
fun fetchListContent(listId: Pair<User, ListName>): ToDoList = TODO()
fun renderHtml(list: ToDoList): Html = TODO()
fun createResponse(html: Html): Response = TODO()

fun fetchList(request: Request): Response =
    createResponse(
        renderHtml(
            fetchListContent(
                extractListData(
                    request
                )
            )
        )
    )

fun fetchList(request: Request): Response =
    request.let(::extractListData)
        .let(::fetchListContent)
        .let(::renderHtml)
        .let(::createResponse)


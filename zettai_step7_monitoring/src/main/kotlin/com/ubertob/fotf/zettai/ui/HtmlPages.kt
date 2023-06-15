package com.ubertob.fotf.zettai.ui

import com.ubertob.fotf.zettai.domain.*
import com.ubertob.fotf.zettai.fp.unlessNullOrEmpty
import org.http4k.core.ContentType.Companion.TEXT_HTML
import org.http4k.core.Response
import org.http4k.core.Status.Companion.OK
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class HtmlPage(val raw: CharSequence) {

    fun toOkResponse() = Response(OK)
        .body(raw.toString())
        .header("Content-Type", TEXT_HTML.toHeaderValue())

}


fun renderListPage(
    user: User,
    toDoList: ToDoList,
    errors: String? = null
): ZettaiOutcome<HtmlPage> =
    mapOf(
        "user" tag user.name,
        "listname" tag toDoList.listName.name,
        "items" tag toDoList.items.toTagMaps(),
        "errors" tag errors,
        "if_error" tag (errors != null)
    ).renderHtml("/html/single_list_page.html")

fun renderListsPage(
    user: User,
    listNames: List<ListName>
): ZettaiOutcome<HtmlPage> =
    mapOf(
        "user" tag user.name,
        "listnames" tag listNames.map { mapOf("listname" tag it.name) },
    ).renderHtml("/html/user_lists_page.html")


fun renderWhatsNextPage(
    user: User,
    items: List<ToDoItem>
): ZettaiOutcome<HtmlPage> = mapOf(
    "user" tag user.name,
    "items" tag items.toTagMaps()
).renderHtml("/html/whatsnew_page.html")


fun renderTemplatefromResources(fileName: String, data: TagMap): ZettaiOutcome<Template> =
    TemplateTag::class.java.getResource(fileName).readText().let { it.renderTemplate(data) }
        .transformFailure { ZettaiRenderError(it) }


val emptyTagMap: TagMap = emptyMap()

fun TagMap.renderHtml(fileName: String): ZettaiOutcome<HtmlPage> =
    renderTemplatefromResources(fileName, this)
        .transform(Template::toString)
        .transform(::HtmlPage)

private fun List<ToDoItem>.toTagMaps(): List<TagMap> = map {
    mapOf(
        "description" tag it.description,
        "dueDate" tag it.dueDate?.toIsoString().orEmpty(),
        "status" tag it.status.toString()
    )
}


fun LocalDate.toIsoString(): String = format(DateTimeFormatter.ISO_LOCAL_DATE)

fun String?.toIsoLocalDate(): LocalDate? =
    unlessNullOrEmpty { LocalDate.parse(this, DateTimeFormatter.ISO_LOCAL_DATE) }

fun String.toStatus(): ToDoStatus = ToDoStatus.valueOf(this)

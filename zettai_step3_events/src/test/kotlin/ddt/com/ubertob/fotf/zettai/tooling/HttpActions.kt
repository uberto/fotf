package ddt.com.ubertob.fotf.zettai.tooling

import com.ubertob.fotf.zettai.commands.AddToDoItem
import com.ubertob.fotf.zettai.commands.CreateToDoList
import com.ubertob.fotf.zettai.domain.*
import com.ubertob.fotf.zettai.ui.HtmlPage
import com.ubertob.fotf.zettai.ui.toIsoLocalDate
import com.ubertob.fotf.zettai.ui.toStatus
import com.ubertob.pesticide.core.DdtProtocol
import com.ubertob.pesticide.core.DomainSetUp
import com.ubertob.pesticide.core.Http
import com.ubertob.pesticide.core.Ready
import org.http4k.client.JettyClient
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.body.Form
import org.http4k.core.body.toBody
import org.http4k.server.Jetty
import org.http4k.server.asServer
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.junit.jupiter.api.fail
import strikt.api.expectThat
import strikt.assertions.hasSize
import strikt.assertions.isEqualTo

data class HttpActions(val env: String = "local") : ZettaiActions {

    val zettaiPort = 8000 //different from the one in main
    val zettai = prepareZettaiForTests()
    val server = zettai.asServer(Jetty(zettaiPort))
    val hub = zettai.hub

    val client = JettyClient()

    override val protocol: DdtProtocol = Http(env)
    override fun prepare(): DomainSetUp {
        server.start()
        return Ready
    }

    override fun tearDown(): HttpActions =
        also { server.stop() }

    override fun getToDoList(user: User, listName: ListName): ToDoList? {

        val response = callZettai(Method.GET, todoListUrl(user, listName))

        if (response.status == Status.NOT_FOUND)
            return null

        expectThat(response.status).isEqualTo(Status.OK)

        val html = HtmlPage(response.bodyString())

        val items = extractItemsFromPage(html)

        return ToDoList(listName, items)
    }


    override fun addListItem(user: User, listName: ListName, item: ToDoItem) {
        val response = submitToZettai(
            todoListUrl(user, listName),
            listOf("itemname" to item.description, "itemdue" to item.dueDate?.toString())
        )

        expectThat(response.status).isEqualTo(Status.SEE_OTHER)

    }

    override fun allUserLists(user: User): List<ListName> {
        val response = callZettai(Method.GET, allUserListsUrl(user))

        expectThat(response.status).isEqualTo(Status.OK)

        val html = HtmlPage(response.bodyString())

        val names = extractListNamesFromPage(html)

        return names.map { name -> ListName.fromTrusted(name) }
    }

    override fun createList(user: User, listName: ListName) {
        val response = submitToZettai(allUserListsUrl(user), newListForm(listName))

        expectThat(response.status).isEqualTo(Status.SEE_OTHER)  //redirect same page
    }

    private fun newListForm(listName: ListName): Form = listOf("listname" to listName.name)

    override fun ToDoListOwner.`starts with a list`(listName: String, items: List<String>) {
        val list = ListName.fromTrusted(listName)
        val events = hub.handle(
            CreateToDoList(user, list)
        )
        events ?: fail("Failed to create list $listName for $name")
        val created = items.mapNotNull {
            hub.handle(
                AddToDoItem(user, list, ToDoItem(it))
            )
        }
        expectThat(created).hasSize(items.size)
    }


    private fun todoListUrl(user: User, listName: ListName) =
        "todo/${user.name}/${listName.name}"

    private fun allUserListsUrl(user: User) =
        "todo/${user.name}"

    private fun extractItemsFromPage(html: HtmlPage): List<ToDoItem> {
        return html.parse()
            .select("tr")
            .filter { it.select("td").size == 3 }
            .map {
                Triple(
                    it.select("td")[0].text().orEmpty(),
                    it.select("td")[1].text().toIsoLocalDate(),
                    it.select("td")[2].text().orEmpty().toStatus()
                )
            }
            .map { (name, date, status) ->
                ToDoItem(name, date, status)
            }
    }

    private fun extractListNamesFromPage(html: HtmlPage): List<String> {
        return html.parse()
            .select("tr")
            .mapNotNull {
                it.select("td").firstOrNull()?.text()
            }
    }

    private fun submitToZettai(path: String, webForm: Form): Response =
        client(log(Request(Method.POST, "http://localhost:$zettaiPort/$path").body(webForm.toBody())))

    private fun callZettai(method: Method, path: String): Response =
        client(log(Request(method, "http://localhost:$zettaiPort/$path")))

    fun <T> log(something: T): T {
        println("--- $something")
        return something
    }

    private fun HtmlPage.parse(): Document = Jsoup.parse(raw)

}


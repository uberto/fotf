package com.ubertob.fotf.zettai.webservice

import org.http4k.core.HttpHandler
import org.http4k.core.Method.GET
import org.http4k.core.Method.POST
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status.Companion.CREATED
import org.http4k.core.Status.Companion.OK
import org.http4k.routing.bind
import org.http4k.routing.routes
import org.http4k.server.Jetty
import org.http4k.server.asServer

val app: HttpHandler = routes(
    "/greetings" bind GET to ::greetings,
    "/data" bind POST to ::receiveData,
)

fun receiveData(req: Request): Response = Response(CREATED).body("Received ${req.bodyString()}")

@Suppress("UNUSED_PARAMETER")
fun greetings(req: Request): Response = Response(OK).body(htmlPage)

val htmlPage = """
<html>
    <body>
        <h1 style="text-align:center; font-size:3em;" >Hello Functional World!</h1>
    </body>
</html>"""

fun main() {
    app.asServer(Jetty(8080)).start()
}

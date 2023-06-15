package com.ubertob.fotf.exercises.chapter4

import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.contains
import strikt.assertions.isEqualTo
import java.net.URI

class E02_ChainNullableTest {

    val processUnlessNull = ::extractListData andUnlessNull
            ::fetchListContent andUnlessNull
            ::renderHtml andUnlessNull
            ::createResponse

    fun fetchList(request: Request): Response = processUnlessNull(request)
        ?: Response(404, "Not found")

    @Test
    fun `chain nullable happy path`() {
        val req = Request("GET", URI("http://example.com/zettai/uberto/mylist"), "")
        val resp = fetchList(req)

        expectThat(resp) {
            get { status }.isEqualTo(200)
            get { body }.contains("<td>uberto buy milk</td>")
            get { body }.contains("<td>complete mylist</td>")
            get { body }.contains("<td>something else</td>")
        }
    }

    @Test
    fun `wrong request`() {
        val req = Request("GET", URI("http://example.com/somethingelse"), "")
        val resp = fetchList(req)

        expectThat(resp) {
            get { status }.isEqualTo(404)
            get { body }.isEqualTo("Not found")
        }
    }

    @Test
    fun `wrong user`() {
        val req = Request("GET", URI("http://example.com/zettai/ub/mylist"), "")
        val resp = fetchList(req)

        expectThat(resp) {
            get { status }.isEqualTo(404)
            get { body }.isEqualTo("Not found")
        }
    }
}
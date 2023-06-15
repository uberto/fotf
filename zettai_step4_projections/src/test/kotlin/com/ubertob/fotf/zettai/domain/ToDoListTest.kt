package com.ubertob.fotf.zettai.domain

import com.ubertob.fotf.zettai.domain.tooling.*
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo

typealias ListNameConstructor = (String) -> ListName?

class ToDoListTest {

    val validCharset = uppercase + lowercase + digits + "-"
    val invalidCharset = " !@#$%^&*()_+={}[]|:;'<>,./?\u2202\u2203\u2204\u2205"

    @Test
    fun `Valid names are alphanum+hiphen between 3 and 40 chars length`() {

        stringsGenerator(validCharset, 3, 40)
            .take(100)
            .forEach {
                expectThat(ListName.fromUntrusted(it)).isEqualTo(ListName.fromTrusted(it))
            }
    }

    @Test
    fun `Name cannot be empty`() {
        expectThat(ListName.fromUntrusted("")).isEqualTo(null)
    }

    @Test
    fun `Names longer than 40 chars are not valid`() {

        stringsGenerator(validCharset, 41, 200)
            .take(100)
            .forEach {
                expectThat(ListName.fromUntrusted(it)).isEqualTo(null)
            }
    }

    @Test
    fun `Invalid chars are not allowed in the name`() {

        stringsGenerator(validCharset, 1, 30)
            .map { substituteRandomChar(invalidCharset, it) }
            .take(1000).forEach {
                expectThat(ListName.fromUntrusted(it)).isEqualTo(null)
            }
    }
}

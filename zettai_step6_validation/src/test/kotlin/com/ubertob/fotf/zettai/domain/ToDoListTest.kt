package com.ubertob.fotf.zettai.domain

import com.ubertob.fotf.zettai.domain.tooling.*
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.contains
import strikt.assertions.isEqualTo

class ToDoListTest {

    val validCharset = uppercase + lowercase + digits + "-"
    val invalidCharset = " !@#$%^&*()_+={}[]|:;'<>,./?\u2202\u2203\u2204\u2205"

    @Test
    fun `Valid names are alphanum+hiphen between 3 and 40 chars length`() {

        stringsGenerator(validCharset, 3, 40)
            .take(100)
            .forEach {
                expectThat(ListName.fromUntrusted(it).expectSuccess()).isEqualTo(ListName.fromTrusted(it))
            }
    }

    @Test
    fun `Name cannot be blank`() {
        expectThat(ListName.fromUntrusted("    ").expectFailure().msg).contains("contains illegal characters")
    }

    @Test
    fun `Name cannot be too short`() {
        expectThat(ListName.fromUntrusted("ab").expectFailure().msg).contains("too short")
    }

    @Test
    fun `Names longer than 40 chars are not valid`() {
        stringsGenerator(validCharset, 41, 200)
            .take(100)
            .forEach {
                val failure = ListName.fromUntrusted(it).expectFailure()
                expectThat(failure.msg).contains("is too long")
            }
    }

    @Test
    fun `Invalid chars are not allowed in the name`() {

        stringsGenerator(validCharset, 3, 40)
            .map { substituteRandomChar(invalidCharset, it) }
            .take(1000).forEach {
                expectThat(
                    ListName.fromUntrusted(it).expectFailure().msg
                ).contains("contains illegal characters")
            }
    }

    @Test
    fun `Multiple failures are reported correctly`() {
        expectThat(ListName.fromUntrusted("0123456789012345678901234567890123456789!").expectFailure().msg)
            .contains("contains illegal characters")
            .contains("is too long")
    }
}

package com.ubertob.fotf.exercises.chapter11

import com.ubertob.fotf.exercises.chapter9.ContextReader
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo

typealias ConfigurationReader = ContextReader<Map<String, String>, String>

class E02_ApplicativeReaderTest {

    val number = ConfigurationReader { ctx -> ctx["number"].orEmpty() }
    val street = ConfigurationReader { ctx -> ctx["street"].orEmpty() }
    val city = ConfigurationReader { ctx -> ctx["city"].orEmpty() }

    fun address(number: String, street: String, city: String): String =
        "$number $street, $city"

    val data = mapOf("number" to "10", "street" to "Downing Street", "city" to "London")

    @Test
    fun `read and apply configuration values correctly`() {
        val res = ::address `!` number `*` street `*` city

        expectThat(res.runWith(data)).isEqualTo("10 Downing Street, London")
    }
}
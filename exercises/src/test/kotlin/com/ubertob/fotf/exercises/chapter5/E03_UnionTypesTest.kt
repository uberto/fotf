package com.ubertob.fotf.exercises.chapter5

import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo

class E03_UnionTypesTest {

    @Test
    fun `compact Json only removing spaces outside strings literals`() {
        val jsonText = """{ 
            "my greetings"   : "hello world! \"How are you?\"" 
            }"""
        val expected = """{"my greetings":"hello world! \"How are you?\""}"""

        expectThat(compactJson(jsonText)).isEqualTo(expected)
    }
}
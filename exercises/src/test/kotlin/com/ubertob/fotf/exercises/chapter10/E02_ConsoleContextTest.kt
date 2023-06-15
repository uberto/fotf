package com.ubertob.fotf.exercises.chapter10

import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.PrintStream


class E02_ConsoleContextTest {

    @Test
    fun `console context read and write from console`() {
        val output = ByteArrayOutputStream()
        val input = ByteArrayInputStream("Uberto".toByteArray())

        val stdOut = System.out
        val stdIn = System.`in`

        try {
            System.setIn(input)
            System.setOut(PrintStream(output))

            greetingsOnConsole()

        } finally {
            System.setOut(stdOut)
            System.setIn(stdIn)
        }

        expectThat(output.toString()).isEqualTo("Hello, what's your name?\nHello, Uberto!\n")
    }
}
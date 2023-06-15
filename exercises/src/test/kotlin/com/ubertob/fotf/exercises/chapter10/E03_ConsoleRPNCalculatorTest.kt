package com.ubertob.fotf.exercises.chapter10

import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.PrintStream

class E03_ConsoleRPNCalculatorTest {


    @Test
    fun `RPN calculator read and write from console`() {
        val output = ByteArrayOutputStream()
        val input = ByteArrayInputStream(
            """
            4 3 2 1 - + *
            1 2 3 * 4 - +
            Q
        """.trimIndent().toByteArray()
        )

        val stdOut = System.out
        val stdIn = System.`in`

        try {
            System.setIn(input)
            System.setOut(PrintStream(output))

            consoleRpnCalculator().runWith(SystemConsole())

        } finally {
            System.setOut(stdOut)
            System.setIn(stdIn)
        }

        val expected =
            """Write an RPN expression to calculate the result or Q to quit.
              |The result is: 16.0
              |Write an RPN expression to calculate the result or Q to quit.
              |The result is: 3.0
              |Write an RPN expression to calculate the result or Q to quit.
              |Bye!
              |
              |""".trimMargin()
        expectThat(output.toString()).isEqualTo(expected)
    }
}
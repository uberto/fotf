package com.ubertob.fotf.exercises.chapter10

import com.ubertob.fotf.exercises.chapter9.ContextReader
import java.io.BufferedReader
import java.io.InputStreamReader

interface ConsoleContext {
    fun printLine(msg: String): String
    fun readLine(): String
}

class SystemConsole : ConsoleContext {
    override fun printLine(msg: String): String = msg.also { println(msg) }

    val reader = BufferedReader(InputStreamReader(System.`in`))
    override fun readLine(): String = reader.readLine()
}

fun contextPrintln(msg: String) = ContextReader<ConsoleContext, String> { ctx -> ctx.printLine(msg) }
fun contextReadln() = ContextReader<ConsoleContext, String> { ctx -> ctx.readLine() }


fun greetingsOnConsole() {
    contextPrintln("Hello, what's your name?")
        .bind { _ -> contextReadln() }
        .bind { name -> contextPrintln("Hello, $name!") }
        .runWith(SystemConsole())
}

fun main() {

    greetingsOnConsole()

}

package com.ubertob.fotf.exercises.chapter7

import java.time.LocalDate


fun sendEmail(fileName: String): Outcome<EmailError, Unit> =
    readFile(fileName)
        .transformFailure { EmailError("error reading file ${it.msg}") }
        .onFailure { return@sendEmail it.asFailure() }
        .let(::sendTextByEmail)


fun readFile(fileName: String): Outcome<FileError, String> =
    if (fileName.startsWith("err"))
        FileError("$fileName missing!").asFailure()
    else
        "something from $fileName".asSuccess()

fun sendTextByEmail(text: String): Outcome<EmailError, Unit> = Unit.asSuccess()


fun todayGreetings(dateString: String): Outcome<ThrowableError, String> =
    tryAndCatch { LocalDate.parse(dateString) }
        .transform { "Today is $it" }



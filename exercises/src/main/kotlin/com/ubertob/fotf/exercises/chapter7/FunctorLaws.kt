package com.ubertob.fotf.exercises.chapter7

import java.time.Instant
import java.util.*

fun <T> identity(x: T): T = x

interface OutcomeError {
    val msg: String
}

data class ThrowableError(val throwable: Throwable) : OutcomeError {
    override val msg = throwable.message ?: "unknown error"
}

data class FileError(override val msg: String) : OutcomeError
data class EmailError(override val msg: String) : OutcomeError

enum class cases { int, string, instant, bool }

val random = Random()
fun randomString(): String {
    val chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"

    val stringLength = random.nextInt(20)
    val randomString = (1..stringLength)
        .map { chars[random.nextInt(chars.length)] }
        .joinToString("")
    return randomString
}

fun randomOutcome(): Outcome<OutcomeError, Any> =
    when (cases.values().random()) {
        cases.int -> random.nextInt().asSuccess()
        cases.string -> randomString().asSuccess()
        cases.instant -> randomInstant().asSuccess()
        cases.bool -> random.nextBoolean().asSuccess()
    }

private fun randomInstant(): Instant = Instant.ofEpochMilli(random.nextLong())


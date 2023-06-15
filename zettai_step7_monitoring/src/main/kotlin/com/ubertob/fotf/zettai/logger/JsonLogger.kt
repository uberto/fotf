package com.ubertob.fotf.zettai.logger

import com.ubertob.fotf.zettai.domain.ZettaiError
import com.ubertob.fotf.zettai.fp.Outcome
import com.ubertob.fotf.zettai.fp.OutcomeError
import com.ubertob.fotf.zettai.fp.recover
import logger.JLogEntry
import org.http4k.core.Request
import java.io.FileOutputStream
import java.io.OutputStream
import java.io.PrintWriter
import java.net.InetAddress.getLocalHost
import java.time.Clock

typealias ZettaiLogger = (Outcome<*, *>, LogContext) -> Unit

fun stdOutLogger() = JsonLogger(
    System.out,
    Clock.systemUTC(),
    getLocalHost().hostName
)


fun fileLogger(fileName: String) =
    JsonLogger(
        FileOutputStream(fileName),
        Clock.systemUTC(),
        getLocalHost().hostName
    )

abstract class StreamLogger(stream: OutputStream) : ZettaiLogger {

    abstract fun onSuccess(value: Any?, logContext: LogContext): String
    abstract fun onFailure(error: OutcomeError, logContext: LogContext): String

    val writer = PrintWriter(stream, true)

    override fun invoke(outcome: Outcome<*, *>, logContext: LogContext) {
        outcome.transform { onSuccess(it, logContext) }
            .recover { onFailure(it, logContext) }
            .let(writer::println)
    }
}


data class SimpleLogger(
    private val stream: OutputStream
) : StreamLogger(stream) {
    override fun onSuccess(value: Any?, logContext: LogContext): String = "success! ${value}"

    override fun onFailure(error: OutcomeError, logContext: LogContext): String = "error! ${error.msg}"

}

data class JsonLogger(
    private val stream: OutputStream,
    private val clock: Clock,
    private val hostName: String
) : StreamLogger(stream) {

    override fun onSuccess(value: Any?, logContext: LogContext): String =
        LogSuccess(
            clock.instant(),
            hostName,
            value.toString(),
            logContext
        ).toJson()

    override fun onFailure(error: OutcomeError, logContext: LogContext): String =
        LogFailure(
            clock.instant(),
            hostName,
            error.toString(),
            logContext
        ).toJson()

    private fun LogEntry.toJson(): String = JLogEntry.toJson(this)

}

data class LoggerError(val error: ZettaiError, val request: Request) : OutcomeError {
    override val msg: String = "$error - processing request: ${request}"
}

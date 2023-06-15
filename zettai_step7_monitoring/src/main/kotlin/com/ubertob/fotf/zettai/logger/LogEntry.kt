package com.ubertob.fotf.zettai.logger

import java.time.Instant

sealed interface LogEntry {
    val time: Instant
    val hostname: String
    val msg: String
    val logContext: LogContext
}

data class LogSuccess(
    override val time: Instant, override val hostname: String, override val msg: String,
    override val logContext: LogContext
) : LogEntry


data class LogFailure(
    override val time: Instant,
    override val hostname: String,
    override val msg: String,
    override val logContext: LogContext
) : LogEntry


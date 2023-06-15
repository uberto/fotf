package logger

import com.ubertob.fotf.zettai.domain.ListName
import com.ubertob.fotf.zettai.domain.User
import com.ubertob.fotf.zettai.logger.LogContext
import com.ubertob.fotf.zettai.logger.LogEntry
import com.ubertob.fotf.zettai.logger.LogFailure
import com.ubertob.fotf.zettai.logger.LogSuccess
import com.ubertob.kondor.json.*
import com.ubertob.kondor.json.datetime.str
import com.ubertob.kondor.json.jsonnode.JsonNodeObject

object JUser : JStringRepresentable<User>() {
    override val cons = ::User
    override val render = User::name
}

object JListName : JStringRepresentable<ListName>() {
    override val cons = ::ListName
    override val render = ListName::name
}

object JLogContext : JAny<LogContext>() {

    private val kind by str(LogContext::kind)

    private val description by str(LogContext::desc)

    private val user by str(JUser, LogContext::user)

    private val list_name by str(JListName, LogContext::listName)

    override fun JsonNodeObject.deserializeOrThrow() =
        LogContext(
            desc = +description,
            kind = +kind,
            user = +user,
            listName = +list_name
        )
}


object JLogEntry : JSealed<LogEntry>() {

    override val discriminatorFieldName: String = "outcome"

    override val subConverters = mapOf(
        "success" to JLogSuccess,
        "failure" to JLogFailure
    )

    override fun extractTypeName(obj: LogEntry): String =
        when (obj) {
            is LogSuccess -> "success"
            is LogFailure -> "failure"
        }

}

object JLogSuccess : JAny<LogSuccess>() {
    private val hostname by str(LogSuccess::hostname)

    private val time by str(LogSuccess::time)

    private val log_context by flatten(JLogContext, LogSuccess::logContext)

    private val log_message by str(LogSuccess::msg)

    override fun JsonNodeObject.deserializeOrThrow(): LogSuccess =
        LogSuccess(
            hostname = +hostname,
            logContext = +log_context,
            msg = +log_message,
            time = +time
        )
}


object JLogFailure : JAny<LogFailure>() {

    private val hostname by str(LogFailure::hostname)

    private val time by str(LogFailure::time)

    private val log_context by flatten(JLogContext, LogFailure::logContext)

    private val error by str(LogFailure::msg)

    override fun JsonNodeObject.deserializeOrThrow(): LogFailure =
        LogFailure(
            msg = +error,
            hostname = +hostname,
            logContext = +log_context,
            time = +time
        )
}

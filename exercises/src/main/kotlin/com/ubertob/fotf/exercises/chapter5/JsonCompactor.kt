package com.ubertob.fotf.exercises.chapter5


fun compactJson(json: String): String =
    json.fold(OutQuotes(""), ::compactor).jsonCompacted


fun compactor(prev: JsonCompactor, c: Char): JsonCompactor =
    prev.compact(c)


sealed class JsonCompactor {
    abstract val jsonCompacted: String
    abstract fun compact(c: Char): JsonCompactor
}

data class InQuotes(override val jsonCompacted: String) : JsonCompactor() {
    override fun compact(c: Char): JsonCompactor = when (c) {
        '\\' -> Escaped(jsonCompacted + c)
        '"' -> OutQuotes(jsonCompacted + c)
        else -> InQuotes(jsonCompacted + c)
    }
}

data class OutQuotes(override val jsonCompacted: String) : JsonCompactor() {
    override fun compact(c: Char): JsonCompactor = when {
        c.isWhitespace() -> this
        c == '"' -> InQuotes(jsonCompacted + c)
        else -> OutQuotes(jsonCompacted + c)
    }
}

data class Escaped(override val jsonCompacted: String) : JsonCompactor() {
    override fun compact(c: Char): JsonCompactor = InQuotes(jsonCompacted + c)
}

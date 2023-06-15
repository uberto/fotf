package com.ubertob.fotf.zettai.fp


data class Parser<A, S>(
    val render: (A) -> S,
    val parse: (S) -> Outcome<OutcomeError, A>
) {
    fun parseOrThrow(encoded: S) =
        parse(encoded).onFailure { error("Error parsing $encoded ${it.msg}") }
}
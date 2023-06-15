package com.ubertob.fotf.zettai.db.fp

import com.ubertob.fotf.zettai.fp.*
import org.jetbrains.exposed.sql.Transaction


data class ContextReader<CTX, out T>(val runWith: (CTX) -> T) {

    fun <U> transform(f: (T) -> U): ContextReader<CTX, U> = ContextReader { ctx -> f(runWith(ctx)) }

    fun <U> bind(f: (T) -> ContextReader<CTX, U>): ContextReader<CTX, U> =
        ContextReader { ctx -> f(runWith(ctx)).runWith(ctx) }


}

typealias KArrow<A, B, CTX> = (A) -> ContextReader<CTX, B>

infix fun <A, B, C, CTX> KArrow<A, B, CTX>.fish(other: KArrow<B, C, CTX>): KArrow<A, C, CTX> =
    { a -> this(a).bind { b -> other(b) } }

infix fun <CTX, T> ContextReader<CTX, T>.composeWith(other: ContextReader<CTX, T>): ContextReader<CTX, T> =
    bind { other }

fun <CTX, T> ContextReader<CTX, ContextReader<CTX, T>>.join(): ContextReader<CTX, T> =
    bind { it }


fun <T> id(x: T): T = x

fun <CTX> createContext(): ContextReader<CTX, CTX> = ContextReader(::id)

fun <CTX, T : Any, U> ContextReader<CTX, T?>.bindNullable(f: (T) -> ContextReader<CTX, U>): ContextReader<CTX, U?> =
    ContextReader { t -> runWith(t)?.let { f(it).runWith(t) } }

fun <CTX, E : OutcomeError, T, U> ContextReader<CTX, Outcome<E, T>>.bindOutcome(f: (T) -> ContextReader<CTX, U>): ContextReader<CTX, Outcome<E, U>> =
    ContextReader { t ->
        val outcome = runWith(t)
        when (outcome) {
            is Success -> f(outcome.value).runWith(t).asSuccess()
            is Failure -> outcome
        }
    }


typealias TxReader<T> = ContextReader<Transaction, T>

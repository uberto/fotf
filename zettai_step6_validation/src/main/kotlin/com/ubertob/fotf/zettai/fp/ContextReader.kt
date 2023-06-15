package com.ubertob.fotf.zettai.fp


data class ContextReader<CTX, out T>(val runWith: (CTX) -> T) {

    fun <U> transform(f: (T) -> U): ContextReader<CTX, U> = ContextReader { ctx -> f(runWith(ctx)) }

    fun <U> bind(f: (T) -> ContextReader<CTX, U>): ContextReader<CTX, U> =
        ContextReader { ctx -> f(runWith(ctx)).runWith(ctx) }

    fun <U, V> arrow(f: (T, U) -> V, other: ContextReader<CTX, U>): ContextReader<CTX, V> =
        ContextReader { ctx -> f(runWith(ctx), other.runWith(ctx)) }

    companion object {

        fun <CTX, A, B, R> transform2(
            first: ContextReader<CTX, A>,
            second: ContextReader<CTX, B>,
            f: (A, B) -> R
        ): ContextReader<CTX, R> =
            first.bind { a ->
                second.transform { b ->
                    f(a, b)
                }
            }
    }

}

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
        runWith(t).transform { f(it).runWith(t) }
    }



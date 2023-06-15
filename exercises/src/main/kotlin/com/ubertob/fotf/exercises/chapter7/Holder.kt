package com.ubertob.fotf.exercises.chapter7

data class Holder<T>(private val value: T) {
    fun <U> transform(f: (T) -> U): Holder<U> = Holder(f(value))

    fun combine(other: Holder<T>, f: (T, T) -> T): Holder<T> =
        transform { v -> f(v, other.value) }
}


fun <T> T.asSuccess(): Outcome<Nothing, T> = Success(this)
fun <E : OutcomeError> E.asFailure(): Outcome<E, Nothing> = Failure(this)


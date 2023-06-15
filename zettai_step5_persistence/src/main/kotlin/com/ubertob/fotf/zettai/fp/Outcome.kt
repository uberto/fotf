package com.ubertob.fotf.zettai.fp

sealed class Outcome<out E : OutcomeError, out T> {

    fun <U> transform(f: (T) -> U): Outcome<E, U> =
        when (this) {
            is Success -> f(value).asSuccess()
            is Failure -> this
        }

    fun <F : OutcomeError> transformFailure(f: (E) -> F): Outcome<F, T> =
        when (this) {
            is Success -> this
            is Failure -> f(error).asFailure()
        }

    companion object {
        fun <T, U, E : OutcomeError> lift(f: (T) -> U): (Outcome<E, T>) -> Outcome<E, U> =
            { o -> o.transform { f(it) } }

        fun <T> tryOrFail(block: () -> T): Outcome<ThrowableError, T> =
            try {
                block().asSuccess()
            } catch (t: Throwable) {
                ThrowableError(t).asFailure()
            }

    }

}

data class Success<T> internal constructor(val value: T) : Outcome<Nothing, T>()
data class Failure<E : OutcomeError> internal constructor(val error: E) : Outcome<E, Nothing>()


inline fun <T, E : OutcomeError> Outcome<E, T>.recover(recoverError: (E) -> T): T =
    when (this) {
        is Success -> value
        is Failure -> recoverError(error)
    }

inline fun <T, U, E : OutcomeError> Outcome<E, T>.bind(f: (T) -> Outcome<E, U>): Outcome<E, U> =
    when (this) {
        is Success -> f(value)
        is Failure -> this
    }


fun <T, E : OutcomeError> Outcome<E, Outcome<E, T>>.join(): Outcome<E, T> =
    bind { it }

fun <T : Any, E : OutcomeError> Outcome<E, T?>.failIfNull(error: E): Outcome<E, T> =
    when (this) {
        is Success -> if (value != null) value.asSuccess() else error.asFailure()
        is Failure -> this
    }


inline fun <T, E : OutcomeError> Outcome<E, T>.onFailure(exitBlock: (E) -> Nothing): T =
    when (this) {
        is Success<T> -> value
        is Failure<E> -> exitBlock(error)
    }


inline fun <T, E : OutcomeError> Outcome<E, T>.failIf(error: E, predicate: (T) -> Boolean): Outcome<E, T> =
    when (this) {
        is Success<T> -> if (predicate(value)) error.asFailure() else this
        is Failure<E> -> this
    }


interface OutcomeError {
    val msg: String
}


fun <E : OutcomeError> E.asFailure(): Outcome<E, Nothing> = Failure(this)
fun <T> T.asSuccess(): Outcome<Nothing, T> = Success(this)


fun <T : Any, E : OutcomeError> T?.failIfNull(error: E): Outcome<E, T> = this?.asSuccess() ?: error.asFailure()
fun <C : Collection<*>, E : OutcomeError> C.failIfEmpty(error: E): Outcome<E, C> =
    if (isEmpty()) error.asFailure() else this.asSuccess()


data class ThrowableError(val t: Throwable) : OutcomeError {
    override val msg: String
        get() = t.message.orEmpty()
}
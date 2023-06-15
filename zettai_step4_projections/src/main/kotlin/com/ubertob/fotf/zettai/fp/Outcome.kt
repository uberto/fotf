package com.ubertob.fotf.zettai.fp

sealed class Outcome<out E : OutcomeError, out T> {

    fun <U> transform(f: (T) -> U): Outcome<E, U> =
        when (this) {
            is Success -> f(value).asSuccess()
            is Failure -> this
        }

    companion object {
        fun <T, U, E : OutcomeError> lift(f: (T) -> U): (Outcome<E, T>) -> Outcome<E, U> =
            { o -> o.transform { f(it) } }

    }

}

data class Success<T> internal constructor(val value: T) : Outcome<Nothing, T>()
data class Failure<E : OutcomeError> internal constructor(val error: E) : Outcome<E, Nothing>()


inline fun <T, E : OutcomeError> Outcome<E, T>.recover(recoverError: (E) -> T): T =
    when (this) {
        is Success -> value
        is Failure -> recoverError(error)
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




package com.ubertob.fotf.zettai.fp

import com.ubertob.fotf.zettai.fp.Outcome.Companion.transform2


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
        fun <E : OutcomeError, T, U> lift(f: (T) -> U): (Outcome<E, T>) -> Outcome<E, U> =
            { o -> o.transform { f(it) } }

        fun <T> tryOrFail(block: () -> T): Outcome<ThrowableError, T> =
            try {
                block().asSuccess()
            } catch (t: Throwable) {
                ThrowableError(t).asFailure()
            }


        fun <ERR : OutcomeError, A, B, R> transform2(
            first: Outcome<ERR, A>,
            second: Outcome<ERR, B>,
            f: (A, B) -> R
        ): Outcome<ERR, R> =
            when (first) {
                is Failure -> first
                is Success -> second.transform { b -> f(first.value, b) }
            }
//            first.bind { a ->
//                second.transform { b ->
//                    f(a, b)
//                }
//            }


        fun <ER : OutcomeError, E1 : ER, E2 : ER, T> transform2Failures(
            first: Outcome<E1, T>,
            second: Outcome<E2, T>,
            f: (E1, E2) -> ER
        ): Outcome<ER, T> =
            when (first) {
                is Success<*> -> second
                is Failure<E1> -> when (second) {
                    is Success<*> -> first
                    is Failure<E2> -> f(first.error, second.error).asFailure()
                }
            }

        fun <ERR : OutcomeError, A, B, C, R> transform3(
            first: Outcome<ERR, A>,
            second: Outcome<ERR, B>,
            third: Outcome<ERR, C>,
            f: (A, B, C) -> R
        ): Outcome<ERR, R> =
            transform2(first,
                transform2(second, third) { b, c -> f.partial(c).partial(b) })
            { a: A, fa: (A) -> R -> fa(a) }
    }

}

data class Success<T> internal constructor(val value: T) : Outcome<Nothing, T>()
data class Failure<E : OutcomeError> internal constructor(val error: E) : Outcome<E, Nothing>()


//it has to be an extension function because there are no lower bounds in Kotlin
fun <E : OutcomeError, T, U> Outcome<E, T>.bind(f: (T) -> Outcome<E, U>): Outcome<E, U> =
    when (this) {
        is Success -> f(value)
        is Failure -> this
    }

fun <E : OutcomeError, T, F : OutcomeError> Outcome<E, T>.bindFailure(f: (E) -> Outcome<F, T>): Outcome<F, T> =
    when (this) {
        is Success -> this
        is Failure -> f(error)
    }

fun <E : OutcomeError, T> Outcome<E, Outcome<E, T>>.join(): Outcome<E, T> =
    bind { it }

fun <E : OutcomeError, T> Outcome<E, T>.recover(recoverError: (E) -> T): T =
    when (this) {
        is Success -> value
        is Failure -> recoverError(error)
    }

fun <E : OutcomeError, T> T.asOutcome(isSuccess: Boolean, errorFn: () -> E): Outcome<E, T> =
    if (isSuccess) asSuccess() else errorFn().asFailure()

fun <E : OutcomeError, T : Any> Outcome<E, T?>.failIfNull(error: E): Outcome<E, T> =
    when (this) {
        is Success -> if (value != null) value.asSuccess() else error.asFailure()
        is Failure -> this
    }

inline fun <E : OutcomeError, T> Outcome<E, T>.onFailure(exitBlock: (E) -> Nothing): T =
    when (this) {
        is Success<T> -> value
        is Failure<E> -> exitBlock(error)
    }


interface OutcomeError {
    val msg: String
}


fun <E : OutcomeError> E.asFailure(): Outcome<E, Nothing> = Failure(this)
fun <T> T.asSuccess(): Outcome<Nothing, T> = Success(this)


fun <E : OutcomeError, T : Any> T?.failIfNull(error: E): Outcome<E, T> = this?.asSuccess() ?: error.asFailure()

data class ThrowableError(val t: Throwable) : OutcomeError {
    override val msg: String
        get() = t.message.orEmpty()
}

fun <ERR : OutcomeError, T, U> Iterable<T>.foldOutcome(
    initial: U,
    operation: (acc: U, T) -> Outcome<ERR, U>
): Outcome<ERR, U> =
    fold(initial.asSuccess() as Outcome<ERR, U>) { acc, el -> acc.bind { operation(it, el) } }


fun <E : OutcomeError, T, U> Iterable<T>.traverseOutcome(f: (T) -> Outcome<E, U>): Outcome<E, List<U>> =
    foldOutcome(emptyList()) { list, e -> f(e).transform { list + it } }

fun <E : OutcomeError, T> Iterable<Outcome<E, T>>.swapWithList(): Outcome<E, List<T>> =
    traverseOutcome { it }

//fun <E : OutcomeError, T> Iterable<Outcome<E, T>>.swapWithList(): Outcome<E, List<T>> =
//    fold(emptyList<T>().asSuccess()) { acc: Outcome<E, Iterable<T>>, e: Outcome<E, T> ->
//        acc.bind { list -> e.transform { list + it } }
//    }


fun <E : OutcomeError, A, B, R> ((A, B) -> R).transformAndCurry(other: Outcome<E, A>): Outcome<E, (B) -> R> =
    other.transform { curry()(it) }

fun <E : OutcomeError, A, B, C, R> ((A, B, C) -> R).transformAndCurry(other: Outcome<E, A>): Outcome<E, (B) -> (C) -> R> =
    other.transform { curry()(it) }

fun <E : OutcomeError, A, B, C, D, R> ((A, B, C, D) -> R).transformAndCurry(other: Outcome<E, A>): Outcome<E, (B) -> (C) -> (D) -> R> =
    other.transform { curry()(it) }

infix fun <E : OutcomeError, A, B, R> ((A, B) -> R).`!`(other: Outcome<E, A>): Outcome<E, (B) -> R> =
    transformAndCurry(other)

infix fun <E : OutcomeError, A, B, C, R> ((A, B, C) -> R).`!`(other: Outcome<E, A>): Outcome<E, (B) -> (C) -> R> =
    transformAndCurry(other)

infix fun <E : OutcomeError, A, B, C, D, R> ((A, B, C, D) -> R).`!`(other: Outcome<E, A>): Outcome<E, (B) -> (C) -> (D) -> R> =
    transformAndCurry(other)


fun <E : OutcomeError, A, B> Outcome<E, (A) -> B>.andApply(a: Outcome<E, A>): Outcome<E, B> =
    transform2(this, a) { f, x -> f(x) }

@Suppress("DANGEROUS_CHARACTERS")
infix fun <E : OutcomeError, A, B> Outcome<E, (A) -> B>.`*`(a: Outcome<E, A>): Outcome<E, B> = andApply(a)



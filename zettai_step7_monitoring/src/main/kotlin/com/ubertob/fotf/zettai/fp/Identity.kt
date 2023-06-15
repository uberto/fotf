package com.ubertob.fotf.zettai.fp


data class Holder<T>(private val value: T) {
    fun <U> transform(f: (T) -> U): Holder<U> = Holder(f(value))
    fun <U> bind(f: (T) -> Holder<U>): Holder<U> = f(value)

    companion object {
        fun <A, B, R> transform2(
            first: Holder<A>,
            second: Holder<B>,
            f: (A, B) -> R
        ): Holder<R> = Holder(f(first.value, second.value))
//alternative implementation   f `!` first `*` second
    }

}

fun <A, B> Holder<(A) -> B>.andApply(a: Holder<A>): Holder<B> = bind { f -> a.transform { f(it) } }


// implemented with bind
//fun <A, B> Iterable<A>.traverse(f: (A) -> Holder<B>): Holder<List<B>> =
//        fold(Holder(emptyList())) { acc, e ->
//            acc.bind { list -> f(e).transform { list + it } }
//        }

fun <A, B> Iterable<A>.traverse(f: (A) -> Holder<B>): Holder<List<B>> =
    fold(Holder(emptyList())) { acc, e ->
        Holder.transform2(acc, f(e)) { list, el -> list + el }
    }

fun <A> Iterable<Holder<A>>.swapWithList(): Holder<List<A>> =
    traverse { it }

@Suppress("DANGEROUS_CHARACTERS")
infix fun <A, B> Holder<(A) -> B>.`*`(a: Holder<A>): Holder<B> = andApply(a)


fun <A, B, C> ((A, B) -> C).transformAndCurry(other: Holder<A>): Holder<(B) -> C> = other.transform { curry()(it) }

infix fun <A, B, C> ((A, B) -> C).`!`(other: Holder<A>): Holder<(B) -> C> = transformAndCurry(other)


infix fun <A, B, C, D> ((A, B, C) -> D).transformAndCurry(other: Holder<A>): Holder<(B) -> (C) -> D> =
    other.transform { curry()(it) }


infix fun <A, B, C, D> ((A, B, C) -> D).`!`(other: Holder<A>): Holder<(B) -> (C) -> D> = transformAndCurry(other)

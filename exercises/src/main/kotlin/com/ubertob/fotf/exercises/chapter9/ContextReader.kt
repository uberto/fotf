package com.ubertob.fotf.exercises.chapter9

data class ContextReader<CTX, out T>(val runWith: (CTX) -> T) {

    fun <U> transform(f: (T) -> U): ContextReader<CTX, U> = ContextReader { ctx -> f(runWith(ctx)) }


    fun <U> bind(f: (T) -> ContextReader<CTX, U>): ContextReader<CTX, U> =
        this.transform(f).join()
// original
//        ContextReader { ctx -> f(runWith(ctx)).runWith(ctx) }


}

fun <CTX, T> ContextReader<CTX, ContextReader<CTX, T>>.join(): ContextReader<CTX, T> =
    ContextReader { ctx ->
        this.runWith(ctx).runWith(ctx)
    }
//original
//        bind { it }

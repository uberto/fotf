package com.ubertob.fotf.zettai.fp


fun <U : Any> CharSequence?.unlessNullOrEmpty(f: (CharSequence) -> U): U? =
    if (isNullOrEmpty()) null else f(this)


fun <T> T.printIt(prefix: String = ">"): T = also { println("$prefix $this") }

fun <T> tryOrNull(block: () -> T): T? =
    try {
        block()
    } catch (e: Exception) {
        null
    }

fun <T> T.discardUnless(predicate: T.() -> Boolean): T? = takeIf { predicate(it) }

fun <T> T.unless(b: Boolean, alternativeSupplier: () -> T) = if (b) alternativeSupplier() else this

fun <A, B, R> ((A, B) -> R).partial(b: B): (A) -> R = { a -> this(a, b) }
fun <A, B, C, R> ((A, B, C) -> R).partial(c: C): (A, B) -> R = { a, b -> this(a, b, c) }
fun <A, B, C, D, R> ((A, B, C, D) -> R).partial(d: D): (A, B, C) -> R = { a, b, c -> this(a, b, c, d) }

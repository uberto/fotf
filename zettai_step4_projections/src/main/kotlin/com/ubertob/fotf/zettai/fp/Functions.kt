package com.ubertob.fotf.zettai.fp


fun <U : Any> CharSequence?.unlessNullOrEmpty(f: (CharSequence) -> U): U? =
    if (isNullOrEmpty()) null else f(this)


fun <T> T.printIt(prefix: String = ">"): T = also { println("$prefix $this") }

fun <T : Any> tryOrNull(block: () -> T): T? =
    try {
        block()
    } catch (e: Exception) {
        null
    }

fun <T, R> liftList(f: (T) -> R): (List<T>) -> List<R> =
    { c: List<T> -> c.map(f) }

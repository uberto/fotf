package com.ubertob.fotf.zettai.fp


fun <U : Any> CharSequence?.unlessNullOrEmpty(f: (CharSequence) -> U): U? =
    if (isNullOrEmpty()) null else f(this)


fun <T> T.printIt(prefix: String = ">"): T = also { println("$prefix $this") }

package com.ubertob.fotf.zettai.domain.tooling

import kotlin.random.Random

const val uppercase = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
const val lowercase = "abcdefghijklmnopqrstuvwxyz"
const val digits = "0123456789"
const val spacesigns = " ,.:+-()%$@"
const val text = lowercase + digits + spacesigns

fun stringsGenerator(charSet: String, minLen: Int, maxLen: Int): Sequence<String> = generateSequence {
    randomString(charSet, minLen, maxLen)
}

fun randomString(charSet: String, minLen: Int, maxLen: Int) =
    StringBuilder().run {
        val len = if (maxLen > minLen) Random.nextInt(maxLen - minLen) + minLen else minLen
        repeat(len) {
            append(charSet.random())
        }
        toString()
    }

fun randomText(len: Int) = randomString(text, len, len)


fun substituteRandomChar(fromCharset: String, intoString: String): String =
    intoString
        .toCharArray()
        .apply { set(Random.nextInt(intoString.length), fromCharset.random()) }
        .joinToString(separator = "")
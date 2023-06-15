package com.ubertob.fotf.exercises.chapter3


data class StringTag(val text: String)

infix fun String.tag(value: String): Pair<String, StringTag> =
    this to StringTag(value)

fun renderTemplate(template: String, data: Map<String, StringTag>): String =
    data.entries.fold(template) { acc, (key, value) ->
        acc.replace("{$key}", value.text)
    }
package com.ubertob.fotf.exercises.chapter4

import com.ubertob.fotf.exercises.chapter3.StringTag
import com.ubertob.fotf.exercises.chapter3.renderTemplate


data class Person(
    val firstName: String,
    val familyName: String
)


data class EmailTemplate(private val template: String) : (Person) -> String {
    override fun invoke(aPerson: Person): String = renderTemplate(template, aPerson.toTags())
}

private fun Person.toTags(): Map<String, StringTag> =
    mapOf(
        "firstName" to StringTag(firstName),
        "familyName" to StringTag(familyName)
    )

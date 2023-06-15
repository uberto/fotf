package com.ubertob.fotf.zettai.json

import com.ubertob.fotf.zettai.db.eventsourcing.PgEvent
import com.ubertob.fotf.zettai.domain.eventsGenerator
import com.ubertob.fotf.zettai.domain.tooling.expectSuccess
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo

class ToDoListEventParserTest {

    val eventParser = toDoListEventParser()

    @Test
    fun `convert events to and from`() {

        eventsGenerator().take(100).forEach { event ->

            val conversion: PgEvent = eventParser.render(event)
            val newEvent = eventParser.parse(conversion).expectSuccess()

            expectThat(newEvent).isEqualTo(event)

        }
    }
}
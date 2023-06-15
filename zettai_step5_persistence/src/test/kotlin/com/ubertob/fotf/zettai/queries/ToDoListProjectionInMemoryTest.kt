package com.ubertob.fotf.zettai.queries

import com.ubertob.fotf.zettai.events.ToDoListEvent
import com.ubertob.fotf.zettai.eventsourcing.EventSeq
import com.ubertob.fotf.zettai.eventsourcing.StoredEvent
import com.ubertob.fotf.zettai.fp.asSuccess
import java.time.Instant

internal class ToDoListProjectionInMemoryTest : ToDoListProjectionAbstractTest() {

    override fun buildListProjection(events: List<ToDoListEvent>): ToDoListProjection =
        ToDoListProjectionInMemory { after ->
            events.mapIndexed { i, e -> StoredEvent(EventSeq(after.progressive + i + 1), Instant.now(), e) }.asSuccess()
        }.also(ToDoListProjectionInMemory::update)

}



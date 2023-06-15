package com.ubertob.fotf.zettai.eventsourcing

import java.time.Instant

data class EventSeq(val progressive: Long) {
    operator fun compareTo(other: EventSeq): Int = progressive.compareTo(other.progressive)
}


data class StoredEvent<E : Any>(val eventSeq: EventSeq, val storedAt: Instant, val event: E)
package com.ubertob.fotf.zettai.db.eventsourcing

import com.ubertob.fotf.zettai.eventsourcing.EntityId

data class PgEvent(
    val entityId: EntityId,
    val eventType: String,
    val jsonString: String,
    val version: Int,
    val source: String
)




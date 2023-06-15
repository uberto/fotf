package com.ubertob.fotf.zettai.db

import com.ubertob.fotf.zettai.db.eventsourcing.EventStreamerTx
import com.ubertob.fotf.zettai.events.ToDoListEvent
import com.ubertob.fotf.zettai.events.UserListName
import com.ubertob.fotf.zettai.eventsourcing.EventStreamer
import com.ubertob.fotf.zettai.json.toDoListEventParser
import org.jetbrains.exposed.sql.Transaction


typealias ToDoListEventStreamerTx = EventStreamer<Transaction, ToDoListEvent, UserListName>

fun createToDoListEventStreamerOnPg(): ToDoListEventStreamerTx =
    EventStreamerTx(toDoListEventsTable, toDoListEventParser()) {
        """
select
	entity_id 
from
	todo_list_events
where
	event_type = 'ListCreated'
	and json_data ->> 'owner' = '${it.user.name}'
	and json_data ->> 'name' = '${it.listName.name}'
        """.trimIndent()
    }
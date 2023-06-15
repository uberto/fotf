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
inner join (
	select
		MAX(id) as maxid
	from
		todo_list_events
	where
		json_data ->> 'owner' = '${it.user.name}'
		and event_type in ('ListCreated', 'ListRenamed')
	group by
		entity_id) lastRename on
	id = lastRename.maxid
where
	json_data ->> 'owner' = '${it.user.name}'
	and (json_data ->> 'name' = '${it.listName.name}' or json_data ->> 'newName' = '${it.listName.name}')
""".trimIndent()
    }
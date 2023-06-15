package com.ubertob.fotf.zettai.queries

import com.ubertob.fotf.zettai.events.ToDoListEvent
import com.ubertob.fotf.zettai.fp.FetchStoredEvents
import com.ubertob.fotf.zettai.fp.ProjectionQuery
import com.ubertob.fotf.zettai.fp.QueryRunner


class ToDoListQueryRunner(eventFetcher: FetchStoredEvents<ToDoListEvent>) : QueryRunner<ToDoListQueryRunner> {
    internal val listProjection = ToDoListProjection(eventFetcher)
    internal val itemProjection = ToDoItemProjection(eventFetcher)

    override fun <R> invoke(f: ToDoListQueryRunner.() -> R): ProjectionQuery<R> =
        ProjectionQuery(setOf(listProjection, itemProjection)) { f(this) }
}





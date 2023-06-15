package com.ubertob.fotf.zettai.queries

import com.ubertob.fotf.zettai.domain.QueryError
import com.ubertob.fotf.zettai.fp.Outcome
import com.ubertob.fotf.zettai.fp.OutcomeError
import com.ubertob.fotf.zettai.fp.asFailure

class ToDoListQueryRunner(
    val listProjection: ToDoListProjection,
    val itemProjection: ToDoItemProjection
) : QueryRunner<ToDoListQueryRunner> {

    override fun <R> invoke(f: ToDoListQueryRunner.() -> Outcome<OutcomeError, R>): Outcome<QueryError, R> =
        try {
            listProjection.update()
            itemProjection.update()
            f(this).transformFailure { QueryError(it.msg) }
        } catch (t: Throwable) {
            QueryError("Projection query failed ${t.message}", t).asFailure()
        }
}




package com.ubertob.fotf.zettai.queries

import com.ubertob.fotf.zettai.domain.QueryError
import com.ubertob.fotf.zettai.fp.Outcome
import com.ubertob.fotf.zettai.fp.OutcomeError

interface QueryRunner<Self : QueryRunner<Self>> {
    operator fun <R> invoke(f: Self.() -> Outcome<OutcomeError, R>): Outcome<QueryError, R>
}
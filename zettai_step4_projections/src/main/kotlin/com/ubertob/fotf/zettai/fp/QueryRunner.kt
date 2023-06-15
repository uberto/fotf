package com.ubertob.fotf.zettai.fp


data class ProjectionQuery<T>(val projections: Set<Projection<*, *>>, val runner: () -> T) {

    fun <U> transform(f: (T) -> U): ProjectionQuery<U> = ProjectionQuery(projections) { f(runner()) }

    fun runIt(): T {
        projections.forEach(Projection<*, *>::update)
        return runner()
    }
}


interface QueryRunner<Self : QueryRunner<Self>> {
    operator fun <R> invoke(f: Self.() -> R): ProjectionQuery<R>
}

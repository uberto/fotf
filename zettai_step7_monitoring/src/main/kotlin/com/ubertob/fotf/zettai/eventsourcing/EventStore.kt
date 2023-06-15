package com.ubertob.fotf.zettai.eventsourcing

interface EventStore<RES, EVENT : EntityEvent, STATE : EntityState<EVENT>, NK> :
    EntityRetriever<RES, STATE, EVENT, NK>,
    EventPersister<RES, EVENT>



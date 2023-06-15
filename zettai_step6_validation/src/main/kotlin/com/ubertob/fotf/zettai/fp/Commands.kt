package com.ubertob.fotf.zettai.fp


typealias CommandHandler<COMMAND, EVENT, ERROR> = (COMMAND) -> Outcome<ERROR, List<EVENT>>

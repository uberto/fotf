package com.ubertob.fotf.zettai.logger

import com.ubertob.fotf.zettai.domain.ListName
import com.ubertob.fotf.zettai.domain.User


enum class OperationKind { Command, Query, SqlStatement }

data class LogContext(val desc: String, val kind: OperationKind, val user: User?, val listName: ListName?)
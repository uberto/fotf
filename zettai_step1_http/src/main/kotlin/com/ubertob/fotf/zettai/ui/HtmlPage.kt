package com.ubertob.fotf.zettai.ui

import com.ubertob.fotf.zettai.domain.ToDoItem
import com.ubertob.fotf.zettai.domain.ToDoList

data class HtmlPage(val raw: String)

fun renderHtml(todoList: ToDoList): HtmlPage =
    HtmlPage(
        """
    <html>
        <body>
            <h1>Zettai</h1>
            <h2>${todoList.listName.name}</h2>
            <table>
                <tbody>${renderItems(todoList.items)}</tbody>
            </table>
        </body>
    </html>
    """.trimIndent()
    )

private fun renderItems(items: List<ToDoItem>) =
    items.map {
        """<tr><td>${it.description}</td></tr>""".trimIndent()
    }.joinToString("")



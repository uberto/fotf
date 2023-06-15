package com.ubertob.fotf.zettai.ui

import com.ubertob.fotf.zettai.domain.ToDoItem
import com.ubertob.fotf.zettai.domain.User


fun renderWhatsNextPage(user: User, items: List<ToDoItem>): HtmlPage =
    HtmlPage(
        """
        <!DOCTYPE html>
        <html>
        <head>
            <link rel="stylesheet" href="https://stackpath.bootstrapcdn.com/bootstrap/4.3.1/css/bootstrap.min.css" integrity="sha384-ggOyR0iXCbMQv3Xipma34MD+dH/1fQ784/j6cY/iJTQUOhcWr7x9JvoRxT2MZw1T" crossorigin="anonymous">
            <title>Zettai - a ToDoList application</title>
        </head>
        <body>
        <div id="container">
        <div class="row justify-content-md-center"> 
        <div class="col-md-center">
            <h1>Zettai</h1>
            <h2>What's Next for ${user.name}</h2>
            <table class="table table-hover">
                <thead>
                    <tr>
                      <th>Name</th>
                      <th>Due Date</th>
                      <th>Status</th>
                    </tr>
                </thead>
                <tbody>
                ${items.renderItems()}
                </tbody>
            </table>
            </div>
        </div>
        </div>
        </body>
        </html>
    """.trimIndent()
    )
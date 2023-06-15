package com.ubertob.fotf.zettai.ui

import com.ubertob.fotf.zettai.domain.ToDoList
import com.ubertob.fotf.zettai.domain.User


fun renderListPage(user: User, todoList: ToDoList): HtmlPage =
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
            <h2>Things to do for ${todoList.listName.name}</h2>
            <table class="table table-hover">
                <thead>
                    <tr>
                      <th>Name</th>
                      <th>Due Date</th>
                      <th>Status</th>
                    </tr>
                </thead>
                <tbody>
                ${todoList.items.renderItems()}
                </tbody>
            </table>
            
            <hr>
            <h5>Create a new thing to do</h5>
            <form action="/todo/${user.name}/${todoList.listName.name}" method="post">
              <label for="itemname">Description:</label>
              <input type="text" name="itemname" id="itemname">
              <label for="itemdue">Due Date:</label>
              <input type="date" name="itemdue" id="itemdue">
              <input type="submit" value="Submit">
            </form>
            </div>
        </div>
        </div>
        </body>
        </html>
    """.trimIndent()
    )
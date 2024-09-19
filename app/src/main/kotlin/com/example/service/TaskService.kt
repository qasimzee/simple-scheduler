package com.example.service

import com.example.model.*
import com.google.cloud.spanner.*
import com.google.cloud.spanner.SpannerOptions.*
import com.google.cloud.*

class TaskService(private val dbClient: DatabaseClient) {
    private val taskTable = "Tasks"

    // Create a Task
    fun createTask(task: Task) {
        val mutationBuilder = Mutation.newInsertBuilder("tasks")
        .set("task_id").to(task.task_id)
        .set("task_name").to(task.task_name)
        .set("task_schedule").to(task.task_schedule)
        .set("status").to(task.status.value) // Store the enum value

        // Handle nullable expiry
        if (task.expiry != null) {
            mutationBuilder.set("expiry").to(task.expiry)
        } else {
            mutationBuilder.set("expiry").to(Value.NAN)
        }
        val mutation = mutationBuilder.build()
        dbClient.write(listOf(mutation))
    }

    fun getAllTasks(): List<Task> {
        val query = Statement.of("SELECT * FROM tasks")
        val resultSet = dbClient.singleUseReadOnlyTransaction().executeQuery(query)
        val tasks = mutableListOf<Task>()
        while (resultSet.next()) {
            tasks.add(Task(
                resultSet.getString("task_id"),
                resultSet.getString("task_name"),
                resultSet.getString("task_schedule"),
                resultSet.getTimestamp("created_time"),
                resultSet.getTimestamp("expiry"),
                resultSet.getTimestamp("last_execution_time"),
                resultSet.getTimestamp("next_execution_time"),
                TaskStatus.fromValue(resultSet.getString("status"))!!
            ))
        }
        return tasks
    }
}

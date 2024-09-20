package com.example.service

import com.example.model.*
import com.google.cloud.spanner.*
import com.google.cloud.spanner.SpannerOptions.*
import com.google.cloud.*
import com.google.cloud.Timestamp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*


class TaskService(private val dbClient: DatabaseClient) {
    private val taskTable = "Tasks"

    // Create a Task
    suspend fun createTask(task: Task): Task {
        return withContext(Dispatchers.IO) {
            dbClient.readWriteTransaction().run { transaction ->
                val mutation = Mutation.newInsertOrUpdateBuilder("task")
                    .set("id").to(task.id)
                    .set("task_name").to(task.task_name)
                    .set("task_schedule").to(task.task_schedule)
                    .set("created_time").to(task.created_time)
                    .set("expiry").to(task.expiry)
                    .set("last_updated_time").to(task.last_updated_time)
                    .set("next_execution_time").to(task.next_execution_time)
                    .set("status").to(task.status.name)
                    .build()
                transaction.buffer(mutation)
            }
            task
        }
    }
    suspend fun updateTask(task: Task): Task {
        return withContext(Dispatchers.IO) {
            dbClient.readWriteTransaction().run { transaction ->
                val mutation = Mutation.newInsertOrUpdateBuilder("task")
                    .set("id").to(task.id)
                    .set("task_name").to(task.task_name)
                    .set("task_schedule").to(task.task_schedule)
                    .set("created_time").to(task.created_time)
                    .set("expiry").to(task.expiry)
                    .set("last_updated_time").to(task.last_updated_time)
                    .set("next_execution_time").to(task.next_execution_time)
                    .set("status").to(task.status.name)
                    .build()
                transaction.buffer(mutation)
            }
            task
        }
    }
    
    suspend fun getTask(taskId: String): Task? {
        return withContext(Dispatchers.IO) {
            var task: Task? = null
            val statement = Statement.newBuilder("SELECT * FROM task WHERE id = @id")
                .bind("id").to(taskId)
                .build()

            dbClient.singleUse().executeQuery(statement).use { resultSet ->
                if (resultSet.next()) {
                    task = Task(
                        resultSet.getString("id"),
                        resultSet.getString("task_name"),
                        resultSet.getString("task_schedule"),
                        resultSet.getTimestamp("created_time"),
                        resultSet.getTimestamp("expiry"),
                        resultSet.getTimestamp("last_updated_time"),
                        resultSet.getTimestamp("next_execution_time"),
                        TaskStatus.fromValue(resultSet.getString("status"))!!
                    )
                }
            }
            task
        }
    }
    suspend fun getTasks(): List<Task> {
        return withContext(Dispatchers.IO) {
            val query = Statement.of("SELECT * FROM task")
            val resultSet = dbClient.singleUse().executeQuery(query)
            val tasks = mutableListOf<Task>()
            while (resultSet.next()) {
                tasks.add(Task(
                    resultSet.getString("id"),
                    resultSet.getString("task_name"),
                    resultSet.getString("task_schedule"),
                    resultSet.getTimestamp("created_time"),
                    resultSet.getTimestamp("expiry"),
                    resultSet.getTimestamp("last_updated_time"),
                    resultSet.getTimestamp("next_execution_time"),
                    TaskStatus.fromValue(resultSet.getString("status"))!!
                ))
            }
            tasks
        }
    }
}

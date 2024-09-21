package com.example.service

import com.example.model.*

import com.google.cloud.spanner.*
import com.google.cloud.spanner.SpannerOptions.*
import com.google.cloud.*
import kotlinx.coroutines.*
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
    suspend fun setTaskRunning(taskId: String): Boolean {
        return withContext(Dispatchers.IO) {
            var success = false
            dbClient.readWriteTransaction().run { transaction ->
                val selectStatement = Statement.newBuilder(
                    "SELECT * FROM task WHERE id = @task_id AND status = 'SCHEDULED'"
                )
                    .bind("task_id").to(taskId)
                    .build()

                val resultSet = transaction.executeQuery(selectStatement)
                if (resultSet.next()) {
                    val updateMutation = Mutation.newUpdateBuilder("task")
                        .set("id").to(taskId)
                        .set("status").to(TaskStatus.RUNNING.name)
                        .set("last_updated_time").to(Timestamp.now())
                        .build()
                    transaction.buffer(updateMutation)
                    success = true
                } 
            }
            success
        }
    }

    suspend fun updateTaskStatus(taskId: String, status: TaskStatus): Task? {
        return withContext(Dispatchers.IO) {
            val task = getTask(taskId)
            if (task != null) {
                val updatedTask = task.copy(
                    status = status,
                    last_updated_time = Timestamp.now()
                )
                updateTask(updatedTask)
            } else {
                null
            }
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
                        resultSet.getTimestampOrNull("next_execution_time"),
                        TaskStatus.fromValue(resultSet.getString("status"))!!
                    )
                }
            }
            task
        }
    }
    suspend fun getTasks(status: TaskStatus? = null): List<Task> {
        return withContext(Dispatchers.IO) {
            val query = when (status) {
                null -> Statement.of("SELECT * FROM tasks")
                else -> Statement.newBuilder("SELECT * FROM task WHERE status = @status")
                    .bind("status").to(status.name)
                    .build()
            }
            val resultSet = dbClient.singleUse().executeQuery(query)
            val tasks = mutableListOf<Task>()
            while (resultSet.next()) {
                tasks.add(Task(
                    resultSet.getString("id"),
                    resultSet.getString("task_name"),
                    resultSet.getString("task_schedule"),
                    resultSet.getTimestamp("created_time"),
                    resultSet.getTimestampOrNull("expiry"),
                    resultSet.getTimestamp("last_updated_time"),
                    resultSet.getTimestampOrNull("next_execution_time"),
                    TaskStatus.fromValue(resultSet.getString("status"))!!
                ))
            }
            tasks
        }
    }

    // This function creates a new job run for a task. Can be stripped out into a JobService
    suspend fun createJobRun(taskId: String): JobRun {
        return withContext(Dispatchers.IO) {
            val jobRun = JobRun(task_id = taskId, status = JobStatus.RUNNING)
            dbClient.readWriteTransaction().run { transaction ->
                val mutation = Mutation.newInsertOrUpdateBuilder("job_runs")
                    .set("run_id").to(jobRun.run_id)
                    .set("task_id").to(jobRun.task_id)
                    .set("start_time").to(jobRun.start_time)
                    .set("status").to(jobRun.status.name)
                    .build()
                transaction.buffer(mutation)
            }
            jobRun
        }
    }
    // This function creates a new job run for a task. Can be stripped out into a JobService
    suspend fun updateJobRun(jobRun: JobRun) {
        withContext(Dispatchers.IO) {
            dbClient.readWriteTransaction().run { transaction ->
                val mutation = Mutation.newUpdateBuilder("job_runs")
                    .set("run_id").to(jobRun.run_id)
                    .set("task_id").to(jobRun.task_id)
                    .set("start_time").to(jobRun.start_time)
                    .set("end_time").to(jobRun.end_time)
                    .set("status").to(jobRun.status.name)
                    .set("error_message").to(jobRun.error_message)
                    .build()
                transaction.buffer(mutation)
            }
        }
    }
    // Helper function to get Timestamp or null
    private fun ResultSet.getTimestampOrNull(columnName: String): com.google.cloud.Timestamp? {
        return if (isNull(columnName)) null else getTimestamp(columnName)
    }
}

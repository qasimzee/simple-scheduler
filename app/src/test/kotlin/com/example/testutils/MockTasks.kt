package com.example.testutils

import com.example.model.Task
import com.example.model.TaskStatus
import com.google.cloud.Timestamp
import com.google.cloud.spanner.*
import java.time.Instant
import java.util.UUID
import io.mockk.*

fun createMockTasks(): List<Task> {
    val now = Instant.now()
    val tasks = listOf(
        Task(
            id = UUID.randomUUID().toString(),
            task_name = "Daily Data Backup",
            task_schedule = "0 0 * * *",  // Run at midnight every day
            created_time = Timestamp.ofTimeSecondsAndNanos(now.epochSecond, now.nano),
            expiry = Timestamp.ofTimeSecondsAndNanos(now.plusSeconds(30 * 24 * 60 * 60).epochSecond, 0),  // 30 days from now
            last_updated_time = Timestamp.ofTimeSecondsAndNanos(now.epochSecond, now.nano),
            next_execution_time = Timestamp.ofTimeSecondsAndNanos(now.plusSeconds(24 * 60 * 60).epochSecond, 0),  // 24 hours from now
            status = TaskStatus.SCHEDULED
        ),
        Task(
            id = UUID.randomUUID().toString(),
            task_name = "Weekly Report Generation",
            task_schedule = "0 0 * * 1",  // Run at midnight every Monday
            created_time = Timestamp.ofTimeSecondsAndNanos(now.epochSecond, now.nano),
            expiry = Timestamp.ofTimeSecondsAndNanos(now.plusSeconds(60 * 24 * 60 * 60).epochSecond, 0),  // 60 days from now
            last_updated_time = Timestamp.ofTimeSecondsAndNanos(now.epochSecond, now.nano),
            next_execution_time = Timestamp.ofTimeSecondsAndNanos(now.plusSeconds(7 * 24 * 60 * 60).epochSecond, 0),  // 7 days from now
            status = TaskStatus.SCHEDULED
        )
    )
    return tasks
}

fun mockResultSetForTasks(mockDbClient: DatabaseClient, tasks: List<Task>) {
    val mockResultSet = mockk<ResultSet>()
    var callCount = 0
    every { mockResultSet.next() } answers { 
        callCount++ < tasks.size
    }
    every { mockDbClient.singleUse().executeQuery(any<Statement>()) } returns mockResultSet

    tasks.forEach { task ->
        every { mockResultSet.getString("id") } returns task.id
        every { mockResultSet.getString("task_name") } returns task.task_name
        every { mockResultSet.getString("task_schedule") } returns task.task_schedule
        every { mockResultSet.getTimestamp("created_time") } returns task.created_time
        every { mockResultSet.isNull("expiry") } returns true
        every { mockResultSet.getTimestamp("expiry") } returns null
        every { mockResultSet.getTimestamp("last_updated_time") } returns com.google.cloud.Timestamp.now()
        every { mockResultSet.getTimestamp("next_execution_time") } returns null
        every { mockResultSet.getTimestamp("next_execution_time") } returns null
        every { mockResultSet.isNull("next_execution_time") } returns true
        every { mockResultSet.getString("status") } returns task.status.name
    }
}
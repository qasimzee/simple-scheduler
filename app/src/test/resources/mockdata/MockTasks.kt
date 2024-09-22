package com.example.mockdata

import com.example.model.Task
import com.example.model.TaskStatus
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import com.google.cloud.Timestamp
import java.time.Instant
import java.util.UUID

val mockTasks = listOf(
    Task(
        id = UUID.randomUUID().toString(),
        task_name = "Daily Data Backup",
        task_schedule = "0 0 * * *",  // Run at midnight every day
        created_time = Timestamp.now(),
        expiry = Timestamp.ofTimeSecondsAndNanos(Instant.now().plusSeconds(30 * 24 * 60 * 60).epochSecond, 0),  // 30 days from now
        last_updated_time = Timestamp.now(),
        next_execution_time = Timestamp.ofTimeSecondsAndNanos(Instant.now().plusSeconds(24 * 60 * 60).epochSecond, 0),  // 24 hours from now
        status = TaskStatus.SCHEDULED
    ),
    Task(
        id = UUID.randomUUID().toString(),
        task_name = "Weekly Report Generation",
        task_schedule = "0 9 * * MON",  // Run at 9 AM every Monday
        created_time = Timestamp.ofTimeSecondsAndNanos(Instant.now().minusSeconds(7 * 24 * 60 * 60).epochSecond, 0),  // 7 days ago
        expiry = null,  // No expiry
        last_updated_time = Timestamp.now(),
        next_execution_time = Timestamp.ofTimeSecondsAndNanos(Instant.now().plusSeconds(3 * 24 * 60 * 60).epochSecond, 0),  // 3 days from now
        status = TaskStatus.RUNNING
    ),
    Task(
        id = UUID.randomUUID().toString(),
        task_name = "Monthly User Cleanup",
        task_schedule = "0 3 1 * *",  // Run at 3 AM on the first day of every month
        created_time = Timestamp.ofTimeSecondsAndNanos(Instant.now().minusSeconds(30 * 24 * 60 * 60).epochSecond, 0),  // 30 days ago
        expiry = Timestamp.ofTimeSecondsAndNanos(Instant.now().plusSeconds(365 * 24 * 60 * 60).epochSecond, 0),  // 1 year from now
        last_updated_time = Timestamp.ofTimeSecondsAndNanos(Instant.now().minusSeconds(24 * 60 * 60).epochSecond, 0),  // 24 hours ago
        next_execution_time = Timestamp.ofTimeSecondsAndNanos(Instant.now().plusSeconds(15 * 24 * 60 * 60).epochSecond, 0),  // 15 days from now
        status = TaskStatus.SCHEDULED
    ),
    Task(
        id = UUID.randomUUID().toString(),
        task_name = "Hourly Log Rotation",
        task_schedule = "0 * * * *",  // Run every hour
        created_time = Timestamp.ofTimeSecondsAndNanos(Instant.now().minusSeconds(2 * 24 * 60 * 60).epochSecond, 0),  // 2 days ago
        expiry = null,  // No expiry
        last_updated_time = Timestamp.now(),
        next_execution_time = Timestamp.ofTimeSecondsAndNanos(Instant.now().plusSeconds(60 * 60).epochSecond, 0),  // 1 hour from now
        status = TaskStatus.RUNNING
    ),
    Task(
        id = UUID.randomUUID().toString(),
        task_name = "Yearly Archive",
        task_schedule = "0 0 1 1 *",  // Run at midnight on January 1st
        created_time = Timestamp.ofTimeSecondsAndNanos(Instant.now().minusSeconds(180 * 24 * 60 * 60).epochSecond, 0),  // 180 days ago
        expiry = Timestamp.ofTimeSecondsAndNanos(Instant.now().plusSeconds(5 * 365 * 24 * 60 * 60).epochSecond, 0),  // 5 years from now
        last_updated_time = Timestamp.ofTimeSecondsAndNanos(Instant.now().minusSeconds(30 * 24 * 60 * 60).epochSecond, 0),  // 30 days ago
        next_execution_time = Timestamp.ofTimeSecondsAndNanos(Instant.now().plusSeconds(180 * 24 * 60 * 60).epochSecond, 0),  // 180 days from now
        status = TaskStatus.DISABLED
    )
)
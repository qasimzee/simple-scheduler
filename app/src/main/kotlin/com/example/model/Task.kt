package com.example.model
import com.google.cloud.Timestamp

enum class TaskStatus(val value: String) {
    SCHEDULED("SCHEDULED"),
    ON("ON"),
    RUNNING("RUNNING"),
    FAILED("FAILED"),
    ERROR("ERROR"),
    PAUSED("PAUSED"),
    SUSPENDED("SUSPENDED"),
    CANCELLED("CANCELLED"),
    COMPLETED("COMPLETED"),
    BLOCKED("BLOCKED"),
    WAITING("WAITING"),
    EXPIRED("EXPIRED"),
    QUEUED("QUEUED"),
    DISABLED("DISABLED");

    companion object {
        fun fromValue(value: String): TaskStatus? {
            return values().find { it.value == value }
        }
    }
}

data class Task(
    val task_id: String,
    val task_name: String,
    val task_schedule: String,
    val created_time: Timestamp,
    val expiry: Timestamp?,
    val last_execution_time: Timestamp?,
    val next_execution_time: Timestamp?,
    val status: TaskStatus
)
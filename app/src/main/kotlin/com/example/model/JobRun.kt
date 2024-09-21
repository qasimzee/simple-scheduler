package com.example.model


import com.google.cloud.Timestamp
import java.util.UUID

data class JobRun(
    val run_id: String = UUID.randomUUID().toString(),
    val task_id: String,
    val start_time: Timestamp = Timestamp.now(),
    val end_time: Timestamp? = null,
    val status: JobStatus,
    val error_message: String? = null
)

enum class JobStatus {
    RUNNING, FAILED, ERRORED, COMPLETED
}

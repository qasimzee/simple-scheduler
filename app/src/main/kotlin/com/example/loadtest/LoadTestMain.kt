package com.example.loadtest

import com.example.service.TaskService
import com.google.cloud.spanner.*
import kotlinx.coroutines.runBlocking
import com.google.auth.oauth2.GoogleCredentials

fun main() {
    // Initialize Spanner client
    val projectId = System.getenv("SPANNER_PROJECT")?: "numeric-pilot-432704-n6"
    val instanceId = System.getenv("SPANNER_INSTANCE")?: "task-management"
    val databaseName = System.getenv("SPANNER_DATABASE")?: "task-scheduler"

    val spannerOptions = SpannerOptions.newBuilder()
        .setProjectId(projectId)
        .setCredentials(GoogleCredentials.getApplicationDefault())
        .build()

    val spanner = spannerOptions.service
    val db = DatabaseId.of(spannerOptions.getProjectId(), instanceId, databaseName)
    val dbClient = spanner.getDatabaseClient(db)


    val taskService = TaskService(dbClient)
    println("Generating tasks...")
    val tasks = TaskGenerator.generateRandomTasks(100000) // Generate 100,000 tasks for this example

    runBlocking {
        tasks.forEach { task ->
            taskService.createTask(task)
        }
    }

    println("Generated and saved ${tasks.size} tasks.")
}

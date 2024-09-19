package com.example

import com.example.controller.tasks
import com.example.service.TaskService
import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.spanner.DatabaseClient
import com.google.cloud.spanner.DatabaseId
import com.google.cloud.spanner.Spanner
import com.google.cloud.spanner.SpannerOptions
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.routing.*
import io.ktor.server.response.*
import io.ktor.server.request.*

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

fun Application.module() {
    val projectId = "your-project-id"
    val instanceId = "your-instance-id"
    val databaseName = "your-database-name"

    val spannerOptions = SpannerOptions.newBuilder()
        .setProjectId(projectId)
        .setCredentials(GoogleCredentials.getApplicationDefault())
        .build()

    val spanner = spannerOptions.service
    val db = DatabaseId.of(spannerOptions.getProjectId(), instanceId, databaseName)
    val databaseClient = spanner.getDatabaseClient(db)

    val taskService = TaskService(databaseClient)

    embeddedServer(Netty, 8080) {
        routing {
            tasks(taskService)
        }
    }.start(wait = true)

}
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
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.serialization.jackson.*
import io.ktor.server.plugins.callloging.*

fun main() {
    embeddedServer(Netty, port = 8080, module = Application::module).start(wait = true)
}

fun Application.module() {
    install(CallLogging)

    install(ContentNegotiation) {
        jackson()
    }

    val projectId = "numeric-pilot-432704-n6"
    val instanceId = "task-management"
    val databaseName = "task-scheduler"

    val spannerOptions = SpannerOptions.newBuilder()
        .setProjectId(projectId)
        .setCredentials(GoogleCredentials.getApplicationDefault())
        .build()

    val spanner = spannerOptions.service
    val db = DatabaseId.of(spannerOptions.getProjectId(), instanceId, databaseName)
    val databaseClient = spanner.getDatabaseClient(db)

    val taskService = TaskService(databaseClient)

    routing {
        tasks(taskService)
    }

}
package com.example

import com.example.controller.tasks
import com.example.service.TaskService
import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.spanner.*
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
import io.ktor.http.*




fun main() {
    println("Environment variables:")
    System.getenv().forEach { (key, value) -> println("$key: $value") }
    
    val port = System.getenv("PORT")?.toIntOrNull() ?: 8080
    embeddedServer(Netty, port = port, module = Application::module).start(wait = true)
}

fun Application.module() {
    install(CallLogging)

    install(ContentNegotiation) {
        jackson()
    }

    val projectId = System.getenv("SPANNER_PROJECT")?: "numeric-pilot-432704-n6"
    val instanceId = System.getenv("SPANNER_INSTANCE")?: "task-management"
    val databaseName = System.getenv("SPANNER_DATABASE")?: "task-scheduler"

    val spannerOptions = SpannerOptions.newBuilder()
        .setProjectId(projectId)
        .setCredentials(GoogleCredentials.getApplicationDefault())
        .build()

    val spanner = spannerOptions.service
    val db = DatabaseId.of(spannerOptions.getProjectId(), instanceId, databaseName)
    val databaseClient = spanner.getDatabaseClient(db)

    val taskService = TaskService(databaseClient)

    routing {
        get("/health") {
            call.respondText("OK", status = HttpStatusCode.OK)
        }
        tasks(taskService)
    }

}
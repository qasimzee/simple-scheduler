package com.example.controller

import com.example.model.Task
import com.example.service.TaskService

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.request.*
import io.ktor.http.*


fun Route.tasks(taskService: TaskService) {
    post("/tasks") {
        try {
            val task = call.receive<Task>()
            if (task.task_name.isEmpty()) {
                call.respond(HttpStatusCode.BadRequest, "Task name is required")
            }
            val createdTask = taskService.createTask(task)
            call.respond(HttpStatusCode.Created, createdTask)
        }
        catch (e: Exception) {
            call.application.environment.log.error(e.stackTraceToString())
            call.respond(HttpStatusCode.InternalServerError, e.message ?: "Internal server error")
        }
    }

    get("/tasks") {
        val tasks = taskService.getTasks()
        call.respond(tasks)
    }

    get("/") {
        call.respond("Welcome to the Task scheduler")
    }
}
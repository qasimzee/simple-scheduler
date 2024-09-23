package com.example.controller

import com.example.model.Task
import com.example.service.TaskService

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.request.*
import io.ktor.http.*


fun Route.tasks(taskService: TaskService) {
    get("/tasks") {
        val tasks = taskService.getTasks()
        call.respond(tasks)
    }

    post("/tasks") {
        val task = call.receive<Task>()
        taskService.createTask(task)
        call.respond(HttpStatusCode.Created)
    }

    get("/") {
        call.respond("Welcome to the Task scheduler")
    }
}
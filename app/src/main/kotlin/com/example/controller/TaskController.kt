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
        val task = call.receive<Task>()
        taskService.createTask(task)
        call.respond(HttpStatusCode.Created)
    }

    get("/tasks") {
        val tasks = taskService.getAllTasks()
        call.respond(tasks)
    }
}
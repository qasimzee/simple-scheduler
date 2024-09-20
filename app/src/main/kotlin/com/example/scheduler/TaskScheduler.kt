package com.example.scheduler

import com.example.model.*
import com.example.service.TaskService

import org.quartz.impl.StdSchedulerFactory
import org.quartz.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.google.cloud.Timestamp
import com.google.cloud.spanner.SpannerOptions
import com.google.cloud.spanner.DatabaseId
import com.google.auth.oauth2.GoogleCredentials
import kotlinx.coroutines.runBlocking


class TaskJob: Job {
    override fun execute(context: JobExecutionContext) {
        val taskId = context.jobDetail.jobDataMap.getString("taskId")
        val taskService = context.scheduler.context.get("taskService") as TaskService
        val scope = CoroutineScope(Dispatchers.Default)
        scope.launch {
            val task = taskService.getTask(taskId)
            if (task != null) {
                println("Executing task: ${task.task_name}")

                val updatedTask = task.copy(
                    status = TaskStatus.COMPLETED,
                    last_updated_time = Timestamp.now()
                )
                taskService.updateTask(updatedTask)
            }
        }
    }
}


class TaskScheduler (private val taskService: TaskService) {
    private val scheduler: Scheduler = StdSchedulerFactory.getDefaultScheduler()
    private val scope = CoroutineScope(Dispatchers.IO)

    fun start() {
        scheduler.context.put("taskService", taskService)
        scheduler.start()
    }

    fun shutdown() {
        scheduler.shutdown()
    }

    fun scheduleTask(task: Task) {
        val jobDetail = JobBuilder.newJob(TaskJob::class.java)
            .withIdentity(task.id)
            .usingJobData("taskId", task.id)
            .build()
        val trigger = TriggerBuilder.newTrigger()
            .withIdentity(task.id)
            .withSchedule(CronScheduleBuilder.cronSchedule(task.task_schedule))
            .startNow()
            .build()
        scheduler.scheduleJob(jobDetail, trigger)
    }
}

fun main() {
    val projectId = "numeric-pilot-432704-n6"
    val instanceId = "task-management"
    val databaseName = "task-scheduler"
    val spannerOptions = SpannerOptions.newBuilder()
        .setProjectId(projectId)
        .setCredentials(GoogleCredentials.getApplicationDefault())
        .build()

    val spanner = spannerOptions.service
    val db = DatabaseId.of(spannerOptions.getProjectId(), instanceId, databaseName)
    val dbClient = spanner.getDatabaseClient(db)
    val taskService = TaskService(dbClient)
    
    val taskScheduler = TaskScheduler(taskService)
    
    runBlocking {
        taskScheduler.start()
    }
    
    // Keep the application running
    Thread.currentThread().join()
}
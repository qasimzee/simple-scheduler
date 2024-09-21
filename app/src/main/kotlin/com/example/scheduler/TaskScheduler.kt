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
            if (taskService.setTaskRunning(taskId)) {
                var jobRun: JobRun? = null
                try{
                    val task = taskService.getTask(taskId)
                    if (task != null) {
                        jobRun = taskService.createJobRun(taskId)
                        println("Executing task: ${task.task_name}")

                        jobRun = jobRun.copy(
                            end_time = Timestamp.now(),
                            status = JobStatus.COMPLETED
                        )
                        taskService.updateJobRun(jobRun)
                        taskService.updateTaskStatus(taskId, TaskStatus.SCHEDULED)
                        println("Task $taskId completed with JobId: ${jobRun?.run_id}")
                    }
                    
                }
                catch (e: Exception) {
                    println("Error executing task $taskId: ${e.message}")
                        
                        // Update job run status
                        if (jobRun != null) {
                            jobRun = jobRun.copy(
                                end_time = Timestamp.now(),
                                status = JobStatus.FAILED,
                                error_message = e.message
                            )
                            taskService.updateJobRun(jobRun)
                        }

                        // Set task back to SCHEDULED
                        taskService.updateTaskStatus(taskId, TaskStatus.SCHEDULED)
                }
            }
            else {
                println("Task $taskId is already running")
            }
        }
    }
}


class TaskScheduler (private val taskService: TaskService) {
    private val scheduler: Scheduler = StdSchedulerFactory.getDefaultScheduler()
    private val scope = CoroutineScope(Dispatchers.IO)

    fun start() {
        println("Starting scheduler...")
        scheduler.context.put("taskService", taskService)
        scheduler.start()
        loadExistingTasks()
    }

    fun shutdown() {
        scheduler.shutdown()
    }
    private fun loadExistingTasks() {
        println("Loading existing tasks...")
        val tasks = runBlocking { taskService.getTasks(status=TaskStatus.SCHEDULED) }
        println("Found ${tasks.size} tasks")
        tasks.forEach { scheduleTask(it) }
    }

    fun scheduleTask(task: Task) {
        try {
            val jobDetail = JobBuilder.newJob(TaskJob::class.java)
                .withIdentity(task.id)
                .usingJobData("taskId", task.id)
                .build()
            val trigger = TriggerBuilder.newTrigger()
                .withIdentity(task.id)
                .withSchedule(CronScheduleBuilder.cronSchedule(task.task_schedule + " ?"))
                .startNow()
                .build()
            
            scheduler.scheduleJob(jobDetail, trigger)
        } catch (e: Exception) {
            println("Error scheduling task: ${e.message}")
            runBlocking {
                taskService.updateTaskStatus(task.id, TaskStatus.DISABLED)
            }
        }
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
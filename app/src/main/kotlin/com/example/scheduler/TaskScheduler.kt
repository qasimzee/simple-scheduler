package com.example.scheduler

import com.example.model.*
import com.example.service.TaskService

import org.quartz.impl.StdSchedulerFactory
import org.quartz.*
import com.google.cloud.Timestamp
import com.google.cloud.spanner.*
import com.google.auth.oauth2.GoogleCredentials
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import java.util.Properties


class TaskJob: Job {
    override fun execute(context: JobExecutionContext) {
        val taskId = context.jobDetail.jobDataMap.getString("taskId")
        val taskService = context.scheduler.context.get("taskService") as TaskService
        val scope = CoroutineScope(Dispatchers.Default)
        runBlocking {
            if (taskService.setTaskRunning(taskId)) {
                var jobRun: JobRun? = null
                try{
                    val task = taskService.getTask(taskId)
                    if (task != null) {
                        // Should be locked to prevent duplicate runs
                        jobRun = taskService.createJobRun(taskId)
                    
                        println("Executing task: ${task.task_name}")
                        // More Execution code here
                        // Just a random delay to simulate task execution time
                        delay((100..10000).random().toLong())

                        jobRun = jobRun.copy(
                            end_time = Timestamp.now(),
                            status = JobStatus.COMPLETED
                        )
                        taskService.updateJobRun(jobRun)
                        taskService.updateTaskStatus(taskId, TaskStatus.SCHEDULED)
                        val threadId = Thread.currentThread().getId();
                        println("Task $taskId completed with JobId: ${jobRun?.run_id} by thread $threadId")
                    
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
        }
    }
}


class TaskScheduler (private val taskService: TaskService) {
    val props = Properties().apply {
        setProperty("org.quartz.threadPool.threadCount", "100")
    }
    private val scheduler: Scheduler = StdSchedulerFactory(props).scheduler
    private val scope = CoroutineScope(Dispatchers.IO)
    private var pollingJob: kotlinx.coroutines.Job? = null

    fun start() {
        println("Starting scheduler...")
        scheduler.context.put("taskService", taskService)
        scheduler.start()
        startPolling()
    }

    private fun startPolling() {
        pollingJob = scope.launch {
            println("Starting polling job...")
            while (isActive) {
                loadAndScheduleTasks()
                delay(TimeUnit.MINUTES.toMillis(1)) // Poll every minute
            }
        }
    }

    fun shutdown() {
        pollingJob?.cancel()
        scheduler.shutdown()
    }

    private suspend fun loadAndScheduleTasks() {
        val tasks = taskService.getTasks(TaskStatus.SCHEDULED)
        if (tasks.isEmpty()) {
            println("No scheduled tasks found. Will check again in 1 minute.")
        } else {
            tasks.forEach { scheduleTask(it) }
        }
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
    
    val taskScheduler = TaskScheduler(taskService)
    
    runBlocking {
        taskScheduler.start()
    }
    
    // Keep the application running
    Thread.currentThread().join()
}
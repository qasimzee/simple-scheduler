package com.example.scheduler

import com.example.testutils.*
import com.example.model.*
import com.example.service.TaskService
import io.mockk.*
import kotlinx.coroutines.runBlocking
import com.google.cloud.spanner.*
import org.junit.jupiter.api.*
import org.quartz.*
import org.quartz.impl.StdSchedulerFactory
import java.util.*
import kotlinx.coroutines.delay


class TaskSchedulerTest {

    private lateinit var taskScheduler: TaskScheduler
    private lateinit var mockTaskService: TaskService
    private lateinit var mockScheduler: Scheduler
    private lateinit var mockContext: JobExecutionContext
    private lateinit var mockDbClient: DatabaseClient


    @BeforeEach
    fun setup() {
        mockDbClient = mockk(relaxed = true)
        mockTaskService = mockk<TaskService>(relaxed = true)
        mockScheduler = mockk(relaxed = true)
        mockkConstructor(StdSchedulerFactory::class)
        every { anyConstructed<StdSchedulerFactory>().scheduler } returns mockScheduler
        
        mockTaskService = TaskService(mockDbClient)
        taskScheduler = TaskScheduler(mockTaskService)
        mockContext = mockk(relaxed = true)

        every { mockContext.scheduler.context.get("taskService") } returns mockTaskService
        every { mockContext.jobDetail.jobDataMap.getString("taskId") } returns "task-1"
        
    }

    @Test
    fun `start should initialize scheduler and start polling`() {
        taskScheduler.start()

        verify {
            mockScheduler.context.put("taskService", mockTaskService)
            mockScheduler.start()
        }
    }

    @Test
    fun `shutdown should cancel polling job and shutdown scheduler`() {
        taskScheduler.start() // Start to initialize pollingJob
        taskScheduler.shutdown()

        verify {
            mockScheduler.shutdown()
        }
    }

    @Test
    fun `loadAndScheduleTasks should schedule tasks when available`() = runBlocking {
        
        val mockTasks: List<Task> = createMockTasks()
        
        mockResultSetForTasks(mockDbClient, mockTasks)
        taskScheduler.start()
        //delay(100) // Wait for coroutine to run

        coVerify {
            mockTaskService.getTasks(TaskStatus.SCHEDULED)
        }
        // verify(exactly = mockTasks.size) {
        //     mockScheduler.scheduleJob(any(), any())
        // }
    }

    // @Test
    // fun `loadAndScheduleTasks should handle empty task list`() = runBlocking {
    //     coEvery { mockTaskService.getTasks(TaskStatus.SCHEDULED) } returns emptyList()

    //     taskScheduler.start()
    //     // Wait a bit to allow the coroutine to run
    //     delay(100)

    //     coVerify {
    //         mockTaskService.getTasks(TaskStatus.SCHEDULED)
    //     }
    //     verify(exactly = 0) {
    //         mockScheduler.scheduleJob(any(), any())
    //     }
    // }

    // @Test
    // fun `scheduleTask should create job and trigger`() {
    //     val mockTask = mockk<Task>()
    //     every { mockTask.id } returns "task-1"
    //     every { mockTask.task_schedule } returns "0 0 12 * * ?"

    //     taskScheduler.scheduleTask(mockTask)

    //     verify {
    //         mockScheduler.scheduleJob(any(), any())
    //     }
    // }

    // @Test
    // fun `scheduleTask should handle exceptions`() = runBlocking {
    //     val mockTask = mockk<Task>()
    //     every { mockTask.id } returns "task-1"
    //     every { mockTask.task_schedule } returns "invalid cron"
    //     every { mockScheduler.scheduleJob(any(), any()) } throws SchedulerException("Invalid cron expression")

    //     taskScheduler.scheduleTask(mockTask)

    //     coVerify {
    //         mockTaskService.updateTaskStatus("task-1", TaskStatus.DISABLED)
    //     }
    // }
}

class TaskJobTest {

    private lateinit var taskJob: TaskJob
    private lateinit var mockContext: JobExecutionContext
    private lateinit var mockTaskService: TaskService

    @BeforeEach
    fun setup() {
        taskJob = TaskJob()
        mockContext = mockk(relaxed = true)
        mockTaskService = mockk(relaxed = true)

        every { mockContext.scheduler.context.get("taskService") } returns mockTaskService
        every { mockContext.jobDetail.jobDataMap.getString("taskId") } returns "task-1"
    }

    // @Test
    // fun `execute should run task successfully`() = runBlocking {
    //     val mockTask = mockk<Task>()
    //     val mockJobRun = mockk<JobRun>(relaxed = true)

    //     coEvery { mockTaskService.setTaskRunning("task-1") } returns true
    //     coEvery { mockTaskService.getTask("task-1") } returns mockTask
    //     coEvery { mockTaskService.createJobRun("task-1") } returns mockJobRun

    //     taskJob.execute(mockContext)

    //     coVerify {
    //         mockTaskService.setTaskRunning("task-1")
    //         mockTaskService.getTask("task-1")
    //         mockTaskService.createJobRun("task-1")
    //         mockTaskService.updateJobRun(any())
    //         mockTaskService.updateTaskStatus("task-1", TaskStatus.SCHEDULED)
    //     }
    // }

    // @Test
    // fun `execute should handle exceptions`() = runBlocking {
    //     coEvery { mockTaskService.setTaskRunning("task-1") } returns true
    //     coEvery { mockTaskService.getTask("task-1") } throws Exception("Test exception")

    //     taskJob.execute(mockContext)

    //     coVerify {
    //         mockTaskService.updateTaskStatus("task-1", TaskStatus.SCHEDULED)
    //     }
    // }
}
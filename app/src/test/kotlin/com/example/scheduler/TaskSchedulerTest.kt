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
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.delay


class TaskSchedulerTest {
    @MockK
    private lateinit var taskScheduler: TaskScheduler
    @MockK
    private lateinit var mockTaskService: TaskService

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        mockTaskService = mockk(relaxed = true)
        taskScheduler = spyk(TaskScheduler(mockTaskService))
    }

    @Test
    fun testStartAndShutdown() {

        taskScheduler.start()
        taskScheduler.shutdown()
    }   
}
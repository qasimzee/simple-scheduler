package com.example

import com.example.model.*
import com.example.service.TaskService

import com.google.cloud.spanner.DatabaseClient
import com.google.cloud.spanner.ResultSet
import com.google.cloud.spanner.Statement
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import io.mockk.every
import io.mockk.mockk
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals


class TaskApiTest {

    private val mockDatabaseClient = mockk<DatabaseClient>()
    private val taskService = TaskService(mockDatabaseClient)

    @Test
    fun testGetTasks() = testApplication {
        application {
            testModule(taskService)
        }

        // Mock the database query
        val mockResultSet = mockk<ResultSet>()
        every { mockDatabaseClient.singleUse().executeQuery(any<Statement>()) } returns mockResultSet
        every { mockResultSet.next() } returnsMany listOf(true, false)
        every { mockResultSet.getString("id") } returns "1"
        every { mockResultSet.getString("task_name") } returns "Test Task"
        every { mockResultSet.getString("task_schedule") } returns "0 0 * * *"
        every { mockResultSet.getTimestamp("created_time") } returns com.google.cloud.Timestamp.now()
        every { mockResultSet.isNull("expiry") } returns true
        every { mockResultSet.getTimestamp("expiry") } returns null
        every { mockResultSet.getTimestamp("last_updated_time") } returns com.google.cloud.Timestamp.now()
        every { mockResultSet.getTimestamp("next_execution_time") } returns null
        every { mockResultSet.isNull("next_execution_time") } returns true
        every { mockResultSet.getString("status") } returns "SCHEDULED"

        client.get("/tasks").apply {
            assertEquals(HttpStatusCode.OK, status)
            val tasks = JacksonUtils.fromJson<List<Map<String, Any>>>(bodyAsText())
            assertEquals(1, tasks.size)
            assertEquals("Test Task", tasks[0]["task_name"] as String)
        }
    }
}

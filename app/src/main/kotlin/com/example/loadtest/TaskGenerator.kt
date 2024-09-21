package com.example.loadtest

import com.example.model.Task
import com.example.model.TaskStatus
import com.google.cloud.Timestamp
import java.util.*
import kotlin.random.Random
import java.time.Instant
import java.time.temporal.ChronoUnit


object TaskGenerator {
    fun generateRandomTasks(count: Int = 1_000_000): List<Task> {
        val random = Random(System.currentTimeMillis())
        
        fun randomCronString(): String {
            val minute = if (random.nextBoolean()) "*" else random.nextInt(0, 60).toString()
            val hour = if (random.nextBoolean()) "*" else random.nextInt(0, 24).toString()
            return "$minute $hour * * *"
        }
        
        fun randomExpiry(creationTime: Timestamp): Timestamp {
            val creationInstant = Instant.ofEpochSecond(creationTime.seconds, creationTime.nanos.toLong())
            val daysToAdd = random.nextLong(1, 31) // Random number of days between 1 and 30
            val expiryInstant = creationInstant.plus(daysToAdd, ChronoUnit.DAYS)
            return Timestamp.ofTimeSecondsAndNanos(
                expiryInstant.epochSecond,
                expiryInstant.nano
            )
        }

        fun randomTaskName(): String {
            val adjectives = listOf("Quick", "Slow", "Smart", "Clever", "Bright", "Dark", "Loud", "Quiet", "Big", "Small")
            val nouns = listOf("Fox", "Dog", "Cat", "Bird", "Fish", "Lion", "Tiger", "Bear", "Wolf", "Deer")
            return "${adjectives.random()} ${nouns.random()} Task"
        }

        return List(count) {
            Task(
                id = UUID.randomUUID().toString(),
                task_name = randomTaskName(),
                task_schedule = randomCronString(),
                created_time = Timestamp.now(),
                expiry = randomExpiry(Timestamp.now()),
                last_updated_time = Timestamp.now(),
                next_execution_time = Timestamp.now(),
                status = TaskStatus.SCHEDULED
            )
        }
    }
}
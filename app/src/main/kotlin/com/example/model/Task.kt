package com.example.model

import com.google.cloud.Timestamp
import kotlinx.serialization.*

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor

import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.util.UUID
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.*
import com.fasterxml.jackson.databind.annotation.*
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import java.time.Instant


class TimestampSerializer : JsonSerializer<Timestamp>() {
    override fun serialize(value: Timestamp, gen: JsonGenerator, serializers: SerializerProvider) {
        gen.writeString(value.toString())
    }
}

class TimestampDeserializer : JsonDeserializer<Timestamp>() {
    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): Timestamp {
        val instant = Instant.parse(p.valueAsString)
        return Timestamp.ofTimeSecondsAndNanos(instant.epochSecond, instant.nano)
    }
}

object JacksonUtils {
    val objectMapper: ObjectMapper = ObjectMapper().apply {
        registerKotlinModule()
        registerModule(SimpleModule().apply {
            addSerializer(Timestamp::class.java, TimestampSerializer())
            addDeserializer(Timestamp::class.java, TimestampDeserializer())
        })
    }

    inline fun <reified T> toJson(value: T): String = objectMapper.writeValueAsString(value)

    inline fun <reified T> fromJson(json: String): T = objectMapper.readValue(json, T::class.java)
}

enum class TaskStatus(val value: String) {
    SCHEDULED("SCHEDULED"),
    ON("ON"),
    RUNNING("RUNNING"),
    FAILED("FAILED"),
    ERROR("ERROR"),
    PAUSED("PAUSED"),
    SUSPENDED("SUSPENDED"),
    CANCELLED("CANCELLED"),
    COMPLETED("COMPLETED"),
    BLOCKED("BLOCKED"),
    WAITING("WAITING"),
    EXPIRED("EXPIRED"),
    QUEUED("QUEUED"),
    DISABLED("DISABLED");

    companion object {
        fun fromValue(value: String): TaskStatus? {
            return values().find { it.value == value }
        }
    }
}


data class Task(
    val id: String = UUID.randomUUID().toString(),
    val task_name: String,
    val task_schedule: String,  // cron string
    @JsonSerialize(using = TimestampSerializer::class)
    @JsonDeserialize(using = TimestampDeserializer::class)
    val created_time: Timestamp = Timestamp.now(),
    @JsonSerialize(using = TimestampSerializer::class)
    @JsonDeserialize(using = TimestampDeserializer::class)
    val expiry: Timestamp?,
    @JsonSerialize(using = TimestampSerializer::class)
    @JsonDeserialize(using = TimestampDeserializer::class)
    val last_updated_time: Timestamp = Timestamp.now(),
    @JsonSerialize(using = TimestampSerializer::class)
    @JsonDeserialize(using = TimestampDeserializer::class)
    val next_execution_time: Timestamp?,
    val status: TaskStatus
)
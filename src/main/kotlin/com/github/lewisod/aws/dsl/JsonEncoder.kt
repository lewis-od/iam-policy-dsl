package com.github.lewisod.aws.dsl

import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.json.Json

internal object JsonEncoder {
    private val encoder = Json {
        encodeDefaults = false
    }

    fun <T> serialize(serializer: SerializationStrategy<T>, thing: T): String = encoder.encodeToString(serializer, thing)
}

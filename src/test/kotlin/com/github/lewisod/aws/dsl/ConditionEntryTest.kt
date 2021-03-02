package com.github.lewisod.aws.dsl

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class ConditionEntryTest {

    private var builder = ConditionEntryBuilder()

    @BeforeEach
    fun beforeEach() {
        builder = ConditionEntryBuilder()
    }

    @Test
    fun `Builds single condition with single value`() {
        val init: ConditionEntryBuilder.() -> Unit = {
            "key" to "value"
        }
        builder.init()
        val entry = builder.build()

        assertThat(entry).isEqualTo(mapOf("key" to listOf("value")))
    }

    @Test
    fun `Builds single condition with multiple values`() {
        val init: ConditionEntryBuilder.() -> Unit = {
            "key" to ("value1" or "value2")
        }
        builder.init()
        val entry = builder.build()

        assertThat(entry).isEqualTo(mapOf("key" to listOf("value1", "value2")))
    }

    @Test
    fun `Builds single condition with multiple keys`() {
        val init: ConditionEntryBuilder.() -> Unit = {
            "key1" to ("value1" or "value2")
            "key2" to "value3"
        }
        builder.init()
        val entry = builder.build()

        assertThat(entry).isEqualTo(mapOf(
            "key1" to listOf("value1", "value2"),
            "key2" to listOf("value3")
        ))
    }
}

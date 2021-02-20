package com.github.lewisod.aws.dsl

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.skyscreamer.jsonassert.JSONAssert

internal class StatementTest {

    @Test
    fun `Builds a statement when provided with a valid configuration`() {
        val builder = StatementBuilder()
        builder.resource("resource")
        builder.effect(Effect.ALLOW)
        builder.action("action1")
        builder.action("action2")
        val builtStatement = builder.build("sid")

        val expectedStatement = Statement("sid", Effect.ALLOW, null, listOf("action1", "action2"), "resource")
        assertThat(builtStatement).isEqualToComparingFieldByField(expectedStatement)
    }

    @Test
    fun `Throws an exception when no actions supplied`() {
        val builder = StatementBuilder()
        builder.resource("resource")
        builder.effect(Effect.ALLOW)

        assertThatThrownBy {
            builder.build("sid")
        }.isInstanceOf(InvalidStatementException::class.java)
            .hasMessageContaining("A statement must contain at least 1 action")
    }

    @Test
    fun `Throws an exception when effect is not provided`() {
        val builder = StatementBuilder()
        builder.resource("resource")
        builder.action("action")

        assertThatThrownBy {
            builder.build("sid")
        }.isInstanceOf(InvalidStatementException::class.java)
            .hasMessageContaining("Statement must specify an effect")
    }

    @Test
    fun `Throws an exception two principals are provided`() {
        val builder = StatementBuilder()
        builder.principal {
            aws("account-1")
        }
        assertThatThrownBy {
            builder.principal {
                aws("account-2")
            }
        }.isInstanceOf(InvalidStatementException::class.java)
            .hasMessageContaining("A statement can only have one policy")
    }

    @Test
    fun `With no principal serializes to JSON correctly`() {
        val statement = Statement("sid", Effect.ALLOW, null, listOf("action1", "action2"), "resource")
        val expectedJson = """
            {
              "Sid": "sid",
              "Effect": "Allow",
              "Action": ["action1", "action2"],
              "Resource": "resource"
            }
        """.trimIndent()

        val actualJson = statement.toJson()

        JSONAssert.assertEquals(expectedJson, actualJson, true)
    }

    @Test
    fun `With no resource serializes to JSON correctly`() {
        val principal = Principal(PrincipalType.AWS, listOf("account"))
        val statement = Statement("sid", Effect.ALLOW, principal, listOf("action1", "action2"), null)
        val expectedJson = """
            {
              "Sid": "sid",
              "Effect": "Allow",
              "Action": ["action1", "action2"],
              "Principal": { "AWS": "account" }
            }
        """.trimIndent()

        val actualJson = statement.toJson()

        JSONAssert.assertEquals(expectedJson, actualJson, true)
    }
}

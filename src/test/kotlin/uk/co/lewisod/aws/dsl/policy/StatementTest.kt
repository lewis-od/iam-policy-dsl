package uk.co.lewisod.aws.dsl.policy

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

        val expectedStatement = Statement("sid", Effect.ALLOW, listOf("action1", "action2"), "resource")
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
    fun `Throws an exception when resource is not provided`() {
        val builder = StatementBuilder()
        builder.effect(Effect.ALLOW)
        builder.action("action")

        assertThatThrownBy {
            builder.build("sid")
        }.isInstanceOf(InvalidStatementException::class.java)
            .hasMessageContaining("Statement must specify a resource")
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
    fun `Serializes to JSON correctly`() {
        val statement = Statement("sid", Effect.ALLOW, listOf("action1", "action2"), "resource")
        val expectedJson = """
            {
              "Sid": "sid",
              "Effect": "Allow",
              "Action": ["action1", "action2"],
              "Resource": "resource"
            }
        """.trimIndent()

        JSONAssert.assertEquals(expectedJson, statement.toJson(), true)
    }
}

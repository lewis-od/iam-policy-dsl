package uk.co.lewisod.aws.dsl.policy

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.skyscreamer.jsonassert.JSONAssert

internal class StatementTest {

    @Test
    fun `Build statement`() {
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
    fun `No actions`() {
        val builder = StatementBuilder()
        builder.resource("resource")
        builder.effect(Effect.ALLOW)

        assertThatThrownBy {
            builder.build("sid")
        }.isInstanceOf(InvalidStatementException::class.java)
            .hasMessageContaining("A statement must contain at least 1 action")
    }

    @Test
    fun `Missing field`() {
        val builder = StatementBuilder()
        builder.effect(Effect.ALLOW)
        builder.action("action")

        assertThatThrownBy {
            builder.build("sid")
        }.isInstanceOf(InvalidStatementException::class.java)
            .hasMessageContaining("Statement missing value for field")
    }

    @Test
    fun `Serialize to JSON`() {
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

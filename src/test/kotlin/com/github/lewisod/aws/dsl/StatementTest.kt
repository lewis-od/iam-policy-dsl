package com.github.lewisod.aws.dsl

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.skyscreamer.jsonassert.JSONAssert

internal class StatementTest {

    private var builder = StatementBuilder()

    @BeforeEach
    fun beforeEach() {
        builder = StatementBuilder()
    }

    @Test
    fun `Builds a statement when provided with a valid configuration`() {
        builder.resource("resource")
        builder.effect(Effect.ALLOW)
        builder.action("action1")
        builder.action("action2")
        val builtStatement = builder.build("sid")

        val expectedStatement = Statement("sid", Effect.ALLOW, action = listOf("action1", "action2"), resource = "resource")
        assertThat(builtStatement).isEqualToComparingFieldByField(expectedStatement)
    }

    @Test
    fun `Throws an exception when no actions supplied`() {
        builder.resource("resource")
        builder.effect(Effect.ALLOW)

        assertThatThrownBy {
            builder.build("sid")
        }.isInstanceOf(InvalidStatementException::class.java)
            .hasMessageContaining("A statement must contain at least 1 Action")
    }

    @Test
    fun `Throws an exception when effect is not provided`() {
        builder.resource("resource")
        builder.action("action")

        assertThatThrownBy {
            builder.build("sid")
        }.isInstanceOf(InvalidStatementException::class.java)
            .hasMessageContaining("Statement must specify an effect")
    }

    @Test
    fun `Throws an exception on conflicting NotAction`() {
        builder.action("action")
        assertThatThrownBy { builder.notAction("action") }
            .isInstanceOf(InvalidStatementException::class.java)
            .hasMessageContaining("A statement can only specify one of Action or NotAction")
    }

    @Test
    fun `Throws an exception on conflicting Action`() {
        builder.notAction("action")
        assertThatThrownBy { builder.action("action") }
            .isInstanceOf(InvalidStatementException::class.java)
            .hasMessageContaining("A statement can only specify one of Action or NotAction")
    }

    @Test
    fun `Throws an exception on conflicting NotResource`() {
        builder.resource("resource")
        assertThatThrownBy { builder.notResource("resource") }
            .isInstanceOf(InvalidStatementException::class.java)
            .hasMessageContaining("A statement can only specify one of Resource or NotResource")
    }

    @Test
    fun `Throws an exception on conflicting Resource`() {
        builder.notResource("resource")
        assertThatThrownBy { builder.resource("resource") }
            .isInstanceOf(InvalidStatementException::class.java)
            .hasMessageContaining("A statement can only specify one of Resource or NotResource")
    }

    @Test
    fun `Throws an exception on conflicting NotPrincipal`() {
        builder.principal { aws("arn") }
        assertThatThrownBy { builder.notPrincipal { aws("arn") } }
            .isInstanceOf(InvalidStatementException::class.java)
            .hasMessageContaining("A statement can only specify one Principal")
    }

    @Test
    fun `Throws an exception on conflicting Principal`() {
        builder.notPrincipal { aws("arn") }
        assertThatThrownBy { builder.principal { aws("arn") } }
            .isInstanceOf(InvalidStatementException::class.java)
            .hasMessageContaining("A statement can only specify one Principal")
    }

    @Test
    fun `Throws an exception two Principals are provided`() {
        builder.principal { aws("account-1") }
        assertThatThrownBy { builder.principal { aws("account-2") } }
            .isInstanceOf(InvalidStatementException::class.java)
            .hasMessageContaining("A statement can only specify one Principal")
    }

    @Test
    fun `Throws an exception two NotPrincipals are provided`() {
        builder.notPrincipal { aws("account-1") }
        assertThatThrownBy { builder.notPrincipal { aws("account-2") } }
            .isInstanceOf(InvalidStatementException::class.java)
            .hasMessageContaining("A statement can only specify one Principal")
    }

    @Test
    fun `With no Principal serializes to JSON correctly`() {
        val statement = Statement("sid", Effect.ALLOW, action = listOf("action1", "action2"), resource = "resource")
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
        val statement = Statement("sid", Effect.ALLOW, principal =  principal, action = listOf("action1", "action2"))
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

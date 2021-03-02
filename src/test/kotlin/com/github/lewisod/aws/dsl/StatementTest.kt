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
        builder.principal { service("service") }
        builder.condition("StringEquals") {
            "aws:arn" to ("arn1" or "arn2")
        }
        val builtStatement = builder.build("sid")

        val expectedStatement = Statement(
            Effect.ALLOW,
            action = ActionElement(listOf("action1", "action2")),
            sid = "sid",
            resource = ResourceElement(listOf("resource")),
            principal = PrincipalElement(Principal(PrincipalType.SERVICE, listOf("service"))),
            condition = mapOf("StringEquals" to mapOf("aws:arn" to listOf("arn1", "arn2")))
        )
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
            .hasMessageContaining("A statement can only specify either Resource or NotResource")
    }

    @Test
    fun `Throws an exception on conflicting Resource`() {
        builder.notResource("resource")
        assertThatThrownBy { builder.resource("resource") }
            .isInstanceOf(InvalidStatementException::class.java)
            .hasMessageContaining("A statement can only specify either Resource or NotResource")
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
    fun `With a single action serializes to JSON correctly`() {
        val statement = Statement(
            Effect.ALLOW,
            action = ActionElement(listOf("action")),
            sid = "sid"
        )

        val actualJson = statement.toJson()

        //language=json
        val expectedJson = """
            {
              "Sid": "sid",
              "Effect": "Allow",
              "Action": "action"
            }
        """.trimIndent()
        JSONAssert.assertEquals(expectedJson, actualJson, true)
    }

    @Test
    fun `With no Principal serializes to JSON correctly`() {
        val statement = Statement(
            Effect.ALLOW,
            action = ActionElement(listOf("action1", "action2")),
            sid = "sid",
            resource = ResourceElement(listOf("resource"))
        )

        val actualJson = statement.toJson()

        //language=json
        val expectedJson = """
            {
              "Sid": "sid",
              "Effect": "Allow",
              "Action": ["action1", "action2"],
              "Resource": "resource"
            }
        """.trimIndent()
        JSONAssert.assertEquals(expectedJson, actualJson, true)
    }

    @Test
    fun `With no resource serializes to JSON correctly`() {
        val principal = Principal(PrincipalType.AWS, listOf("account"))
        val statement = Statement(
            Effect.ALLOW,
            action = ActionElement(listOf("action1", "action2")),
            sid = "sid",
            principal = PrincipalElement(principal)
        )

        val actualJson = statement.toJson()

        //language=json
        val expectedJson = """
            {
              "Sid": "sid",
              "Effect": "Allow",
              "Action": ["action1", "action2"],
              "Principal": { "AWS": "account" }
            }
        """.trimIndent()
        JSONAssert.assertEquals(expectedJson, actualJson, true)
    }

    @Test
    fun `With negated elements serializes to JSON correctly`() {
        val principal = Principal(PrincipalType.AWS, listOf("account"))
        val statement = Statement(
            Effect.ALLOW,
            action = ActionElement(listOf("action1", "action2"), isNegated = true),
            sid = "sid",
            principal = PrincipalElement(principal, isNegated = true),
            resource = ResourceElement(listOf("resource"), isNegated = true)
        )

        val actualJson = statement.toJson()

        //language=json
        val expectedJson = """
            {
              "Sid": "sid",
              "Effect": "Allow",
              "NotAction": ["action1", "action2"],
              "NotPrincipal": { "AWS": "account" },
              "NotResource": "resource"
            }
        """.trimIndent()
        JSONAssert.assertEquals(expectedJson, actualJson, true)
    }

    @Test
    fun `With multiple Resources serializes to JSON correctly`() {
        val principal = Principal(PrincipalType.AWS, listOf("account"))
        val statement = Statement(
            Effect.ALLOW,
            action = ActionElement(listOf("action1", "action2")),
            sid = "sid",
            resource = ResourceElement(listOf("resource1", "resource2"))
        )

        val actualJson = statement.toJson()

        //language=json
        val expectedJson = """
            {
              "Sid": "sid",
              "Effect": "Allow",
              "Action": ["action1", "action2"],
              "Resource": ["resource1", "resource2"]
            }
        """.trimIndent()
        JSONAssert.assertEquals(expectedJson, actualJson, true)
    }

    @Test
    fun `With Condition serializes to JSON correctly`() {
        val statement = Statement(
            Effect.ALLOW,
            sid = "sid",
            action = ActionElement(listOf("action")),
            condition = mapOf("StringEquals" to mapOf("aws:arn" to listOf("my arn")))
        )

        val actualJson = statement.toJson()

        //language=json
        val expectedJson = """
            {
              "Sid": "sid",
              "Effect": "Allow",
              "Action": "action",
              "Condition": {
                "StringEquals": { "aws:arn": ["my arn"] }
              }
            }
        """.trimIndent()
        JSONAssert.assertEquals(expectedJson, actualJson, true)
    }
}

package uk.co.lewisod.aws.dsl.policy

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.skyscreamer.jsonassert.JSONAssert

internal class PolicyTest {

    @Test
    fun `Specified version`() {
        val statement = Statement("sid", Effect.ALLOW, listOf("action"), "resource")
        val expectedPolicy = Policy("version", listOf(statement))

        val policy = policy("version") {
            statement("sid") {
                effect(Effect.ALLOW)
                action("action")
                resource("resource")
            }
        }

        assertThat(policy).isEqualToComparingFieldByField(expectedPolicy)
    }

    @Test
    fun `Unspecified version`() {
        val statement = Statement("sid", Effect.ALLOW, listOf("action"), "resource")
        val expectedPolicy = Policy("2012-10-17", listOf(statement))

        val policy = policy {
            statement("sid") {
                effect(Effect.ALLOW)
                action("action")
                resource("resource")
            }
        }

        assertThat(policy).isEqualToComparingFieldByField(expectedPolicy)
    }

    @Test
    fun `Serialize to json`() {
        val statement = Statement("sid", Effect.ALLOW, listOf("action"), "resource")
        val policy = Policy("2012-10-17", listOf(statement))

        val expectedJson = """
            {
              "Version": "2012-10-17",
              "Statement": [
                {
                  "Sid": "sid",
                  "Effect": "Allow",
                  "Action": ["action"],
                  "Resource": "resource"
                }
              ]
            }
        """.trimIndent()

        JSONAssert.assertEquals(expectedJson, policy.toJson(), true)
    }
}

package com.github.lewisod.aws.dsl

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.skyscreamer.jsonassert.JSONAssert

internal class PrincipalTest {

    @Test
    fun `Builds an AWS principal`() {
        val builder = PrincipalBuilder()
        builder.aws("account-1", "account-2")
        val builtPrincipal = builder.build()

        val expectedPrincipal = Principal(PrincipalType.AWS, listOf("account-1", "account-2"))
        assertThat(builtPrincipal).isEqualToComparingFieldByFieldRecursively(expectedPrincipal)
    }

    @Test
    fun `Builds a CanonicalUser principal`() {
        val builder = PrincipalBuilder()
        builder.canonicalUser("user")
        val builtPrincipal = builder.build()

        val expectedPrincipal = Principal(PrincipalType.CANONICAL_USER, listOf("user"))
        assertThat(builtPrincipal).isEqualToComparingFieldByFieldRecursively(expectedPrincipal)
    }

    @Test
    fun `Builds a Federated principal`() {
        val builder = PrincipalBuilder()
        builder.federated("user")
        val builtPrincipal = builder.build()

        val expectedPrincipal = Principal(PrincipalType.FEDERATED, listOf("user"))
        assertThat(builtPrincipal).isEqualToComparingFieldByFieldRecursively(expectedPrincipal)
    }

    @Test
    fun `Builds a Service principal`() {
        val builder = PrincipalBuilder()
        builder.service("service-1", "service-2")
        val builtPrincipal = builder.build()

        val expectedPrincipal = Principal(PrincipalType.SERVICE, listOf("service-1", "service-2"))
        assertThat(builtPrincipal).isEqualToComparingFieldByFieldRecursively(expectedPrincipal)
    }

    @Test
    fun `Throws an exception when principal not specified`() {
        assertThatThrownBy {
            PrincipalBuilder().build()
        }.isInstanceOf(InvalidPrincipalException::class.java)
    }

    @Test
    fun `Throws an exception when multiple principles specified`() {
        val builder = PrincipalBuilder()
        builder.service("service")
        assertThatThrownBy {
            builder.federated("federated")
        }.isInstanceOf(InvalidPrincipalException::class.java)
    }

    @ParameterizedTest
    @EnumSource(PrincipalType::class)
    fun `Serializes single values to JSON correctly`(type: PrincipalType) {
        val principal = Principal(type, listOf("account"))
        val expectedJson = """
            {
                "${type.textValue}": "account"
            }
        """.trimIndent()

        val actualJson = principal.toJson()

        JSONAssert.assertEquals(expectedJson, actualJson, true)
    }

    @ParameterizedTest
    @EnumSource(PrincipalType::class)
    fun `Serializes list values to JSON correctly`(type: PrincipalType) {
        val principal = Principal(type, listOf("account-1", "account-2"))
        val expectedJson = """
            {
                "${type.textValue}": ["account-1", "account-2"]
            }
        """.trimIndent()

        val actualJson = principal.toJson()

        JSONAssert.assertEquals(expectedJson, actualJson, true)
    }
}

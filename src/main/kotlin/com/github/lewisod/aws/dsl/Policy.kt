package com.github.lewisod.aws.dsl

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.Collections

private const val DEFAULT_VERSION: String = "2012-10-17"

/**
 * Represents an IAM policy. Built by the [policy] function
 */
@Serializable
data class Policy internal constructor(
    @SerialName("Version") val version: String = DEFAULT_VERSION,
    @SerialName("Statement") val statement: List<Statement>
)

/**
 * Convert the [Policy] to it's AWS-compliant JSON format
 */
fun Policy.toJson(): String = Json.encodeToString(this)

class PolicyBuilder internal constructor() {

    private var statements = mutableListOf<Statement>()

    /**
     * Declares a [statement](https://docs.aws.amazon.com/IAM/latest/UserGuide/reference_policies_elements_statement.html)
     * inside the [Policy]
     *
     * @param sid The SID of the statement
     * @param statementBuilderBlock A function with receiver of type [StatementBuilder] tht defines the statement
     * contents
     * @return A [Statement] instance, as defined by [statementBuilderBlock]
     * @throws InvalidStatementException
     */
    fun statement(sid: String, statementBuilderBlock: StatementBuilder.() -> Unit) {
        val statementBuilder = StatementBuilder()
        statementBuilderBlock.invoke(statementBuilder)
        this.statements.add(statementBuilder.build(sid))
    }

    internal fun build(version: String): Policy {
        if (statements.isEmpty()) {
            throw InvalidStatementException("A policy must contain at least 1 statement")
        }

        return Policy(version, Collections.unmodifiableList(statements))
    }
}

class InvalidPolicyException(message: String) : RuntimeException(message)

/**
 * Top-level function for defining a policy. Allows defining a policy like
 *
 * ```kotlin
 * val policy = policy {
 *   statement("EC2FullAccess") {
 *     effect(ALLOW)
 *     action("ec2:*")
 *     resource("*")
 *   }
 *   statement("S3ProdAccess") {
 *     effect(ALLOW)
 *     action("s3:ListObjects")
 *     action("s3:GetObject")
 *     resource("arn:aws:s3:::prod-bucket")
 *   }
 * }
 * ```
 *
 * @param version The [version](https://docs.aws.amazon.com/IAM/latest/UserGuide/reference_policies_elements_version.html)
 * of the policy
 * @param builderBlock A function with receiver of type [PolicyBuilder] that defines the policy contents
 * @return A [Policy] instance, as defined by the [builderBlock]
 * @throws InvalidPolicyException
 * @throws InvalidStatementException
 */
fun policy(version: String = DEFAULT_VERSION, builderBlock: PolicyBuilder.() -> Unit): Policy {
    val builder = PolicyBuilder()
    builderBlock.invoke(builder)
    return builder.build(version)
}

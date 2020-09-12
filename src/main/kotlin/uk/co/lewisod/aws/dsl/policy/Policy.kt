package uk.co.lewisod.aws.dsl.policy

import java.util.Collections

private const val DEFAULT_VERSION: String = "2012-10-17"

data class Policy internal constructor(
    val version: String = DEFAULT_VERSION,
    val statements: List<Statement>
)

class PolicyBuilder internal constructor() {

    private var statements = mutableListOf<Statement>()

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

fun policy(builderBlock: PolicyBuilder.() -> Unit): Policy {
    return policy(DEFAULT_VERSION, builderBlock)
}

fun policy(version: String, builderBlock: PolicyBuilder.() -> Unit): Policy {
    val builder = PolicyBuilder()
    builderBlock.invoke(builder)
    return builder.build(version)
}

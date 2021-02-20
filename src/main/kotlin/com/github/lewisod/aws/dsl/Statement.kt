package com.github.lewisod.aws.dsl

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.Collections

@Serializable
enum class Effect {
    @SerialName("Allow") ALLOW,
    @SerialName("Deny") DENY
}

/**
 * Represents a statement within an IAM policy
 */
@Serializable
data class Statement internal constructor(
    @SerialName("Sid") val sid: String,
    @SerialName("Effect") val effect: Effect,
    @SerialName("Principal") val principal: Principal? = null,
    @SerialName("Action") val action: List<String>,
    @SerialName("Resource") val resource: String? = null
)

/**
 * Convert the [Statement] to it's AWS-compliant JSON
 */
fun Statement.toJson(): String = JsonEncoder.serialize(Statement.serializer(), this)

class StatementBuilder internal constructor() {

    private var effect: Effect? = null
    private val actions = mutableListOf<String>()
    private var resource: String? = null
    private var principal: Principal? = null

    /**
     * Sets the [effect](https://docs.aws.amazon.com/IAM/latest/UserGuide/reference_policies_elements_effect.html) field
     * of the statement
     */
    fun effect(effect: Effect) { this.effect = effect }

    /**
     * Sets the [action](https://docs.aws.amazon.com/IAM/latest/UserGuide/reference_policies_elements_action.html) field
     * of the statement
     */
    fun action(vararg actions: String) { this.actions.addAll(actions) }

    /**
     * Sets the [resource](https://docs.aws.amazon.com/IAM/latest/UserGuide/reference_policies_elements_resource.html)
     * field of the statement
     */
    fun resource(resource: String) { this.resource = resource }

    /**
     * Sets the [principal](https://docs.aws.amazon.com/IAM/latest/UserGuide/reference_policies_elements_principal.html)
     * field of the statement
     */
    fun principal(principalBuilderBlock: PrincipalBuilder.() -> Unit) {
        if (this.principal != null) {
            throw InvalidStatementException("A statement can only have one policy")
        }

        val builder = PrincipalBuilder()
        principalBuilderBlock.invoke(builder)
        this.principal = builder.build()
    }

    internal fun build(sid: String): Statement {
        if (actions.isEmpty()) {
            throw InvalidStatementException("A statement must contain at least 1 action")
        }

        return Statement(
            sid,
            effect ?: throw InvalidStatementException("Statement must specify an effect"),
            principal,
            Collections.unmodifiableList(actions),
            resource
        )
    }
}

class InvalidStatementException : RuntimeException {
    constructor(message: String, cause: Throwable) : super (message, cause)
    constructor(message: String) : super(message)
}

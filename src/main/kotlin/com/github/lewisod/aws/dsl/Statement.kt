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
    @SerialName("Sid") val sid: String? = null,
    @SerialName("Effect") val effect: Effect,
    @SerialName("Principal") val principal: Principal? = null,
    @SerialName("NotPrincipal") val notPrincipal: Principal? = null,
    @SerialName("Action") val action: List<String> = listOf(),
    @SerialName("NotAction") val notAction: List<String> = listOf(),
    @SerialName("Resource") val resource: String? = null,
    @SerialName("NotResource") val notResource: String? = null
)

/**
 * Convert the [Statement] to it's AWS-compliant JSON
 */
fun Statement.toJson(): String = JsonEncoder.serialize(Statement.serializer(), this)

@PolicyElementBuilder
class StatementBuilder internal constructor() {

    private var effect: Effect? = null
    private val actions = mutableListOf<String>()
    private val notActions = mutableListOf<String>()
    private var resource: String? = null
    private var notResource: String? = null
    private var principal: Principal? = null
    private var notPrincipal: Principal? = null

    /**
     * Sets the [Effect](https://docs.aws.amazon.com/IAM/latest/UserGuide/reference_policies_elements_effect.html) field
     * of the statement
     */
    fun effect(effect: Effect) {
        if (this.effect != null) {
            throw InvalidStatementException("A statement can only specify one Effect")
        }
        this.effect = effect
    }

    /**
     * Sets the [Action](https://docs.aws.amazon.com/IAM/latest/UserGuide/reference_policies_elements_action.html) field
     * of the statement
     */
    fun action(vararg actions: String) {
        if (this.notActions.isNotEmpty()) {
            throw InvalidStatementException("A statement can only specify one of Action or NotAction")
        }
        this.actions.addAll(actions)
    }

    /**
     * Sets the [NotAction](https://docs.aws.amazon.com/IAM/latest/UserGuide/reference_policies_elements_notaction.html)
     * field of the statement
     */
    fun notAction(vararg actions: String) {
        if (this.actions.isNotEmpty()) {
            throw InvalidStatementException("A statement can only specify one of Action or NotAction")
        }
        this.notActions.addAll(actions)
    }

    /**
     * Sets the [Resource](https://docs.aws.amazon.com/IAM/latest/UserGuide/reference_policies_elements_resource.html)
     * field of the statement
     */
    fun resource(resource: String) {
        if (this.notResource != null) {
            throw InvalidStatementException("A statement can only specify one of Resource or NotResource")
        }
        this.resource = resource
    }

    /**
     * Sets the [NotResource](https://docs.aws.amazon.com/IAM/latest/UserGuide/reference_policies_elements_notresource.html)
     * field of the statement
     */
    fun notResource(resource: String) {
        if (this.resource != null) {
            throw InvalidStatementException("A statement can only specify one of Resource or NotResource")
        }
        this.notResource = resource
    }

    /**
     * Sets the [Principal](https://docs.aws.amazon.com/IAM/latest/UserGuide/reference_policies_elements_principal.html)
     * field of the statement
     */
    fun principal(principalBuilderBlock: PrincipalBuilder.() -> Unit) {
        if (this.principal != null || this.notPrincipal != null) {
            throw InvalidStatementException("A statement can only specify one Principal")
        }

        val builder = PrincipalBuilder()
        principalBuilderBlock.invoke(builder)
        this.principal = builder.build()
    }

    /**
     * Sets the [NotPrincipal](https://docs.aws.amazon.com/IAM/latest/UserGuide/reference_policies_elements_notprincipal.html)
     * field of the statement
     */
    fun notPrincipal(principalBuilderBlock: PrincipalBuilder.() -> Unit) {
        if (this.principal != null || this.notPrincipal != null) {
            throw InvalidStatementException("A statement can only specify one Principal")
        }

        val builder = PrincipalBuilder()
        principalBuilderBlock.invoke(builder)
        this.principal = builder.build()
    }

    fun build(): Statement = build(null)

    internal fun build(sid: String?): Statement {
        if (actions.isEmpty()) {
            throw InvalidStatementException("A statement must contain at least 1 Action")
        }

        return Statement(
            sid,
            effect ?: throw InvalidStatementException("Statement must specify an effect"),
            principal,
            notPrincipal,
            Collections.unmodifiableList(actions),
            Collections.unmodifiableList(notActions),
            resource,
            notResource
        )
    }
}

class InvalidStatementException : RuntimeException {
    constructor(message: String, cause: Throwable) : super (message, cause)
    constructor(message: String) : super(message)
}

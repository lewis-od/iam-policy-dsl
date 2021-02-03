package uk.co.lewisod.aws.dsl.policy

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
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
    @SerialName("Action") val action: List<String>,
    @SerialName("Resource") val resource: String
)

/**
 * Convert the [Statement] to it's AWS-compliant JSON
 */
fun Statement.toJson(): String = Json.encodeToString(this)

class StatementBuilder internal constructor() {

    private var effect: Effect? = null
    private val actions = mutableListOf<String>()
    private var resource: String? = null

    /**
     * Sets the [effect](https://docs.aws.amazon.com/IAM/latest/UserGuide/reference_policies_elements_effect.html) field
     * of the statement
     */
    fun effect(effect: Effect) { this.effect = effect }

    /**
     * Sets the [action](https://docs.aws.amazon.com/IAM/latest/UserGuide/reference_policies_elements_action.html) field
     * of the statement
     */
    fun action(action: String) { this.actions.add(action) }

    /**
     * Sets the [resource](https://docs.aws.amazon.com/IAM/latest/UserGuide/reference_policies_elements_resource.html)
     * field of the statement
     */
    fun resource(resource: String) { this.resource = resource }

    internal fun build(sid: String): Statement {
        if (actions.isEmpty()) {
            throw InvalidStatementException("A statement must contain at least 1 action")
        }

        return Statement(
            sid,
            effect ?: throw InvalidStatementException("Statement must specify an effect"),
            Collections.unmodifiableList(actions),
            resource ?: throw InvalidStatementException("Statement must specify a resource")
        )
    }
}

class InvalidStatementException : RuntimeException {
    constructor(message: String, cause: Throwable) : super (message, cause)
    constructor(message: String) : super(message)
}

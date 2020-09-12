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

@Serializable
data class Statement internal constructor(
    val Sid: String,
    val Effect: Effect,
    val Action: List<String>,
    val Resource: String
)

fun Statement.toJson(): String {
    return Json.encodeToString(this)
}

class StatementBuilder internal constructor() {

    private var effect: Effect? = null
    private val actions = mutableListOf<String>()
    private var resource: String? = null

    fun effect(effect: Effect) { this.effect = effect }
    fun action(action: String) { this.actions.add(action) }
    fun resource(resource: String) { this.resource = resource }

    internal fun build(sid: String): Statement {
        if (actions.isEmpty()) {
            throw InvalidStatementException("A statement must contain at least 1 action")
        }

        try {
            return Statement(sid, effect!!, Collections.unmodifiableList(actions), resource!!)
        } catch (e: NullPointerException) {
            throw InvalidStatementException("Statement missing value for field", e)
        }
    }
}

class InvalidStatementException : RuntimeException {
    constructor(message: String, cause: Throwable) : super (message, cause)
    constructor(message: String) : super(message)
}

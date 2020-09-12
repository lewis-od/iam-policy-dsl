package uk.co.lewisod.aws.dsl.policy

import java.util.Collections

enum class Effect {
    ALLOW,
    DENY
}

data class Statement internal constructor(
    val sid: String,
    val effect: Effect,
    val action: List<String>,
    val resource: String
)

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

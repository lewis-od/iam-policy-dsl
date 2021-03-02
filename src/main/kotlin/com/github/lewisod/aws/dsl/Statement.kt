package com.github.lewisod.aws.dsl

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.CompositeEncoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable
enum class Effect {
    @SerialName("Allow") ALLOW,
    @SerialName("Deny") DENY
}

/**
 * Represents a statement within an IAM policy
 */
@Serializable(with = StatementSerializer::class)
data class Statement internal constructor(
    val effect: Effect,
    val action: ActionElement,
    val sid: String? = null,
    val principal: PrincipalElement? = null,
    val resource: ResourceElement? = null,
    val condition: Condition? = null
)

object StatementSerializer : KSerializer<Statement> {

    private val conditionSerializer = MapSerializer(
            String.serializer(),
            MapSerializer(String.serializer(), ListSerializer(String.serializer())))

    override fun serialize(encoder: Encoder, value: Statement) {
        val composite = encoder.beginStructure(descriptor)
        if (value.sid != null) {
            composite.encodeStringElement(descriptor, 0, value.sid)
        }
        composite.encodeSerializableElement(descriptor, 1, Effect.serializer(), value.effect)
        if (value.principal != null) {
            val principalIndex = calculateElementIndex(value.principal, 2)
            composite.encodeSerializableElement(descriptor, principalIndex, Principal.serializer(), value.principal.value)
        }
        encodeList(composite, value.action, 4)
        if (value.resource != null) {
            encodeList(composite, value.resource, 6)
        }
        if (value.condition != null) {
            composite.encodeSerializableElement(
                    descriptor,
                    8,
                    conditionSerializer,
                    value.condition)
        }
        composite.endStructure(descriptor)
    }

    private fun encodeList(composite: CompositeEncoder, element: NegatablePolicyElement<List<String>>, index: Int) {
        val actionIndex = calculateElementIndex(element, index)
        val values = element.value
        if (values.size > 1) {
            composite.encodeSerializableElement(descriptor, actionIndex, ListSerializer(String.serializer()), values)
        } else {
            composite.encodeStringElement(descriptor, actionIndex, values.first())
        }
    }

    private fun <T> calculateElementIndex(element: NegatablePolicyElement<T>, startIndex: Int): Int =
        if (element.isNegated) startIndex + 1 else startIndex

    // Not used
    override fun deserialize(decoder: Decoder): Statement = Statement(Effect.ALLOW, ActionElement(listOf(), false))

    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("StatementSerializer") {
        element<String>("Sid", isOptional = true)
        element<Effect>("Effect")
        element<Principal>("Principal", isOptional = true)
        element<Principal>("NotPrincipal", isOptional = true)
        element<List<String>>("Action", isOptional = true)
        element<List<String>>("NotAction", isOptional = true)
        element<String>("Resource", isOptional = true)
        element<String>("NotResource", isOptional = true)
        element<String>("Condition", isOptional = true)
    }
}

/**
 * Convert the [Statement] to it's AWS-compliant JSON
 */
fun Statement.toJson(): String = JsonEncoder.serialize(Statement.serializer(), this)

@PolicyElementBuilder
class StatementBuilder internal constructor() {

    private var effect: Effect? = null
    private var actions: ActionElement = ActionElement(mutableListOf(), isNegated = false)
    private var resource: ResourceElement? = null
    private var principal: PrincipalElement? = null
    private val conditionMap = mutableMapOf<String, ConditionEntry>()

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
        if (this.actions.isNegated && this.actions.value.isNotEmpty()) {
            throw InvalidStatementException("A statement can only specify one of Action or NotAction")
        }
        this.actions = ActionElement(this.actions.value + actions, isNegated = false)
    }

    /**
     * Sets the [NotAction](https://docs.aws.amazon.com/IAM/latest/UserGuide/reference_policies_elements_notaction.html)
     * field of the statement
     */
    fun notAction(vararg actions: String) {
        if (!this.actions.isNegated && this.actions.value.isNotEmpty()) {
            throw InvalidStatementException("A statement can only specify one of Action or NotAction")
        }
        this.actions = ActionElement(this.actions.value + actions, isNegated = true)
    }

    /**
     * Sets the [Resource](https://docs.aws.amazon.com/IAM/latest/UserGuide/reference_policies_elements_resource.html)
     * field of the statement
     */
    fun resource(vararg resource: String) {
        if (this.resource != null && this.resource!!.value.isNotEmpty() && this.resource!!.isNegated) {
            throw InvalidStatementException("A statement can only specify either Resource or NotResource")
        }
        val existingResources = if (this.resource != null) this.resource!!.value else listOf()
        this.resource = ResourceElement(existingResources + resource, isNegated = false)
    }

    /**
     * Sets the [NotResource](https://docs.aws.amazon.com/IAM/latest/UserGuide/reference_policies_elements_notresource.html)
     * field of the statement
     */
    fun notResource(vararg resources: String) {
        if (this.resource != null && this.resource!!.value.isNotEmpty() && !this.resource!!.isNegated) {
            throw InvalidStatementException("A statement can only specify either Resource or NotResource")
        }
        val existingResources = if (this.resource != null) this.resource!!.value else listOf()
        this.resource = ResourceElement(existingResources + resources, isNegated = true)
    }

    /**
     * Sets the [Principal](https://docs.aws.amazon.com/IAM/latest/UserGuide/reference_policies_elements_principal.html)
     * field of the statement
     */
    fun principal(init: PrincipalBuilder.() -> Unit) =
        createPrincipal(init, isNegated = false)

    /**
     * Sets the [NotPrincipal](https://docs.aws.amazon.com/IAM/latest/UserGuide/reference_policies_elements_notprincipal.html)
     * field of the statement
     */
    fun notPrincipal(init: PrincipalBuilder.() -> Unit) =
        createPrincipal(init, isNegated = true)

    private fun createPrincipal(init: PrincipalBuilder.() -> Unit, isNegated: Boolean) {
        if (this.principal != null) {
            throw InvalidStatementException("A statement can only specify one Principal")
        }

        val builder = PrincipalBuilder()
        builder.init()
        this.principal = PrincipalElement(builder.build(), isNegated)
    }

    fun condition(operator: String, init: ConditionEntryBuilder.() -> Unit) {
        val builder = ConditionEntryBuilder()
        builder.init()
        conditionMap[operator] = builder.build()
    }

    internal fun build(sid: String?): Statement {
        if (actions.value.isEmpty()) {
            throw InvalidStatementException("A statement must contain at least 1 Action")
        }

        return Statement(
            effect ?: throw InvalidStatementException("Statement must specify an effect"),
            actions,
            sid,
            principal,
            resource,
            if (conditionMap.isNotEmpty()) conditionMap.toMap() else null
        )
    }
}

class InvalidStatementException : RuntimeException {
    constructor(message: String, cause: Throwable) : super (message, cause)
    constructor(message: String) : super(message)
}

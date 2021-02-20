package com.github.lewisod.aws.dsl

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encodeToString
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json

enum class PrincipalType(val textValue: String) {
    AWS("AWS"),
    CANONICAL_USER("CanonicalUser"),
    FEDERATED("Federated"),
    SERVICE("Service"),
}

@Serializable(with = PrincipalSerializer::class)
data class Principal internal constructor(
    val type: PrincipalType,
    val values: List<String>
)

object PrincipalSerializer : KSerializer<Principal> {

    override fun serialize(encoder: Encoder, value: Principal) {
        val descriptorIndex: Int = getIndexForType(value.type)

        val composite = encoder.beginStructure(descriptor)
        if (value.values.size > 1) {
            composite.encodeSerializableElement(descriptor, descriptorIndex, ListSerializer(String.serializer()), value.values)
        } else {
            composite.encodeStringElement(descriptor, descriptorIndex, value.values.first())
        }
        composite.endStructure(descriptor)
    }

    // Never used
    override fun deserialize(decoder: Decoder): Principal = Principal(PrincipalType.AWS, listOf())

    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("Principal") {
        element<String>("AWS")
        element<String>("CanonicalUser")
        element<String>("Federated")
        element<String>("Service")
    }

    private fun getIndexForType(type: PrincipalType): Int = when (type) {
        PrincipalType.AWS -> 0
        PrincipalType.CANONICAL_USER -> 1
        PrincipalType.FEDERATED -> 2
        PrincipalType.SERVICE -> 3
    }
}

fun Principal.toJson(): String = Json.encodeToString(this)

class PrincipalBuilder internal constructor() {

    private var type: PrincipalType? = null
    private var values: List<String> = listOf()

    fun aws(vararg values: String) {
        checkTypeNotAlreadySpecified()
        this.type = PrincipalType.AWS
        this.values = values.asList()
    }

    fun canonicalUser(value: String) {
        checkTypeNotAlreadySpecified()
        this.type = PrincipalType.CANONICAL_USER
        this.values = listOf(value)
    }

    fun federated(value: String) {
        checkTypeNotAlreadySpecified()
        this.type = PrincipalType.FEDERATED
        this.values = listOf(value)
    }

    fun service(vararg values: String) {
        checkTypeNotAlreadySpecified()
        this.type = PrincipalType.SERVICE
        this.values = values.asList()
    }

    private fun checkTypeNotAlreadySpecified() {
        if (type != null) {
            throw InvalidPrincipalException("A principal can only have one type, but multiple specified")
        }
    }

    internal fun build(): Principal = Principal(
        type ?: throw InvalidPrincipalException("No principal specified"),
        values
    )
}

class InvalidPrincipalException : RuntimeException {
    constructor(message: String, cause: Throwable) : super (message, cause)
    constructor(message: String) : super(message)
}

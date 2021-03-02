package com.github.lewisod.aws.dsl

typealias ConditionEntry = Map<String, List<String>>

class ConditionEntryBuilder {

    private val conditionEntryMap = mutableMapOf<String, List<String>>()

    infix fun String.to(value: String) {
        conditionEntryMap[this] = listOf(value)
    }

    infix fun String.to(value: List<String>) {
        conditionEntryMap[this] = value
    }

    infix fun String.or(other: String): List<String> = listOf(this, other)

    infix fun List<String>.or(other: String): List<String> = this.toMutableList().apply { add(other) }.toList()

    fun build(): ConditionEntry = conditionEntryMap.toMap()
}

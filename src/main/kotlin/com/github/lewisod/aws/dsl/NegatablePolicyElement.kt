package com.github.lewisod.aws.dsl

data class NegatablePolicyElement<T>(val value: T, val isNegated: Boolean = false)

typealias PrincipalElement = NegatablePolicyElement<Principal>
typealias ActionElement = NegatablePolicyElement<List<String>>
typealias ResourceElement = NegatablePolicyElement<List<String>>

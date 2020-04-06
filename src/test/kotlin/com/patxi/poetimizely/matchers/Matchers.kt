package com.patxi.poetimizely.matchers

import io.kotest.matchers.Matcher
import io.kotest.matchers.MatcherResult
import java.lang.reflect.Modifier

fun publicStaticMethod(methodName: String, vararg parameterTypes: Class<*>) = object : Matcher<Class<*>> {
    override fun test(value: Class<*>) =
        MatcherResult(
            try {
                value.getMethod(methodName, *parameterTypes).modifiers and Modifier.STATIC != 0
            } catch (e: NoSuchMethodException) {
                false
            },
            "Class ${value.name} should have static method $methodName(${parameterTypes.joinToString(", ") { it.name }})",
            "Class ${value.name} should NOT have static method $methodName(${parameterTypes.joinToString(", ") { it.name }})"
        )
}

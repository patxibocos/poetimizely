package com.patxi.poetimizely.matchers

import io.kotest.matchers.Matcher
import io.kotest.matchers.MatcherResult
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldHave
import java.lang.reflect.Modifier

fun publicStaticMethod(methodName: String, vararg parameterTypes: Class<*>, returnType: Class<*>? = null) =
    object : Matcher<Class<*>> {
        override fun test(value: Class<*>) =
            MatcherResult(
                try {
                    if (returnType == null) {
                        value.getMethod(methodName, *parameterTypes).modifiers and Modifier.STATIC != 0
                    } else {
                        value.declaredMethods.find {
                            it.parameterTypes.contentEquals(parameterTypes) && it.returnType == returnType
                        } != null
                    }
                } catch (e: NoSuchMethodException) {
                    false
                },
                "Class ${value.name} should have static method $methodName(${parameterTypes.joinToString(", ") { it.name }})",
                "Class ${value.name} should NOT have static method $methodName(${parameterTypes.joinToString(", ") { it.name }})"
            )
    }

private fun kotlinObject() = object : Matcher<Class<*>> {
    override fun test(value: Class<*>) =
        MatcherResult(
            try {
                value.getField("INSTANCE").get(null) != null
            } catch (e: Exception) {
                false
            },
            "Class ${value.name} should be a Kotlin object",
            "Class ${value.name} should NOT be a Kotlin object"
        )
}

infix fun Class<*>.shouldBeKotlinObject(callback: (Any) -> Unit) {
    this shouldBe kotlinObject()
    callback(this.getField("INSTANCE").get(null))
}

private fun field(clazz: Class<Any>, fieldName: String) = object : Matcher<Any> {
    override fun test(value: Any): MatcherResult =
        MatcherResult(
            try {
                clazz.getDeclaredField(fieldName).apply { isAccessible = true }.get(this)
                true
            } catch (e: Exception) {
                false
            },
            "Class $javaClass should have a field named $fieldName",
            "Class $javaClass should NOT have a field named $fieldName"
        )
}

fun Any.shouldHaveField(fieldName: String, callback: (Any) -> Unit = {}) {
    this shouldHave field(javaClass, fieldName)
    callback(javaClass.getDeclaredField(fieldName).apply { isAccessible = true }.get(this))
}

package com.patxi.poetimizely.generator

import com.optimizely.ab.Optimizely
import com.patxi.poetimizely.generator.optimizely.Feature
import com.patxi.poetimizely.generator.optimizely.Variable
import com.patxi.poetimizely.matchers.publicStaticMethod
import com.patxi.poetimizely.matchers.shouldBeKotlinObject
import com.patxi.poetimizely.matchers.shouldHaveField
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldHave

class FeaturesGeneratorTest : BehaviorSpec({

    given("A list of Optimizely features") {
        val features = listOf(
            Feature("new_checkout_page", listOf(Variable("variable_key_1", "boolean"))),
            Feature("new_login_page", listOf(Variable("variable_key_2", "string"))),
            Feature("new_sign_up_page", listOf(Variable("variable_key_3", "double"))),
            Feature("new_onboarding_page", listOf(Variable("variable_key_4", "integer")))
        )
        and("A package name") {
            val packageName = "what.ever.pack.age"
            `when`("Compiling the generated code for features") {
                val featuresCode = generateFeaturesCode(features, packageName)
                val compilationResult = KotlinCompilation().apply {
                    sources = listOf(SourceFile.kotlin("Features.kt", featuresCode))
                    inheritClassPath = true
                    messageOutputStream = System.out
                }.compile()
                then("Generated code compiles") {
                    compilationResult.exitCode shouldBe KotlinCompilation.ExitCode.OK
                }
                then("Kotlin objects for features and variables exist with their properties") {
                    compilationResult.classLoader.loadClass("$packageName.Features\$NewCheckoutPage") shouldBeKotlinObject { feature ->
                        feature.getFieldValueFromParentClass("key") shouldBe "new_checkout_page"
                        feature.shouldHaveField("variableKey1") { featureVariable ->
                            featureVariable.getFieldValue("featureKey") shouldBe "new_checkout_page"
                            featureVariable.getFieldValue("variableKey") shouldBe "variable_key_1"
                        }
                    }
                    compilationResult.classLoader.loadClass("$packageName.Features\$NewLoginPage") shouldBeKotlinObject { feature ->
                        feature.getFieldValueFromParentClass("key") shouldBe "new_login_page"
                        feature.shouldHaveField("variableKey2") { featureVariable ->
                            featureVariable.getFieldValue("featureKey") shouldBe "new_login_page"
                            featureVariable.getFieldValue("variableKey") shouldBe "variable_key_2"
                        }
                    }
                    compilationResult.classLoader.loadClass("$packageName.Features\$NewSignUpPage") shouldBeKotlinObject { feature ->
                        feature.getFieldValueFromParentClass("key") shouldBe "new_sign_up_page"
                        feature.shouldHaveField("variableKey3") { featureVariable ->
                            featureVariable.getFieldValue("featureKey") shouldBe "new_sign_up_page"
                            featureVariable.getFieldValue("variableKey") shouldBe "variable_key_3"
                        }
                    }
                    compilationResult.classLoader.loadClass("$packageName.Features\$NewOnboardingPage") shouldBeKotlinObject { feature ->
                        feature.getFieldValueFromParentClass("key") shouldBe "new_onboarding_page"
                        feature.shouldHaveField("variableKey4") { featureVariable ->
                            featureVariable.getFieldValue("featureKey") shouldBe "new_onboarding_page"
                            featureVariable.getFieldValue("variableKey") shouldBe "variable_key_4"
                        }
                    }
                }
                then("Extension functions haven been created") {
                    val featureClass = compilationResult.classLoader.loadClass("$packageName.Features")
                    val extensionFunctionContainerClass =
                        compilationResult.classLoader.loadClass("$packageName.FeaturesKt")
                    extensionFunctionContainerClass shouldHave publicStaticMethod(
                        "isFeatureEnabled",
                        Optimizely::class.java,
                        featureClass,
                        String::class.java,
                        Map::class.java
                    )
                    val featureVariableClass = compilationResult.classLoader.loadClass("$packageName.FeatureVariable")
                    extensionFunctionContainerClass shouldHave publicStaticMethod(
                        "getFeatureVariable",
                        Optimizely::class.java,
                        featureVariableClass,
                        String::class.java,
                        Map::class.java,
                        returnType = Boolean::class.javaObjectType
                    )
                    extensionFunctionContainerClass shouldHave publicStaticMethod(
                        "getFeatureVariable",
                        Optimizely::class.java,
                        featureVariableClass,
                        String::class.java,
                        Map::class.java,
                        returnType = String::class.java
                    )
                    extensionFunctionContainerClass shouldHave publicStaticMethod(
                        "getFeatureVariable",
                        Optimizely::class.java,
                        featureVariableClass,
                        String::class.java,
                        Map::class.java,
                        returnType = Double::class.javaObjectType
                    )
                    extensionFunctionContainerClass shouldHave publicStaticMethod(
                        "getFeatureVariable",
                        Optimizely::class.java,
                        featureVariableClass,
                        String::class.java,
                        Map::class.java,
                        returnType = Int::class.javaObjectType
                    )
                }
            }
        }
    }
})

private fun Any.getFieldValue(fieldName: String): Any =
    javaClass.getDeclaredField(fieldName).apply { isAccessible = true }.get(this)

private fun Any.getFieldValueFromParentClass(fieldName: String): Any =
    javaClass.superclass.getDeclaredField(fieldName).apply { isAccessible = true }.get(this)

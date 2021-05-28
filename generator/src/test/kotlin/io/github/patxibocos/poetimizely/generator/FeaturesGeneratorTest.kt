package io.github.patxibocos.poetimizely.generator

import com.optimizely.ab.Optimizely
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import io.github.patxibocos.poetimizely.generator.optimizely.Feature
import io.github.patxibocos.poetimizely.generator.optimizely.Variable
import io.github.patxibocos.poetimizely.matchers.parentClassShouldHaveFieldWithValue
import io.github.patxibocos.poetimizely.matchers.publicStaticMethod
import io.github.patxibocos.poetimizely.matchers.shouldBeKotlinObject
import io.github.patxibocos.poetimizely.matchers.shouldHaveField
import io.github.patxibocos.poetimizely.matchers.shouldHaveFieldWithValue
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.data.forAll
import io.kotest.data.row
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
                    forAll(
                        row("new_checkout_page", "NewCheckoutPage", "variable_key_1", "variableKey1"),
                        row("new_login_page", "NewLoginPage", "variable_key_2", "variableKey2"),
                        row("new_sign_up_page", "NewSignUpPage", "variable_key_3", "variableKey3"),
                        row("new_onboarding_page", "NewOnboardingPage", "variable_key_4", "variableKey4")
                    ) { featureKey, generatedFeatureClassName, variableKey, generatedFeatureVariableName ->
                        val feature =
                            compilationResult.classLoader.loadClass("$packageName.Features\$$generatedFeatureClassName")
                                .shouldBeKotlinObject()
                        feature.parentClassShouldHaveFieldWithValue("key", featureKey)
                        feature.shouldHaveField(generatedFeatureVariableName) { featureVariable ->
                            featureVariable.shouldHaveFieldWithValue("featureKey", featureKey)
                            featureVariable.shouldHaveFieldWithValue("variableKey", variableKey)
                        }
                    }
                }
                then("Optimizely extension functions for features and variables have been created") {
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
                    listOf(
                        Boolean::class.javaObjectType,
                        String::class.java,
                        Double::class.javaObjectType,
                        Int::class.javaObjectType
                    ).forEach { returnTypeClass ->
                        extensionFunctionContainerClass shouldHave publicStaticMethod(
                            "getFeatureVariable",
                            Optimizely::class.java,
                            featureVariableClass,
                            String::class.java,
                            Map::class.java,
                            returnType = returnTypeClass
                        )
                    }
                }
            }
        }
    }
})

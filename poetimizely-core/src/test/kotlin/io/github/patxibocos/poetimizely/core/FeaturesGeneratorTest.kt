package io.github.patxibocos.poetimizely.core

import com.optimizely.ab.Optimizely
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import io.github.patxibocos.poetimizely.core.generator.generateFeaturesCode
import io.github.patxibocos.poetimizely.core.optimizely.Feature
import io.github.patxibocos.poetimizely.core.optimizely.Variable
import io.github.patxibocos.poetimizely.matchers.parentClassShouldHaveFieldWithValue
import io.github.patxibocos.poetimizely.matchers.publicStaticMethod
import io.github.patxibocos.poetimizely.matchers.shouldBeKotlinObject
import io.github.patxibocos.poetimizely.matchers.shouldHaveField
import io.github.patxibocos.poetimizely.matchers.shouldHaveFieldWithValue
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.datatest.withData
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldHave
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi

@OptIn(ExperimentalCompilerApi::class)
class FeaturesGeneratorTest :
    BehaviorSpec({

        given("A list of Optimizely features") {
            val features =
                listOf(
                    Feature("new_checkout_page", listOf(Variable("variable_key_1", "boolean"))),
                    Feature("new_login_page", listOf(Variable("variable_key_2", "string"))),
                    Feature("new_sign_up_page", listOf(Variable("variable_key_3", "double"))),
                    Feature("new_onboarding_page", listOf(Variable("variable_key_4", "integer"))),
                )
            and("A package name") {
                val packageName = "what.ever.pack.age"
                `when`("Compiling the generated code for features") {
                    val featuresCode = generateFeaturesCode(features, packageName)
                    val compilationResult =
                        KotlinCompilation()
                            .apply {
                                sources = listOf(SourceFile.kotlin("Features.kt", featuresCode))
                                inheritClassPath = true
                                messageOutputStream = System.out
                            }.compile()
                    then("Generated code compiles") {
                        compilationResult.exitCode shouldBe KotlinCompilation.ExitCode.OK
                    }
                    and("Kotlin objects for features and variables exist with their properties") {
                        data class FeatureTestCase(
                            val featureKey: String,
                            val generatedFeatureClassName: String,
                            val variableKey: String,
                            val generatedFeatureVariableName: String,
                        )
                        withData(
                            FeatureTestCase("new_checkout_page", "NewCheckoutPage", "variable_key_1", "variableKey1"),
                            FeatureTestCase("new_login_page", "NewLoginPage", "variable_key_2", "variableKey2"),
                            FeatureTestCase("new_sign_up_page", "NewSignUpPage", "variable_key_3", "variableKey3"),
                            FeatureTestCase(
                                "new_onboarding_page",
                                "NewOnboardingPage",
                                "variable_key_4",
                                "variableKey4",
                            ),
                        ) { testCase ->
                            val feature =
                                compilationResult.classLoader
                                    .loadClass("$packageName.Features$${testCase.generatedFeatureClassName}")
                                    .shouldBeKotlinObject()
                            feature.parentClassShouldHaveFieldWithValue("key", testCase.featureKey)
                            feature.shouldHaveField(testCase.generatedFeatureVariableName) { featureVariable ->
                                featureVariable.shouldHaveFieldWithValue("featureKey", testCase.featureKey)
                                featureVariable.shouldHaveFieldWithValue("variableKey", testCase.variableKey)
                            }
                        }
                    }
                    then("Optimizely extension functions for features and variables have been created") {
                        val featureClass = compilationResult.classLoader.loadClass("$packageName.Features")
                        val extensionFunctionContainerClass =
                            compilationResult.classLoader.loadClass("$packageName.FeaturesKt")
                        extensionFunctionContainerClass shouldHave publicStaticMethod("getAllFeatures")
                        val allFeatures =
                            listOf(
                                compilationResult.classLoader
                                    .loadClass("$packageName.Features\$NewCheckoutPage")
                                    .shouldBeKotlinObject(),
                                compilationResult.classLoader
                                    .loadClass("$packageName.Features\$NewLoginPage")
                                    .shouldBeKotlinObject(),
                                compilationResult.classLoader
                                    .loadClass("$packageName.Features\$NewSignUpPage")
                                    .shouldBeKotlinObject(),
                                compilationResult.classLoader
                                    .loadClass("$packageName.Features\$NewOnboardingPage")
                                    .shouldBeKotlinObject(),
                            )
                        extensionFunctionContainerClass.getMethod("getAllFeatures").invoke(null) shouldBe allFeatures
                        extensionFunctionContainerClass shouldHave
                            publicStaticMethod(
                                "isFeatureEnabled",
                                Optimizely::class.java,
                                featureClass,
                                String::class.java,
                                Map::class.java,
                            )
                        val featureVariableClass =
                            compilationResult.classLoader.loadClass("$packageName.FeatureVariable")
                        listOf(
                            Boolean::class.javaObjectType,
                            String::class.java,
                            Double::class.javaObjectType,
                            Int::class.javaObjectType,
                        ).forEach { returnTypeClass ->
                            extensionFunctionContainerClass shouldHave
                                publicStaticMethod(
                                    "getFeatureVariable",
                                    Optimizely::class.java,
                                    featureVariableClass,
                                    String::class.java,
                                    Map::class.java,
                                    returnType = returnTypeClass,
                                )
                        }
                    }
                }
            }
        }
    })

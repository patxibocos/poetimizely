package com.patxi.poetimizely.generator

import com.optimizely.ab.Optimizely
import com.patxi.poetimizely.generator.optimizely.OptimizelyExperiment
import com.patxi.poetimizely.generator.optimizely.OptimizelyVariation
import com.patxi.poetimizely.matchers.publicStaticMethod
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldHave
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.mockk

class ExperimentsGeneratorTest : BehaviorSpec({

    given("An Optimizely experiment") {
        val experimentKey = "TEST-EXPERIMENT"
        val variationKey = "TEST-VARIATION"
        val optimizelyExperiment =
            OptimizelyExperiment(experimentKey, variations = listOf(OptimizelyVariation(variationKey)))
        and("A package name") {
            val packageName = "what.ever.pack.age"
            `when`("Compiling the generated code for experiment and its variations") {
                val experimentsCode = generateExperimentsCode(listOf(optimizelyExperiment), packageName)
                val compilationResult = KotlinCompilation().apply {
                    sources = listOf(SourceFile.kotlin("Experiments.kt", experimentsCode))
                    inheritClassPath = true
                    messageOutputStream = System.out
                }.compile()
                then("Generated code compiles") {
                    fun loadClass(className: String): Class<*> =
                        compilationResult.classLoader.loadClass("$packageName.$className")

                    compilationResult.exitCode shouldBe KotlinCompilation.ExitCode.OK
                    // Variations enum
                    val variationsClass = loadClass("TestExperimentVariations")
                    with(variationsClass.enumConstants) {
                        this shouldHaveSize optimizelyExperiment.variations.size
                        this.shouldBeInstanceOf<Array<Any>>()
                        this.map { it.getFieldValue("key") as String } shouldContainExactlyInAnyOrder optimizelyExperiment.variations.map { it.key }
                    }
                    // Experiment object
                    val testExperimentClass = loadClass("Experiments\$TestExperiment")
                    val experimentObject = testExperimentClass.getField("INSTANCE").get(null)
                    experimentObject.getFieldValueFromParentClass("key") shouldBe experimentKey
                    experimentObject.getFieldValueFromParentClass("variations") shouldBe variationsClass.enumConstants
                    // Optimizely extension functions
                    val experimentClass = loadClass("Experiments")
                    val extensionFunctionContainerClass = loadClass("ExperimentsKt")
                    with(extensionFunctionContainerClass) {
                        this shouldHave publicStaticMethod("getAllExperiments", Optimizely::class.java)
                        this.getMethod("getAllExperiments", Optimizely::class.java)
                            .invoke(null, mockk<Optimizely>()) shouldBe listOf(experimentObject)
                        this shouldHave publicStaticMethod(
                            "getVariationForExperiment",
                            Optimizely::class.java,
                            experimentClass,
                            String::class.java,
                            Map::class.java
                        )
                    }
                }
            }
        }
    }
})

private fun Any.getFieldValue(fieldName: String): Any =
    javaClass.getDeclaredField(fieldName).apply { isAccessible = true }.get(this)

private fun Any.getFieldValueFromParentClass(fieldName: String): Any =
    javaClass.superclass.getDeclaredField(fieldName).apply { isAccessible = true }.get(this)

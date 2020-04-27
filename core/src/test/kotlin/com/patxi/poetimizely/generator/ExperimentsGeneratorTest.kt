package com.patxi.poetimizely.generator

import com.optimizely.ab.Optimizely
import com.patxi.poetimizely.matchers.publicStaticMethod
import com.patxi.poetimizely.optimizely.OptimizelyExperiment
import com.patxi.poetimizely.optimizely.OptimizelyVariation
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
        and("A Generator for a package") {
            val packageName = "what.ever.pack.age"
            val experimentsGenerator = ExperimentsGenerator(packageName)
            `when`("Compiling the generated code for experiment and its variations") {
                val experimentsCode = experimentsGenerator.generate(listOf(optimizelyExperiment))
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
                    val experimentClass = loadClass("Experiments\$TestExperiment")
                    val experimentObject = experimentClass.getField("INSTANCE").get(null)
                    experimentObject.getFieldValue("key") shouldBe experimentKey
                    experimentObject.getFieldValue("variations") shouldBe variationsClass.enumConstants
                    // Optimizely extension functions
                    val baseExperimentClass = loadClass("BaseExperiment")
                    val extensionFunctionContainerClass = loadClass("ExperimentsKt")
                    with(extensionFunctionContainerClass) {
                        this shouldHave publicStaticMethod("getAllExperiments", Optimizely::class.java)
                        this.getMethod("getAllExperiments", Optimizely::class.java)
                            .invoke(null, mockk<Optimizely>()) shouldBe listOf(experimentObject)
                        this shouldHave publicStaticMethod(
                            "getVariationForExperiment",
                            Optimizely::class.java,
                            baseExperimentClass,
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

package io.github.patxibocos.poetimizely.core

import com.optimizely.ab.Optimizely
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import io.github.patxibocos.poetimizely.core.generator.generateExperimentsCode
import io.github.patxibocos.poetimizely.core.optimizely.Experiment
import io.github.patxibocos.poetimizely.core.optimizely.Variation
import io.github.patxibocos.poetimizely.matchers.parentClassShouldHaveFieldWithValue
import io.github.patxibocos.poetimizely.matchers.publicStaticMethod
import io.github.patxibocos.poetimizely.matchers.shouldBeKotlinObject
import io.github.patxibocos.poetimizely.matchers.shouldHaveFieldWithValue
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldBeSingleton
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldHave

class ExperimentsGeneratorTest : BehaviorSpec({

    given("An Optimizely experiment") {
        val experimentKey = "TEST-EXPERIMENT"
        val variationKey = "TEST-VARIATION"
        val optimizelyExperiment =
            Experiment(experimentKey, variations = listOf(Variation(variationKey)))
        and("A package name") {
            val packageName = "what.ever.pack.age"
            `when`("Compiling the generated code for experiment and its variations") {
                val experimentsCode = generateExperimentsCode(listOf(optimizelyExperiment), packageName)
                println(experimentsCode)
                val compilationResult =
                    KotlinCompilation().apply {
                        sources = listOf(SourceFile.kotlin("Experiments.kt", experimentsCode))
                        inheritClassPath = true
                        messageOutputStream = System.out
                    }.compile()
                then("Generated code compiles") {
                    compilationResult.exitCode shouldBe KotlinCompilation.ExitCode.OK
                }
                then("Kotlin objects for experiments and variations enums exist with their properties") {
                    val variationsClass =
                        compilationResult.classLoader.loadClass("$packageName.TestExperimentVariations")
                    with(variationsClass.enumConstants) {
                        this.shouldBeSingleton {
                            it.shouldHaveFieldWithValue("key", variationKey)
                        }
                    }
                    val experiment =
                        compilationResult.classLoader.loadClass("$packageName.Experiments\$TestExperiment")
                            .shouldBeKotlinObject()
                    experiment.parentClassShouldHaveFieldWithValue("key", experimentKey)
                    experiment.parentClassShouldHaveFieldWithValue("variations", variationsClass.enumConstants)
                }
                then("Optimizely extension function for experiments has been created") {
                    val experimentClass = compilationResult.classLoader.loadClass("$packageName.Experiments")
                    val extensionFunctionContainerClass =
                        compilationResult.classLoader.loadClass("$packageName.ExperimentsKt")
                    with(extensionFunctionContainerClass) {
                        this shouldHave publicStaticMethod("getAllExperiments")
                        val allExperiments =
                            listOf(
                                compilationResult.classLoader.loadClass("$packageName.Experiments\$TestExperiment")
                                    .shouldBeKotlinObject(),
                            )
                        this.getMethod("getAllExperiments").invoke(null) shouldBe allExperiments
                        this shouldHave
                            publicStaticMethod(
                                "getVariationForExperiment",
                                Optimizely::class.java,
                                experimentClass,
                                String::class.java,
                                Map::class.java,
                            )
                    }
                }
            }
        }
    }
})

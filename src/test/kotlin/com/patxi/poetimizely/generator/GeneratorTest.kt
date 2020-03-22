package com.patxi.poetimizely.generator

import com.optimizely.ab.Optimizely
import com.patxi.poetimizely.generator.base.ExperimentsClient
import com.patxi.poetimizely.generator.base.Variant
import com.patxi.poetimizely.optimizely.Variation
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldBeSingleton
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.mockk
import com.patxi.poetimizely.generator.base.Experiment as GeneratorExperiment
import com.patxi.poetimizely.optimizely.Experiment as OptimizelyExperiment

class GeneratorTest : BehaviorSpec({

    fun compileKotlinCode(vararg sourceCode: Pair<String, String>): KotlinCompilation.Result {
        val kotlinSource = sourceCode.map { SourceFile.kotlin(it.first, it.second) }
        return KotlinCompilation().apply {
            sources = kotlinSource
            inheritClassPath = true
            messageOutputStream = System.out
        }.compile()
    }

    given("An Optimizely experiment") {
        val experimentKey = "TEST_EXPERIMENT"
        val variationKey = "TEST_VARIATION"
        val optimizelyExperiment = OptimizelyExperiment(experimentKey, variations = listOf(Variation(variationKey)))
        and("A Generator for a package") {
            val packageName = "what.ever.pack.age"
            val generator = Generator(packageName)
            `when`("Compiling the generated code for experiment and its variants") {
                val experimentCode = generator.buildExperimentObject(optimizelyExperiment)
                val experimentsClientCode = generator.buildExperimentsClient(listOf(experimentKey))
                val compilationResult = compileKotlinCode(
                    "Experiment.kt" to experimentCode,
                    "ExperimentsClient.kt" to experimentsClientCode
                )
                then("Generated code compiles") {
                    compilationResult.exitCode shouldBe KotlinCompilation.ExitCode.OK
                    val variantsClass = compilationResult.classLoader.loadClass("$packageName.${experimentKey}Variants")
                    with(variantsClass.enumConstants) {
                        this shouldHaveSize optimizelyExperiment.variations.size
                        this.shouldBeInstanceOf<Array<Variant>>()
                        this.map { (it as Variant).key } shouldContainExactlyInAnyOrder optimizelyExperiment.variations.map { it.key }
                    }
                    val experimentClass = compilationResult.classLoader.loadClass("$packageName.$experimentKey")
                    experimentClass.getField("INSTANCE").get(null).shouldBeInstanceOf<GeneratorExperiment<Variant>>()
                    @Suppress("UNCHECKED_CAST")
                    val experimentObject =
                        experimentClass.getField("INSTANCE").get(null) as GeneratorExperiment<Variant>
                    experimentObject.key shouldBe experimentKey
                    experimentObject.variants shouldBe variantsClass.enumConstants
                    val experimentsClientClass =
                        compilationResult.classLoader.loadClass("$packageName.TestExperimentsClient")
                    experimentsClientClass.constructors.first().newInstance(mockk<Optimizely>(), "")
                        .shouldBeInstanceOf<ExperimentsClient> { experimentsClient ->
                            val allExperiments = experimentsClient.getAllExperiments()
                            allExperiments.shouldBeSingleton()
                            allExperiments.map { it.javaClass }.shouldContainExactly(experimentObject::class.java)
                        }
                }
            }
        }
    }
})

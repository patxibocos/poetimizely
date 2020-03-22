package com.patxi.poetimizely.generator

import com.patxi.poetimizely.generator.base.Variant
import com.patxi.poetimizely.optimizely.Variation
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
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
        `when`("Generating code for experiment and its variants") {
            val experimentCode = buildExperimentObject(optimizelyExperiment)
            then("Generated code compiles") {
                val experimentCompilationResult = compileKotlinCode("Experiment.kt" to experimentCode)
                experimentCompilationResult.exitCode shouldBe KotlinCompilation.ExitCode.OK
                val variantsClass = experimentCompilationResult.classLoader.loadClass("${experimentKey}Variants")
                with(variantsClass.enumConstants) {
                    this shouldHaveSize optimizelyExperiment.variations.size
                    this.shouldBeInstanceOf<Array<Variant>>()
                    this.map { (it as Variant).key } shouldContainExactlyInAnyOrder optimizelyExperiment.variations.map { it.key }
                }

                val experimentClass = experimentCompilationResult.classLoader.loadClass(experimentKey)
                experimentClass.getField("INSTANCE").get(null).shouldBeInstanceOf<GeneratorExperiment<Variant>>()
                @Suppress("UNCHECKED_CAST")
                val experimentObject = experimentClass.getField("INSTANCE").get(null) as GeneratorExperiment<Variant>
                experimentObject.key shouldBe experimentKey
                experimentObject.variants shouldBe variantsClass.enumConstants

                val experimentsClientCode = buildExperimentsClient(listOf(experimentObject::class))
                val experimentsClientCompilationResult = compileKotlinCode(
                    "Experiment.kt" to experimentCode,
                    "ExperimentsClient.kt" to experimentsClientCode
                )
                experimentsClientCompilationResult.exitCode shouldBe KotlinCompilation.ExitCode.OK
                val experimentsClientClass =
                    experimentsClientCompilationResult.classLoader.loadClass("TestExperimentsClient")
            }
        }
    }
})

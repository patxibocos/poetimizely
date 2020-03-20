package com.patxi.poetimizely.generator

import com.patxi.poetimizely.generator.base.Variant
import com.patxi.poetimizely.optimizely.Variation
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import io.kotlintest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotlintest.matchers.collections.shouldHaveSize
import io.kotlintest.matchers.types.shouldBeInstanceOf
import io.kotlintest.shouldBe
import io.kotlintest.specs.BehaviorSpec
import com.patxi.poetimizely.generator.base.Experiment as GeneratorExperiment
import com.patxi.poetimizely.optimizely.Experiment as OptimizelyExperiment

class GeneratorTest : BehaviorSpec({

    fun compileKotlinCode(fileName: String, kotlinSourceCode: String): KotlinCompilation.Result {
        val kotlinSource = SourceFile.kotlin(fileName, kotlinSourceCode)
        return KotlinCompilation().apply {
            sources = listOf(kotlinSource)
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
                val compilationResult = compileKotlinCode("Experiment.kt", experimentCode)
                // Assert compilation was successful
                compilationResult.exitCode shouldBe KotlinCompilation.ExitCode.OK
                // Assert Variants enum
                val variantsClass = compilationResult.classLoader.loadClass("${experimentKey}Variants")
                with(variantsClass.enumConstants) {
                    this shouldHaveSize optimizelyExperiment.variations.size
                    this.shouldBeInstanceOf<Array<Variant>>()
                    this.map { (it as Variant).key } shouldContainExactlyInAnyOrder optimizelyExperiment.variations.map { it.key }
                }
                // Assert Experiment object
                val experimentClass = compilationResult.classLoader.loadClass(experimentKey)
                with(experimentClass.getField("INSTANCE")) {
                    this.get(null).shouldBeInstanceOf<GeneratorExperiment<*>>()
                    val generatedExperiment = (this.get(null) as GeneratorExperiment<*>)
                    generatedExperiment.key shouldBe experimentKey
                    generatedExperiment.variants shouldBe variantsClass.enumConstants
                }
            }
        }
    }
})

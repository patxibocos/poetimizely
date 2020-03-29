package com.patxi.poetimizely.generator

import com.optimizely.ab.Optimizely
import com.patxi.poetimizely.generator.base.BaseExperiment
import com.patxi.poetimizely.generator.base.BaseVariation
import com.patxi.poetimizely.optimizely.OptimizelyExperiment
import com.patxi.poetimizely.optimizely.OptimizelyVariation
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.shouldBeInstanceOf

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
                    compilationResult.exitCode shouldBe KotlinCompilation.ExitCode.OK
                    val variationsClass =
                        compilationResult.classLoader.loadClass("$packageName.TestExperimentVariations")
                    with(variationsClass.enumConstants) {
                        this shouldHaveSize optimizelyExperiment.variations.size
                        this.shouldBeInstanceOf<Array<BaseVariation>>()
                        this.map { (it as BaseVariation).key } shouldContainExactlyInAnyOrder optimizelyExperiment.variations.map { it.key }
                    }
                    val experimentClass = compilationResult.classLoader.loadClass("$packageName.TestExperiment")
                    experimentClass.getField("INSTANCE").get(null).shouldBeInstanceOf<BaseExperiment<BaseVariation>>()
                    @Suppress("UNCHECKED_CAST")
                    val experimentObject =
                        experimentClass.getField("INSTANCE").get(null) as BaseExperiment<BaseVariation>
                    experimentObject.key shouldBe experimentKey
                    experimentObject.variations shouldBe variationsClass.enumConstants
                    val experimentsClientClass =
                        compilationResult.classLoader.loadClass("$packageName.ExperimentsClient")
                    experimentsClientClass.constructors shouldHaveSize 1
                    with(experimentsClientClass.constructors.first().parameters) {
                        this shouldHaveSize 2
                        this[0].type shouldBe Optimizely::class.java
                        this[1].type shouldBe String::class.java
                    }
                    experimentsClientClass.methods.find { it.name == "getAllExperiments" } shouldNotBe null
                    experimentsClientClass.methods.find { it.name == "getVariationForExperiment" } shouldNotBe null
                }
            }
        }
    }
})

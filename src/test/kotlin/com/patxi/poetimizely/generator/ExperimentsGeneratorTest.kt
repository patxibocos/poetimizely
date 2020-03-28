package com.patxi.poetimizely.generator

import com.optimizely.ab.Optimizely
import com.patxi.poetimizely.generator.base.BaseExperiment
import com.patxi.poetimizely.generator.base.BaseExperimentsClient
import com.patxi.poetimizely.generator.base.BaseVariant
import com.patxi.poetimizely.optimizely.OptimizelyExperiment
import com.patxi.poetimizely.optimizely.OptimizelyVariation
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

class ExperimentsGeneratorTest : BehaviorSpec({

    given("An Optimizely experiment") {
        val experimentKey = "TEST_EXPERIMENT"
        val variationKey = "TEST_VARIATION"
        val optimizelyExperiment =
            OptimizelyExperiment(experimentKey, variations = listOf(OptimizelyVariation(variationKey)))
        and("A Generator for a package") {
            val packageName = "what.ever.pack.age"
            val experimentsGenerator = ExperimentsGenerator(packageName)
            `when`("Compiling the generated code for experiment and its variants") {
                val experimentsCode = experimentsGenerator.build(listOf(optimizelyExperiment))
                val compilationResult = KotlinCompilation().apply {
                    sources = listOf(SourceFile.kotlin("Experiments.kt", experimentsCode))
                    inheritClassPath = true
                    messageOutputStream = System.out
                }.compile()
                then("Generated code compiles") {
                    compilationResult.exitCode shouldBe KotlinCompilation.ExitCode.OK
                    val variantsClass = compilationResult.classLoader.loadClass("$packageName.${experimentKey}Variants")
                    with(variantsClass.enumConstants) {
                        this shouldHaveSize optimizelyExperiment.variations.size
                        this.shouldBeInstanceOf<Array<BaseVariant>>()
                        this.map { (it as BaseVariant).key } shouldContainExactlyInAnyOrder optimizelyExperiment.variations.map { it.key }
                    }
                    val experimentClass = compilationResult.classLoader.loadClass("$packageName.$experimentKey")
                    experimentClass.getField("INSTANCE").get(null).shouldBeInstanceOf<BaseExperiment<BaseVariant>>()
                    @Suppress("UNCHECKED_CAST")
                    val experimentObject =
                        experimentClass.getField("INSTANCE").get(null) as BaseExperiment<BaseVariant>
                    experimentObject.key shouldBe experimentKey
                    experimentObject.variants shouldBe variantsClass.enumConstants
                    val experimentsClientClass =
                        compilationResult.classLoader.loadClass("$packageName.ExperimentsClient")
                    experimentsClientClass.constructors.first().newInstance(mockk<Optimizely>(), "")
                        .shouldBeInstanceOf<BaseExperimentsClient> { experimentsClient ->
                            val allExperiments = experimentsClient.getAllExperiments()
                            allExperiments.shouldBeSingleton()
                            allExperiments.map { it.javaClass }.shouldContainExactly(experimentObject::class.java)
                        }
                }
            }
        }
    }
})

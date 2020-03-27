package com.patxi.poetimizely.generator

import com.optimizely.ab.Optimizely
import com.patxi.poetimizely.optimizely.Feature
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe

class FeaturesGeneratorTest : BehaviorSpec({

    given("A list of Optimizely features") {
        val features = listOf(
            Feature("new_checkout_page"),
            Feature("new_login_page"),
            Feature("new_sign_up_page")
        )
        and("A FeaturesGenerator for a package") {
            val packageName = "what.ever.pack.age"
            val generator = FeaturesGenerator(packageName)
            `when`("Compiling the generated code for features") {
                val featuresCode = generator.build(features)
                val compilationResult = KotlinCompilation().apply {
                    sources = listOf(SourceFile.kotlin("Features.kt", featuresCode))
                    inheritClassPath = true
                    messageOutputStream = System.out
                }.compile()
                then("Generated code compiles") {
                    compilationResult.exitCode shouldBe KotlinCompilation.ExitCode.OK
                    val featuresEnum = compilationResult.classLoader.loadClass("$packageName.Features")
                    featuresEnum.isEnum shouldBe true
                    with(featuresEnum.enumConstants) {
                        this shouldHaveSize features.size
                        (this[0] as Enum<*>).name shouldBe "NEW_CHECKOUT_PAGE"
                        (this[1] as Enum<*>).name shouldBe "NEW_LOGIN_PAGE"
                        (this[2] as Enum<*>).name shouldBe "NEW_SIGN_UP_PAGE"
                    }
                    val featuresClientClass =
                        compilationResult.classLoader.loadClass("$packageName.FeaturesClient")
                    with(featuresClientClass.constructors.first().parameters) {
                        this shouldHaveSize 2
                        this[0].type shouldBe Optimizely::class.java
                        this[1].type shouldBe String::class.java
                    }
                    with(featuresClientClass.methods.first()) {
                        this.name shouldBe "isFeatureEnabled"
                        this.parameters.first().type.toString() shouldBe "class what.ever.pack.age.Features"
                    }
                }
            }
        }
    }
})

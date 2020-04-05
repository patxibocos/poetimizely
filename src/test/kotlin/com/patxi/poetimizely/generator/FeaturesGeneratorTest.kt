package com.patxi.poetimizely.generator

import com.optimizely.ab.Optimizely
import com.patxi.poetimizely.matchers.publicStaticMethod
import com.patxi.poetimizely.optimizely.Feature
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldHave

class FeaturesGeneratorTest : BehaviorSpec({

    given("A list of Optimizely features") {
        val features = listOf(
            Feature("new_checkout_page"),
            Feature("new_login_page"),
            Feature("new_sign_up_page")
        )
        and("A FeaturesGenerator for a package") {
            val packageName = "what.ever.pack.age"
            val featuresGenerator = FeaturesGenerator(packageName)
            `when`("Compiling the generated code for features") {
                val featuresCode = featuresGenerator.generate(features)
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
                        this.map { (it as Enum<*>).name } shouldBe listOf(
                            "NEW_CHECKOUT_PAGE",
                            "NEW_LOGIN_PAGE",
                            "NEW_SIGN_UP_PAGE"
                        )
                    }
                    val extensionFunctionContainerClass =
                        compilationResult.classLoader.loadClass("$packageName.FeaturesKt")
                    extensionFunctionContainerClass shouldHave publicStaticMethod(
                        "isFeatureEnabled",
                        Optimizely::class.java,
                        featuresEnum,
                        String::class.java,
                        Map::class.java
                    )
                }
            }
        }
    }
})

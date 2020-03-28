package com.patxi.poetimizely.generator.base

import com.optimizely.ab.Optimizely
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk

class ExperimentsClientTest : BehaviorSpec({
    given("An experiment with two variants") {
        val experiment = object : BaseExperiment<ExperimentVariants> {
            override val key = "Experiment"
            override val variants = ExperimentVariants.values()
        }
        and("A experiments client with a mock Optimizely instance") {
            val userId = "userId"
            val optimizelyMock = mockk<Optimizely> {
                every { activate(experiment.key, userId, emptyMap<String, Any>()) } returns mockk {
                    every { key } returns ExperimentVariants.Variant1.key
                }
            }
            val experimentsClient: BaseExperimentsClient = object : BaseExperimentsClient(optimizelyMock, userId) {
                override fun getAllExperiments() = listOf(experiment)
            }
            `when`("Getting the variant for the experiment") {
                val variant = experimentsClient.getVariantForExperiment(experiment)
                then("The returned variant by the client is the expected one") {
                    variant shouldBe ExperimentVariants.Variant1
                }
            }
        }
    }
})

private enum class ExperimentVariants : BaseVariant {
    Variant1 {
        override val key = "Variant1"
    },
    Variant2 {
        override val key = "Variant2"
    },
}

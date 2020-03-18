package com.patxi.poetimizely.generator

import com.patxi.poetimizely.optimizely.Experiment
import com.patxi.poetimizely.optimizely.Variation
import io.kotest.core.spec.style.StringSpec

class GeneratorTest : StringSpec({
    "Experiment code is properly generated" {
        val experiment = Experiment("EXPERIMENT_KEY", variations = listOf(Variation("VARIATION_KEY")))

        val experimentCode = buildExperimentObject(experiment)
        println(experimentCode)
    }
})
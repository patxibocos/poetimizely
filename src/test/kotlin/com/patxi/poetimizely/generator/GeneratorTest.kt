package com.patxi.poetimizely.generator

import com.patxi.poetimizely.optimizely.Experiment
import io.kotest.core.spec.style.StringSpec
import java.io.File

class GeneratorTest : StringSpec({
    "Experiment code is properly generated" {
        val experimentKey = System.getProperty("testExperimentKey")
        val experiment = Experiment(experimentKey, variations = emptyList())

        val experimentCode = buildExperimentObject(experiment)
        File("src/test/kotlin/$experimentKey.kt").writeText(experimentCode)
    }
})
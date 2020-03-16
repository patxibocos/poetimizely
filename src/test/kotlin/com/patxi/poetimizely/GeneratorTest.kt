package com.patxi.poetimizely

import com.patxi.poetimizely.optimizely.Experiment
import com.patxi.poetimizely.optimizely.Variation
import io.kotest.core.spec.style.StringSpec
import java.net.URLClassLoader

class GeneratorTest : StringSpec({
    "whatever" {
        val experiment = Experiment("EXPERIMENT_KEY", variations = listOf(Variation("VARIATION_KEY")))

        val experimentClassFile = buildExperimentObject(experiment)
        val urls = arrayOf(experimentClassFile.toURI().toURL())
        val classLoader: ClassLoader = URLClassLoader(urls)

        classLoader.loadClass(experiment.key)
        classLoader.loadClass("${experiment.key}Variants")
    }
})
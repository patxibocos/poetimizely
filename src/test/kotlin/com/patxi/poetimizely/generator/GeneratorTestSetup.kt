package com.patxi.poetimizely.generator

import com.patxi.poetimizely.optimizely.Experiment
import com.patxi.poetimizely.optimizely.Variation
import io.kotest.core.spec.style.StringSpec
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import java.io.File

class GeneratorTestSetup : StringSpec({
    "Build a sample experiment for tests" {
        val json = Json(JsonConfiguration.Stable)
        val mapSerializer = MapSerializer(String.serializer(), String.serializer())
        val data = json.parse(mapSerializer, System.getProperty("experimentGeneratedCodeData"))
        val experiment =
            Experiment(data.getValue("experimentKey"), variations = listOf(Variation(data.getValue("variationKey"))))

        val experimentCode = buildExperimentObject(experiment)
        File(data.getValue("path")).writeText(experimentCode)
    }
})
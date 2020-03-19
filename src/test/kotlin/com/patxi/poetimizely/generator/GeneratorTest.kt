package com.patxi.poetimizely.generator

import com.patxi.poetimizely.generator.base.Variant
import io.kotest.core.spec.style.StringSpec
import io.kotest.inspectors.forAll
import io.kotest.matchers.shouldBe
import java.io.File
import java.net.URLClassLoader
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration

class GeneratorTest : StringSpec({
    "Experiment code is properly generated" {
        val json = Json(JsonConfiguration.Stable)
        val mapSerializer = MapSerializer(String.serializer(), String.serializer())
        val data = json.parse(mapSerializer, System.getProperty("experimentGeneratedCodeData"))
        val fileUrl = File(data.getValue("path")).toURI().toURL()
        val classLoader: ClassLoader = URLClassLoader(arrayOf(fileUrl))
        val experimentKey = data.getValue("experimentKey")
        val variationKey = data.getValue("variationKey")

        val experimentClass = classLoader.loadClass(experimentKey)
        val variantsClass = classLoader.loadClass("${experimentKey}Variants")

        val variantsEnumConstants = variantsClass.enumConstants
        variantsEnumConstants.size shouldBe 1
        variantsEnumConstants.forAll {
            (it is Variant) shouldBe true
            (it as Variant).key shouldBe variationKey
        }

        val instanceField = experimentClass.getField("INSTANCE")
        (instanceField.get(null) is com.patxi.poetimizely.generator.base.Experiment<*>) shouldBe true
        val experiment = (instanceField.get(null) as com.patxi.poetimizely.generator.base.Experiment<*>)
        experiment.key shouldBe experimentKey
        experiment.variants shouldBe variantsEnumConstants
    }
})

package com.patxi.poetimizely.generator

import com.optimizely.ab.Optimizely
import com.patxi.poetimizely.optimizely.OptimizelyExperiment
import com.patxi.poetimizely.optimizely.OptimizelyVariation
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.TypeVariableName
import com.squareup.kotlinpoet.WildcardTypeName
import java.io.StringWriter

class ExperimentsGenerator(private val packageName: String) {

    private val baseVariationClassName = ClassName(packageName, "BaseVariation")
    private val baseExperimentClassName = ClassName(packageName, "BaseExperiment")

    fun generate(optimizelyExperiments: List<OptimizelyExperiment>): String =
        FileSpec.builder(packageName, "Experiments")
            .apply {
                addType(baseVariationTypeSpec())
                addType(baseExperimentTypeSpec())
                optimizelyExperiments.forEach { experiment ->
                    val variationsEnumClassName =
                        ClassName(packageName, experiment.key.optimizelyExperimentKeyToVariationEnumName())
                    addType(experimentVariationsEnumTypeSpec(variationsEnumClassName, experiment.variations))
                    addType(experimentObjectTypeSpec(variationsEnumClassName, experiment))
                }
                addType(buildExperimentsClient(optimizelyExperiments))
            }.build().run {
                StringWriter().also { appendable: Appendable ->
                    this.writeTo(appendable)
                }.toString()
            }

    private fun experimentVariationsEnumTypeSpec(
        variationsEnumClassName: ClassName,
        optimizelyVariations: Collection<OptimizelyVariation>
    ): TypeSpec =
        TypeSpec.enumBuilder(variationsEnumClassName)
            .addSuperinterface(baseVariationClassName).apply {
                optimizelyVariations.forEach { variation ->
                    addEnumConstant(
                        variation.key.optimizelyExperimentKeyToVariationEnumConstant(),
                        TypeSpec.anonymousClassBuilder().addProperty(
                            PropertySpec.builder("key", String::class, KModifier.OVERRIDE)
                                .initializer("%S", variation.key)
                                .build()
                        ).build()
                    ).build()
                }
            }.build()

    private fun baseVariationTypeSpec(): TypeSpec =
        TypeSpec.interfaceBuilder(baseVariationClassName).addProperty("key", String::class).build()

    private fun baseExperimentTypeSpec(): TypeSpec =
        TypeVariableName("V", baseVariationClassName)
            .let { variationTypeName ->
                TypeSpec.interfaceBuilder(baseExperimentClassName).addTypeVariable(variationTypeName)
                    .addProperty("key", String::class)
                    .addProperty("variations", ClassName("kotlin", "Array").parameterizedBy(variationTypeName))
                    .build()
            }

    private fun experimentObjectTypeSpec(
        variationsEnumClassName: ClassName,
        optimizelyExperiment: OptimizelyExperiment
    ): TypeSpec =
        TypeSpec.objectBuilder(ClassName(packageName, optimizelyExperiment.key.optimizelyExperimentKeyToObjectName()))
            .apply {
                val experimentObjectClassName = baseExperimentClassName.parameterizedBy(variationsEnumClassName)
                addSuperinterface(experimentObjectClassName).addProperty(
                    PropertySpec.builder("key", String::class, KModifier.OVERRIDE)
                        .initializer("%S", optimizelyExperiment.key)
                        .build()
                )
                val arrayClassName = ClassName("kotlin", "Array")
                val listOfVariations = arrayClassName.parameterizedBy(variationsEnumClassName)
                addProperty(
                    PropertySpec.builder("variations", listOfVariations, KModifier.OVERRIDE)
                        .initializer(CodeBlock.of("$variationsEnumClassName.values()")).build()
                )
            }.build()

    private fun buildExperimentsClient(optimizelyExperiments: List<OptimizelyExperiment>): TypeSpec =
        TypeSpec.classBuilder(ClassName(packageName, "ExperimentsClient"))
            .primaryConstructor(
                FunSpec.constructorBuilder()
                    .addParameter("optimizely", Optimizely::class)
                    .addParameter("userId", String::class)
                    .build()
            )
            .addProperty(
                PropertySpec.builder("optimizely", Optimizely::class, KModifier.PRIVATE)
                    .initializer("optimizely")
                    .build()
            )
            .addProperty(
                PropertySpec.builder("userId", String::class, KModifier.PRIVATE)
                    .initializer("userId")
                    .build()
            )
            // This function builds the following:
            // fun getAllExperiments(): List<BaseExperiment<out BaseVariation>> = listOf(...)
            .addFunction(
                FunSpec.builder("getAllExperiments")
                    .returns(
                        ClassName("kotlin.collections", "List").parameterizedBy(
                            baseExperimentClassName.parameterizedBy(WildcardTypeName.producerOf(baseVariationClassName))
                        )
                    )
                    .addStatement("""return listOf(${optimizelyExperiments.joinToString {
                        it.key.optimizelyExperimentKeyToObjectName()
                    }})""".trimIndent())
                    .build()
            )
            .addFunction(
                TypeVariableName("V", baseVariationClassName).let { variationTypeName ->
                    FunSpec.builder("getVariationForExperiment")
                        .addTypeVariable(variationTypeName)
                        .returns(variationTypeName.copy(nullable = true))
                        .addParameter(
                            "experiment",
                            baseExperimentClassName.parameterizedBy(WildcardTypeName.producerOf(variationTypeName))
                        )
                        .addParameter(
                            ParameterSpec.Companion.builder(
                                "attributes",
                                Map::class.parameterizedBy(String::class, Any::class)
                            ).defaultValue(CodeBlock.of("emptyMap()")).build()
                        )
                        .addStatement("val variation = optimizely.activate(experiment.key, userId, attributes)")
                        .addStatement("return experiment.variations.find { it.key == variation?.key }")
                        .build()
                }
            )
            .build()
}

private fun String.optimizelyExperimentKeyToObjectName(): String =
    split("-", "_").joinToString("") { it.toLowerCase().capitalize() }

private fun String.optimizelyExperimentKeyToVariationEnumName(): String =
    split("-", "_").joinToString("") { it.toLowerCase().capitalize() } + "Variations"

private fun String.optimizelyExperimentKeyToVariationEnumConstant(): String =
    split("-", "_").joinToString("_") { it.toUpperCase() }

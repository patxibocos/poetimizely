package com.patxi.poetimizely.generator

import com.optimizely.ab.Optimizely
import com.patxi.poetimizely.generator.optimizely.OptimizelyExperiment
import com.patxi.poetimizely.generator.optimizely.OptimizelyVariation
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

internal fun generateExperimentsCode(optimizelyExperiments: List<OptimizelyExperiment>, packageName: String): String {
    val baseVariationClassName = ClassName(packageName, "BaseVariation")
    val baseExperimentClassName = ClassName(packageName, "BaseExperiment")

    return FileSpec.builder(packageName, "Experiments")
        .apply {
            addType(baseVariationTypeSpec(baseVariationClassName))
            addType(baseExperimentTypeSpec(baseVariationClassName, baseExperimentClassName))
            addFunction(
                getAllExperimentsFunSpec(
                    optimizelyExperiments,
                    baseExperimentClassName,
                    baseVariationClassName
                )
            )
            addFunction(getVariationForExperimentFunSpec(baseVariationClassName, baseExperimentClassName))
            val experimentsObjectBuilder = TypeSpec.objectBuilder("Experiments")
            optimizelyExperiments.forEach { experiment ->
                val variationsEnumClassName =
                    ClassName(packageName, experiment.key.optimizelyExperimentKeyToVariationEnumName())
                addType(
                    experimentVariationsEnumTypeSpec(
                        variationsEnumClassName,
                        baseVariationClassName,
                        experiment.variations
                    )
                )
                experimentsObjectBuilder.addType(
                    experimentObjectTypeSpec(
                        packageName,
                        baseExperimentClassName,
                        variationsEnumClassName,
                        experiment
                    )
                )
            }
            addType(experimentsObjectBuilder.build())
        }.build().run {
            StringWriter().also { appendable: Appendable ->
                this.writeTo(appendable)
            }.toString()
        }
}

private fun experimentVariationsEnumTypeSpec(
    variationsEnumClassName: ClassName,
    baseVariationClassName: ClassName,
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

private fun baseVariationTypeSpec(baseVariationClassName: ClassName): TypeSpec =
    TypeSpec.interfaceBuilder(baseVariationClassName).addProperty("key", String::class).build()

private fun baseExperimentTypeSpec(baseVariationClassName: ClassName, baseExperimentClassName: ClassName): TypeSpec =
    TypeVariableName("V", baseVariationClassName)
        .let { variationTypeName ->
            TypeSpec.interfaceBuilder(baseExperimentClassName).addTypeVariable(variationTypeName)
                .addProperty("key", String::class)
                .addProperty("variations", ClassName("kotlin", "Array").parameterizedBy(variationTypeName))
                .build()
        }

private fun experimentObjectTypeSpec(
    packageName: String,
    baseExperimentClassName: ClassName,
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

// This function builds the following:
// fun getAllExperiments(): List<BaseExperiment<out BaseVariation>> = listOf(...)
private fun getAllExperimentsFunSpec(
    optimizelyExperiments: List<OptimizelyExperiment>,
    baseExperimentClassName: ClassName,
    baseVariationClassName: ClassName
): FunSpec =
    FunSpec.builder("getAllExperiments")
        .receiver(Optimizely::class)
        .returns(
            ClassName("kotlin.collections", "List").parameterizedBy(
                baseExperimentClassName.parameterizedBy(WildcardTypeName.producerOf(baseVariationClassName))
            )
        )
        .addStatement("""return listOf(${optimizelyExperiments.joinToString {
            "Experiments.${it.key.optimizelyExperimentKeyToObjectName()}"
        }})""".trimIndent())
        .build()

private fun getVariationForExperimentFunSpec(
    baseVariationClassName: ClassName,
    baseExperimentClassName: ClassName
): FunSpec =
    TypeVariableName("V", baseVariationClassName).let { variationTypeName ->
        FunSpec.builder("getVariationForExperiment")
            .receiver(Optimizely::class)
            .addTypeVariable(variationTypeName)
            .addParameter(
                "experiment",
                baseExperimentClassName.parameterizedBy(WildcardTypeName.producerOf(variationTypeName))
            )
            .addParameter("userId", String::class)
            .addParameter(
                ParameterSpec.Companion.builder(
                    "attributes",
                    Map::class.parameterizedBy(String::class, Any::class)
                ).defaultValue(CodeBlock.of("emptyMap()")).build()
            )
            .returns(variationTypeName.copy(nullable = true))
            .addStatement("val variation = this.activate(experiment.key, userId, attributes)")
            .addStatement("return experiment.variations.find { it.key == variation?.key }")
            .build()
    }

private fun String.optimizelyExperimentKeyToObjectName(): String =
    split("-", "_").joinToString("") { it.toLowerCase().capitalize() }

private fun String.optimizelyExperimentKeyToVariationEnumName(): String =
    split("-", "_").joinToString("") { it.toLowerCase().capitalize() } + "Variations"

private fun String.optimizelyExperimentKeyToVariationEnumConstant(): String =
    split("-", "_").joinToString("_") { it.toUpperCase() }

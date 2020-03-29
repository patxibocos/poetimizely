package com.patxi.poetimizely.generator

import com.optimizely.ab.Optimizely
import com.patxi.poetimizely.generator.base.BaseExperiment
import com.patxi.poetimizely.generator.base.BaseVariant
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
import kotlin.reflect.KClass

class ExperimentsGenerator(private val packageName: String = "") {

    fun build(optimizelyExperiments: List<OptimizelyExperiment>): String =
        FileSpec.builder(packageName, "Experiments")
            .apply {
                optimizelyExperiments.forEach { experiment ->
                    val variantsEnumClassName = ClassName(packageName, "${experiment.key}Variants")
                    addType(experimentVariantsEnumTypeSpec(variantsEnumClassName, experiment.variations))
                    addType(experimentObjectTypeSpec(variantsEnumClassName, experiment))
                }
                addType(buildExperimentsClient(optimizelyExperiments))
            }.build().run {
                StringWriter().also { appendable: Appendable ->
                    this.writeTo(appendable)
                }.toString()
            }

    private fun experimentVariantsEnumTypeSpec(
        variantsEnumClassName: ClassName,
        optimizelyVariations: Collection<OptimizelyVariation>
    ): TypeSpec =
        TypeSpec.enumBuilder(variantsEnumClassName)
            .addSuperinterface(BaseVariant::class).apply {
                optimizelyVariations.forEach { variation ->
                    addEnumConstant(
                        variation.key,
                        TypeSpec.anonymousClassBuilder().addProperty(
                            PropertySpec.builder("key", String::class, KModifier.OVERRIDE)
                                .initializer("%S", variation.key)
                                .build()
                        ).build()
                    ).build()
                }
            }.build()

    private fun experimentObjectTypeSpec(
        variantsEnumClassName: ClassName,
        optimizelyExperiment: OptimizelyExperiment
    ): TypeSpec =
        TypeSpec.objectBuilder(ClassName(packageName, optimizelyExperiment.key)).apply {
            val experimentObjectClassName =
                BaseExperiment::class.className().parameterizedBy(
                    variantsEnumClassName
                )
            addSuperinterface(experimentObjectClassName).addProperty(
                PropertySpec.builder("key", String::class, KModifier.OVERRIDE)
                    .initializer("%S", optimizelyExperiment.key)
                    .build()
            )
            val arrayClassName = ClassName("kotlin", "Array")
            val listOfVariants = arrayClassName.parameterizedBy(variantsEnumClassName)
            addProperty(
                PropertySpec.builder("variants", listOfVariants, KModifier.OVERRIDE)
                    .initializer(CodeBlock.of("$variantsEnumClassName.values()")).build()
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
            // fun getAllExperiments(): List<BaseExperiment<out BaseVariant>> = listOf(...)
            .addFunction(
                FunSpec.builder("getAllExperiments")
                    .returns(
                        ClassName("kotlin.collections", "List").parameterizedBy(
                            BaseExperiment::class.className().parameterizedBy(
                                WildcardTypeName.producerOf(BaseVariant::class)
                            )
                        )
                    )
                    .addStatement("return listOf(${optimizelyExperiments.joinToString { it.key }})")
                    .build()
            )
            .addFunction(
                TypeVariableName("V", BaseVariant::class).let { variantTypeName ->
                    FunSpec.builder("getVariantForExperiment")
                        .addTypeVariable(variantTypeName)
                        .returns(variantTypeName.copy(nullable = true))
                        .addParameter(
                            "experiment",
                            BaseExperiment::class.className()
                                .parameterizedBy(WildcardTypeName.producerOf(variantTypeName))
                        )
                        .addParameter(
                            ParameterSpec.Companion.builder(
                                "attributes",
                                Map::class.parameterizedBy(String::class, Any::class)
                            ).defaultValue(CodeBlock.of("emptyMap()")).build()
                        )
                        .addStatement("val variation = optimizely.activate(experiment.key, userId, attributes)")
                        .addStatement("return experiment.variants.find { it.key == variation?.key }")
                        .build()
                }
            )
            .build()
}

private fun KClass<*>.className(): ClassName = ClassName(this.java.`package`.name, this.java.simpleName)

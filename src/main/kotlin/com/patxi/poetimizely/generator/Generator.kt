package com.patxi.poetimizely.generator

import com.patxi.poetimizely.generator.base.ExperimentsClient
import com.patxi.poetimizely.generator.base.Variant
import com.patxi.poetimizely.optimizely.Experiment
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import java.io.StringWriter
import kotlin.reflect.KClass

fun buildExperimentObject(experiment: Experiment): String {
    val variantsEnumClassName = ClassName("", "${experiment.key}Variants")
    val variantsEnum = TypeSpec.enumBuilder(variantsEnumClassName)
        .addSuperinterface(Variant::class).apply {
            experiment.variations.forEach { variation ->
                addEnumConstant(
                    variation.key,
                    TypeSpec.anonymousClassBuilder().addProperty(
                        PropertySpec.builder("key", String::class, KModifier.OVERRIDE).initializer("%S", variation.key)
                            .build()
                    ).build()
                ).build()
            }
        }.build()

    val experimentClazz = com.patxi.poetimizely.generator.base.Experiment::class.java
    val experimentObjectClassName =
        ClassName(experimentClazz.`package`.name, experimentClazz.simpleName).parameterizedBy(variantsEnumClassName)
    val arrayClassName = ClassName("kotlin", "Array")
    val listOfVariants = arrayClassName.parameterizedBy(variantsEnumClassName)

    val experimentObject = TypeSpec.objectBuilder(experiment.key)
        .addSuperinterface(experimentObjectClassName).addProperty(
            PropertySpec.builder("key", String::class, KModifier.OVERRIDE).initializer("%S", experiment.key).build()
        ).addProperty(
            PropertySpec.builder("variants", listOfVariants, KModifier.OVERRIDE)
                .initializer(CodeBlock.of("$variantsEnumClassName.values()")).build()
        ).build()

    val appendable: Appendable = StringWriter()
    FileSpec.builder("", experiment.key).addType(variantsEnum).addType(experimentObject).build().writeTo(appendable)
    return appendable.toString()
}

fun buildOptimizelyClient(experimentClasses: List<KClass<out com.patxi.poetimizely.generator.base.Experiment<Variant>>>): String {
    val parameterizedExperiment = com.patxi.poetimizely.generator.base.Experiment::class.parameterizedBy(Variant::class)
    ClassName("kotlin.collections", "List").parameterizedBy(parameterizedExperiment)
    val experimentClientTypeSpec =
        TypeSpec.classBuilder("TestExperimentsClient")//.addSuperinterface(ExperimentsClient::class)
            .addFunction(
                FunSpec.builder("getAllExperiments")/*.addModifiers(KModifier.OVERRIDE)*/
                    .addStatement("return listOf(${experimentClasses.map { it.qualifiedName }.joinToString()})")
                    .build()
            ).build()
    val appendable: Appendable = StringWriter()
    FileSpec.builder("", "TestExperimentsClient").addType(experimentClientTypeSpec).build().writeTo(appendable)
    return appendable.toString()
}
package com.patxi.poetimizely.generator

import com.patxi.poetimizely.generator.base.Variant
import com.patxi.poetimizely.optimizely.Experiment
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import java.io.StringWriter

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

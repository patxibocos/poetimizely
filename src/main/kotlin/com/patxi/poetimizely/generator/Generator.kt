package com.patxi.poetimizely.generator

import com.optimizely.ab.Optimizely
import com.patxi.poetimizely.generator.base.ExperimentsClient
import com.patxi.poetimizely.generator.base.Variant
import com.patxi.poetimizely.optimizely.Experiment
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import java.io.StringWriter
import com.patxi.poetimizely.generator.base.Experiment as GeneratorExperiment

class Generator(private val packageName: String = "") {

    fun buildExperimentObject(experiment: Experiment): String {
        val variantsEnumClassName = ClassName(packageName, "${experiment.key}Variants")
        val variantsEnum = TypeSpec.enumBuilder(variantsEnumClassName)
            .addSuperinterface(Variant::class).apply {
                experiment.variations.forEach { variation ->
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

        val experimentClazz = GeneratorExperiment::class.java
        val experimentObjectClassName =
            ClassName(experimentClazz.`package`.name, experimentClazz.simpleName).parameterizedBy(variantsEnumClassName)
        val arrayClassName = ClassName("kotlin", "Array")
        val listOfVariants = arrayClassName.parameterizedBy(variantsEnumClassName)

        val experimentObject = TypeSpec.objectBuilder(ClassName(packageName, experiment.key))
            .addSuperinterface(experimentObjectClassName).addProperty(
                PropertySpec.builder("key", String::class, KModifier.OVERRIDE).initializer("%S", experiment.key).build()
            ).addProperty(
                PropertySpec.builder("variants", listOfVariants, KModifier.OVERRIDE)
                    .initializer(CodeBlock.of("$variantsEnumClassName.values()")).build()
            ).build()
        return StringWriter().also { appendable: Appendable ->
            FileSpec.builder(packageName, experiment.key).addType(variantsEnum).addType(experimentObject).build()
                .writeTo(appendable)
        }.toString()
    }

    fun buildExperimentsClient(experimentObjectNames: List<String>): String {
        val experimentClientTypeSpec =
            TypeSpec.classBuilder(ClassName(packageName, "TestExperimentsClient"))
                .primaryConstructor(
                    FunSpec.constructorBuilder().addParameter("optimizely", Optimizely::class)
                        .addParameter("userId", String::class).build()
                )
                .superclass(ExperimentsClient::class)
                .addSuperclassConstructorParameter("optimizely")
                .addSuperclassConstructorParameter("userId")
                .addFunction(
                    FunSpec.builder("getAllExperiments").addModifiers(KModifier.OVERRIDE)
                        .addStatement("return listOf(${experimentObjectNames.joinToString()})").build()
                ).build()
        return StringWriter().also { appendable: Appendable ->
            FileSpec.builder(packageName, "TestExperimentsClient").addType(experimentClientTypeSpec).build()
                .writeTo(appendable)
        }.toString()
    }
}

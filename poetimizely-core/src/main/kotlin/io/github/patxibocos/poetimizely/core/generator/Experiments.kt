package io.github.patxibocos.poetimizely.core.generator

import com.optimizely.ab.Optimizely
import com.squareup.kotlinpoet.AnnotationSpec
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
import io.github.patxibocos.poetimizely.core.optimizely.Experiment
import io.github.patxibocos.poetimizely.core.optimizely.Variation
import java.io.StringWriter

internal fun generateExperimentsCode(
    optimizelyExperiments: List<Experiment>,
    packageName: String,
): String {
    val baseVariationClassName = ClassName(packageName, "BaseVariation")
    val experimentClassName = ClassName(packageName, "Experiments")

    return FileSpec
        .builder(packageName, "Experiments")
        .apply {
            addAnnotation(
                AnnotationSpec
                    .builder(Suppress::class)
                    .useSiteTarget(AnnotationSpec.UseSiteTarget.FILE)
                    .addMember("%S, %S", "RedundantVisibilityModifier", "Unused")
                    .build(),
            )
            val experimentSealedClassTypeSpecBuilder =
                experimentsSealedClassTypeSpec(baseVariationClassName, experimentClassName)
            addType(baseVariationTypeSpec(baseVariationClassName))
            addFunction(
                getAllExperimentsFunSpec(
                    optimizelyExperiments,
                    experimentClassName,
                    baseVariationClassName,
                ),
            )
            addFunction(getVariationForExperimentFunSpec(baseVariationClassName, experimentClassName))
            optimizelyExperiments.forEach { experiment ->
                val variationsEnumClassName =
                    ClassName(packageName, experiment.key.optimizelyExperimentKeyToVariationEnumName())
                addType(
                    experimentVariationsEnumTypeSpec(
                        variationsEnumClassName,
                        baseVariationClassName,
                        experiment.variations,
                    ),
                )
                experimentSealedClassTypeSpecBuilder.addType(
                    experimentObjectTypeSpec(
                        packageName,
                        experimentClassName,
                        variationsEnumClassName,
                        experiment,
                    ),
                )
            }
            addType(experimentSealedClassTypeSpecBuilder.build())
        }.build()
        .run {
            StringWriter()
                .also {
                    this.writeTo(it)
                }.toString()
                .let(::ktLint)
        }
}

private fun experimentVariationsEnumTypeSpec(
    variationsEnumClassName: ClassName,
    baseVariationClassName: ClassName,
    optimizelyVariations: Collection<Variation>,
): TypeSpec =
    TypeSpec
        .enumBuilder(variationsEnumClassName)
        .addSuperinterface(baseVariationClassName)
        .apply {
            optimizelyVariations.forEach { variation ->
                addEnumConstant(
                    variation.key.optimizelyExperimentKeyToVariationEnumConstant(),
                    TypeSpec
                        .anonymousClassBuilder()
                        .addProperty(
                            PropertySpec
                                .builder("key", String::class, KModifier.OVERRIDE)
                                .initializer("%S", variation.key)
                                .build(),
                        ).build(),
                ).build()
            }
        }.build()

private fun baseVariationTypeSpec(baseVariationClassName: ClassName): TypeSpec =
    TypeSpec.interfaceBuilder(baseVariationClassName).addProperty("key", String::class).build()

private fun experimentsSealedClassTypeSpec(
    baseVariationClassName: ClassName,
    experimentsClassName: ClassName,
): TypeSpec.Builder =
    TypeVariableName("V", baseVariationClassName)
        .let { variationTypeName ->
            TypeSpec
                .classBuilder(experimentsClassName)
                .addModifiers(KModifier.SEALED)
                .addTypeVariable(variationTypeName)
                .primaryConstructor(
                    FunSpec
                        .constructorBuilder()
                        .addParameter("key", String::class)
                        .addParameter(
                            "variations",
                            ClassName("kotlin.collections", "List").parameterizedBy(variationTypeName),
                        ).build(),
                ).addProperty(PropertySpec.builder("key", String::class).initializer("key").build())
                .addProperty(
                    PropertySpec
                        .builder(
                            "variations",
                            ClassName("kotlin.collections", "List").parameterizedBy(variationTypeName),
                        ).initializer("variations")
                        .build(),
                )
        }

private fun experimentObjectTypeSpec(
    packageName: String,
    experimentClassName: ClassName,
    variationsEnumClassName: ClassName,
    optimizelyExperiment: Experiment,
): TypeSpec =
    TypeSpec
        .objectBuilder(ClassName(packageName, optimizelyExperiment.key.optimizelyExperimentKeyToObjectName()))
        .apply {
            superclass(experimentClassName.parameterizedBy(variationsEnumClassName))
            addModifiers(KModifier.DATA)
            addSuperclassConstructorParameter("%S", optimizelyExperiment.key)
            addSuperclassConstructorParameter(CodeBlock.of("$variationsEnumClassName.entries"))
        }.build()

// This function builds the following:
// fun getAllExperiments(): List<BaseExperiment<out BaseVariation>> = listOf(...)
private fun getAllExperimentsFunSpec(
    optimizelyExperiments: List<Experiment>,
    baseExperimentClassName: ClassName,
    baseVariationClassName: ClassName,
): FunSpec =
    FunSpec
        .builder("getAllExperiments")
        .returns(
            ClassName("kotlin.collections", "List").parameterizedBy(
                baseExperimentClassName.parameterizedBy(WildcardTypeName.producerOf(baseVariationClassName)),
            ),
        ).addStatement(
            """return listOf(${
                optimizelyExperiments.joinToString {
                    "Experiments.${it.key.optimizelyExperimentKeyToObjectName()}"
                }
            })""".trimIndent(),
        ).build()

private fun getVariationForExperimentFunSpec(
    baseVariationClassName: ClassName,
    baseExperimentClassName: ClassName,
): FunSpec =
    TypeVariableName("V", baseVariationClassName).let { variationTypeName ->
        FunSpec
            .builder("getVariationForExperiment")
            .receiver(Optimizely::class)
            .addTypeVariable(variationTypeName)
            .addParameter(
                "experiment",
                baseExperimentClassName.parameterizedBy(variationTypeName),
            ).addParameter("userId", String::class)
            .addParameter(
                ParameterSpec.Companion
                    .builder(
                        "attributes",
                        Map::class.parameterizedBy(String::class, Any::class),
                    ).defaultValue(CodeBlock.of("emptyMap()"))
                    .build(),
            ).returns(variationTypeName.copy(nullable = true))
            .addStatement("val variation = this.activate(experiment.key, userId, attributes)")
            .addStatement("return experiment.variations.find { it.key == variation?.key }")
            .build()
    }

// example_experiment -> ExampleExperiment
private fun String.optimizelyExperimentKeyToObjectName(): String =
    split(
        "-",
        "_",
    ).joinToString("") {
        it.mapIndexed { index, c -> if (index == 0) c.uppercaseChar() else c.lowercaseChar() }.joinToString("")
    }

private fun String.optimizelyExperimentKeyToVariationEnumName(): String =
    split(
        "-",
        "_",
    ).joinToString("") {
        it.mapIndexed { index, c -> if (index == 0) c.uppercaseChar() else c.lowercaseChar() }.joinToString("")
    } + "Variations"

private fun String.optimizelyExperimentKeyToVariationEnumConstant(): String =
    split("-", "_").joinToString("_") { it.map { c -> c.uppercaseChar() }.joinToString("") }

package io.github.patxibocos.poetimizely.generator

import com.optimizely.ab.Optimizely
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
import com.squareup.kotlinpoet.asTypeName
import io.github.patxibocos.poetimizely.generator.optimizely.Feature
import io.github.patxibocos.poetimizely.generator.optimizely.Variable
import java.io.StringWriter

/**
 * The functionality to parse features to type safe code
 * is designed and documented in https://docs.google.com/document/d/1XJap6OKnzAM-C4GoL1Ubs2zKRzJSlIdzcvddOOE2Z7Q.
 */

internal fun generateFeaturesCode(features: List<Feature>, packageName: String): String {
    val featureClassName = ClassName(packageName, "Features")
    val featureVariableClassName = ClassName(packageName, "FeatureVariable")
    return FileSpec.builder(packageName, "Features")
        .addType(featureVariableTypeSpec(featureVariableClassName))
        .addType(featuresSealedClassTypeSpec(features, featureClassName, featureVariableClassName))
        .addFunction(isFeatureEnabledFunSpec(featureClassName))
        .addFunction(getFeatureVariableBooleanFunSpec(featureVariableClassName))
        .addFunction(getFeatureVariableStringFunSpec(featureVariableClassName))
        .addFunction(getFeatureVariableDoubleFunSpec(featureVariableClassName))
        .addFunction(getFeatureVariableIntFunSpec(featureVariableClassName))
        .build().run {
            StringWriter().also { appendable: Appendable ->
                this.writeTo(appendable)
            }.toString()
        }
}

private fun featureVariableTypeSpec(featureVariableClassName: ClassName): TypeSpec =
    TypeSpec.classBuilder(featureVariableClassName).addTypeVariable(TypeVariableName("T"))
        .primaryConstructor(
            FunSpec.constructorBuilder().addParameter("featureKey", String::class)
                .addParameter("variableKey", String::class).build()
        )
        .addProperty(PropertySpec.builder("featureKey", String::class).initializer("featureKey").build())
        .addProperty(PropertySpec.builder("variableKey", String::class).initializer("variableKey").build())
        .build()

/**
 * This will produce something like this.
 *
 * fun Optimizely.isFeatureEnabled(
 *    feature: Features,
 *    userId: String,
 *    attributes: Map<String, Any> = emptyMap()
 * ): Boolean = this.isFeatureEnabled(feature.key, userId, attributes)
 */
private fun isFeatureEnabledFunSpec(featureClassName: ClassName): FunSpec =
    FunSpec.builder("isFeatureEnabled")
        .receiver(Optimizely::class)
        .addParameter("feature", featureClassName)
        .addParameter("userId", String::class)
        .addParameter(
            ParameterSpec.Companion.builder(
                "attributes",
                Map::class.parameterizedBy(String::class, Any::class)
            ).defaultValue(CodeBlock.of("emptyMap()")).build()
        )
        .returns(Boolean::class)
        .addStatement("return this.isFeatureEnabled(feature.key, userId, attributes)")
        .build()

private fun getFeatureVariableBooleanFunSpec(featureVariableClassName: ClassName): FunSpec =
    FunSpec.builder("getFeatureVariable")
        .receiver(Optimizely::class)
        .addParameter("variable", featureVariableClassName.parameterizedBy(Boolean::class.asTypeName()))
        .addParameter("userId", String::class)
        .addParameter(
            ParameterSpec.Companion.builder(
                "attributes",
                Map::class.parameterizedBy(String::class, Any::class)
            ).defaultValue(CodeBlock.of("emptyMap()")).build()
        )
        .returns(Boolean::class.asTypeName().copy(nullable = true))
        .addStatement("return this.getFeatureVariableBoolean(variable.featureKey, variable.variableKey, userId, attributes)")
        .build()

private fun getFeatureVariableStringFunSpec(featureVariableClassName: ClassName): FunSpec =
    FunSpec.builder("getFeatureVariable")
        .receiver(Optimizely::class)
        .addParameter("variable", featureVariableClassName.parameterizedBy(String::class.asTypeName()))
        .addParameter("userId", String::class)
        .addParameter(
            ParameterSpec.Companion.builder(
                "attributes",
                Map::class.parameterizedBy(String::class, Any::class)
            ).defaultValue(CodeBlock.of("emptyMap()")).build()
        )
        .returns(String::class.asTypeName().copy(nullable = true))
        .addStatement("return this.getFeatureVariableString(variable.featureKey, variable.variableKey, userId, attributes)")
        .build()

private fun getFeatureVariableDoubleFunSpec(featureVariableClassName: ClassName): FunSpec =
    FunSpec.builder("getFeatureVariable")
        .receiver(Optimizely::class)
        .addParameter("variable", featureVariableClassName.parameterizedBy(Double::class.asTypeName()))
        .addParameter("userId", String::class)
        .addParameter(
            ParameterSpec.Companion.builder(
                "attributes",
                Map::class.parameterizedBy(String::class, Any::class)
            ).defaultValue(CodeBlock.of("emptyMap()")).build()
        )
        .returns(Double::class.asTypeName().copy(nullable = true))
        .addStatement("return this.getFeatureVariableDouble(variable.featureKey, variable.variableKey, userId, attributes)")
        .build()

private fun getFeatureVariableIntFunSpec(featureVariableClassName: ClassName): FunSpec =
    FunSpec.builder("getFeatureVariable")
        .receiver(Optimizely::class)
        .addParameter("variable", featureVariableClassName.parameterizedBy(Int::class.asTypeName()))
        .addParameter("userId", String::class)
        .addParameter(
            ParameterSpec.Companion.builder(
                "attributes",
                Map::class.parameterizedBy(String::class, Any::class)
            ).defaultValue(CodeBlock.of("emptyMap()")).build()
        )
        .returns(Int::class.asTypeName().copy(nullable = true))
        .addStatement("return this.getFeatureVariableInteger(variable.featureKey, variable.variableKey, userId, attributes)")
        .build()

/**
 * This will produce something that looks like this:
 * sealed class Features(val key: String) {
 *    object Feature1 : Features("new_checkout_page") {
 *    }
 *    ...
 * }
 */
private fun featuresSealedClassTypeSpec(
    features: List<Feature>,
    featureClassName: ClassName,
    featureVariableClassName: ClassName
): TypeSpec =
    TypeSpec.classBuilder(featureClassName).addModifiers(KModifier.SEALED)
        .primaryConstructor(FunSpec.constructorBuilder().addParameter("key", String::class).build())
        .addProperty(PropertySpec.builder("key", String::class).initializer("key").build())
        .also { typeSpecBuilder ->
            features.forEach { feature ->
                typeSpecBuilder.addType(
                    TypeSpec.objectBuilder(feature.key.optimizelyFeatureKeyToObjectName())
                        .superclass(featureClassName)
                        .addSuperclassConstructorParameter("%S", feature.key).apply {
                            feature.variables.forEach { featureVariable ->
                                addProperty(
                                    featureVariablePropertySpec(
                                        feature,
                                        featureVariable,
                                        featureVariableClassName
                                    )
                                )
                            }
                        }.build()
                )
            }
        }.build()

private fun featureVariablePropertySpec(
    feature: Feature,
    featureVariable: Variable,
    featureVariableClassName: ClassName
): PropertySpec =
    PropertySpec.builder(
        featureVariable.key.optimizelyFeatureVariableKeyToPropertyName(),
        featureVariableClassName.parameterizedBy(
            when (featureVariable.type) {
                "boolean" -> Boolean::class.asTypeName()
                "string" -> String()::class.asTypeName()
                "double" -> Double::class.asTypeName()
                "integer" -> Int::class.asTypeName()
                else -> throw IllegalArgumentException("Unexpected feature variable type ${featureVariable.type}")
            }
        )
    ).initializer(
        CodeBlock.of(
            "%T(%S, %S)",
            featureVariableClassName,
            feature.key,
            featureVariable.key
        )
    ).build()

/**
 * According to the Optimizely Docs (https://docs.developers.optimizely.com/full-stack/docs/create-feature-flags)
 * a feature key has the following restrictions:
 * - unique
 * - alphanumeric characters
 * - hyphens
 * - underscores
 * - max 64 chars
 * - NO spaces
 *
 * The key will be used "to determine whether the feature is on or off in your code".
 */
private fun String.optimizelyFeatureKeyToObjectName(): String = split("-", "_").joinToString("") { it.trim().replaceFirstChar(Char::uppercase) }

private fun String.optimizelyFeatureVariableKeyToPropertyName(): String = split("-", "_").joinToString("") { it.trim().replaceFirstChar(Char::uppercase) }.replaceFirstChar(Char::lowercase)

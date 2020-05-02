package com.patxi.poetimizely.generator

import com.optimizely.ab.Optimizely
import com.patxi.poetimizely.generator.optimizely.Feature
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import java.io.StringWriter

/**
 * The functionality to parse features to type safe code
 * is designed and documented in https://docs.google.com/document/d/1XJap6OKnzAM-C4GoL1Ubs2zKRzJSlIdzcvddOOE2Z7Q.
 */

internal fun generateFeaturesCode(features: List<Feature>, packageName: String): String {
    val featureClassName = ClassName(packageName, "Features")
    return FileSpec.builder(packageName, "Features")
        .addType(featuresEnumTypeSpec(features, featureClassName))
        .addFunction(isFeatureEnabledFunSpec(featureClassName))
        .build().run {
            StringWriter().also { appendable: Appendable ->
                this.writeTo(appendable)
            }.toString()
        }
}

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

/**
 * This will produce something that looks like this:
 * enum class Features(val key: String) {
 *    NEW_CHECKOUT_PAGE("new_checkout_page"),
 *    ...
 * }
 */
private fun featuresEnumTypeSpec(features: List<Feature>, featureClassName: ClassName): TypeSpec =
    // enum Features
    TypeSpec.enumBuilder(featureClassName)
        // with constructor(val key: String)
        .primaryConstructor(FunSpec.constructorBuilder().addParameter("key", String::class).build())
        .addProperty(PropertySpec.builder("key", String::class).initializer("key").build())
        .also { typeSpecBuilder ->
            // each feature is a const with the key
            features.forEach {
                typeSpecBuilder.addEnumConstant(
                    it.key.optimizelyFeatureKeyToEnumConstant(),
                    TypeSpec.anonymousClassBuilder().addSuperclassConstructorParameter("%S", it.key).build()
                )
            }
        }.build()

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
private fun String.optimizelyFeatureKeyToEnumConstant(): String =
    split("-", "_").joinToString("_") { it.trim().toUpperCase() }

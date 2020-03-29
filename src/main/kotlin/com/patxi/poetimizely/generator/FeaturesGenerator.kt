package com.patxi.poetimizely.generator

import com.optimizely.ab.Optimizely
import com.patxi.poetimizely.optimizely.Feature
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import java.io.StringWriter

/**
 * The functionality to parse features to type safe code
 * is designed and documented in https://docs.google.com/document/d/1XJap6OKnzAM-C4GoL1Ubs2zKRzJSlIdzcvddOOE2Z7Q.
 */
class FeaturesGenerator(private val packageName: String = "") {
    fun generate(features: List<Feature>): String =
        FileSpec.builder(packageName, "Features")
            .addType(featuresEnumTypeSpec(features))
            .addType(featuresClientTypeSpec(packageName))
            .build().run {
                StringWriter().also { appendable: Appendable ->
                    this.writeTo(appendable)
                }.toString()
            }
}

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

/**
 * This will produce something that looks like this:
 * enum class Features(val key: String) {
 *    NEW_CHECKOUT_PAGE("new_checkout_page"),
 *    ...
 * }
 */
private fun featuresEnumTypeSpec(features: List<Feature>): TypeSpec =
    // enum Features
    TypeSpec.enumBuilder("Features")
        // with constructor(val key: String)
        .primaryConstructor(
            FunSpec.constructorBuilder()
                .addParameter("key", String::class)
                .build()
        ).addProperty(
            PropertySpec.builder("key", String::class)
                .initializer("key")
                .build()
        ).also { typeSpecBuilder ->
            // each feature is a const with the key
            features.forEach {
                typeSpecBuilder.addEnumConstant(
                    it.key.optimizelyFeatureKeyToEnumConstant(),
                    TypeSpec.anonymousClassBuilder()
                        .addSuperclassConstructorParameter("%S", it.key)
                        .build()
                )
            }
        }.build()

/**
 * This will produce something like this.
 *
 * class FeaturesClient(
 *    private val optimizely: Optimizely,
 *    private val userId: String
 * ) {
 *    fun isFeatureEnabled(feature: Features): Boolean =
 *        optimizely.isFeatureEnabled(feature.key, userId)
 * }
 */
private fun featuresClientTypeSpec(packageName: String): TypeSpec =
    TypeSpec.classBuilder("FeaturesClient")
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
        .addFunction(
            FunSpec.builder("isFeatureEnabled")
                .addParameter("feature", ClassName(packageName, "Features"))
                .returns(Boolean::class)
                .addStatement("return optimizely.isFeatureEnabled(feature.key, userId)")
                .build()
        ).build()

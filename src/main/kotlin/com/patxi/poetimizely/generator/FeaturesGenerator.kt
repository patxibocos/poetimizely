package com.patxi.poetimizely.generator

import com.optimizely.ab.Optimizely
import com.patxi.poetimizely.generator.base.BaseFeature
import com.patxi.poetimizely.generator.base.BaseFeaturesClient
import com.patxi.poetimizely.optimizely.Feature
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec
import java.io.StringWriter

/**
 * The functionality to parse features to type safe code
 * is designed and documented in https://docs.google.com/document/d/1XJap6OKnzAM-C4GoL1Ubs2zKRzJSlIdzcvddOOE2Z7Q.
 */
class FeaturesGenerator(private val packageName: String = "") {
    fun build(features: List<Feature>): String {
        val featuresEnumTypeName = ClassName(packageName, "Features")
        return FileSpec.builder(packageName, "Features")
            .addType(featuresEnumTypeSpec(features, featuresEnumTypeName))
            .addType(featuresClientTypeSpec(featuresEnumTypeName))
            .build().run {
                StringWriter().also { appendable: Appendable ->
                    this.writeTo(appendable)
                }.toString()
            }
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
 * enum class Features : Feature {
 *     NEW_CHECKOUT_PAGE {
 *         override val key: String = "new_checkout_page"
 *     },
 *    ...
 * }
 */
private fun featuresEnumTypeSpec(features: List<Feature>, className: ClassName): TypeSpec =
    // enum Features
    TypeSpec.enumBuilder(className)
        .addSuperinterface(BaseFeature::class)
        .also { typeSpecBuilder ->
            // each feature is a const with the key
            features.forEach {
                typeSpecBuilder.addEnumConstant(
                    it.key.optimizelyFeatureKeyToEnumConstant(),
                    TypeSpec.anonymousClassBuilder().addProperty(
                        PropertySpec.builder("key", String::class, KModifier.OVERRIDE)
                            .initializer("%S", it.key).build()
                    ).build()
                )
            }
        }.build()

/**
 * This will produce something like this.
 *
 * class TestFeaturesClient(
 *     optimizely: Optimizely,
 *     userId: String
 * ) : FeaturesClient<Features>(optimizely, userId)
 */
private fun featuresClientTypeSpec(featuresEnumTypeName: TypeName): TypeSpec {
    val featuresClientClazz = BaseFeaturesClient::class.java
    val featuresClientClassName =
        ClassName(featuresClientClazz.`package`.name, featuresClientClazz.simpleName).parameterizedBy(
            featuresEnumTypeName
        )
    return TypeSpec.classBuilder("FeaturesClient")
        .superclass(featuresClientClassName)
        .addSuperclassConstructorParameter("optimizely")
        .addSuperclassConstructorParameter("userId")
        .primaryConstructor(
            FunSpec.constructorBuilder()
                .addParameter("optimizely", Optimizely::class)
                .addParameter("userId", String::class)
                .build()
        ).build()
}

@file:JvmName("Main")

package com.patxi.poetimizely

import com.patxi.poetimizely.optimizely.Experiment
import com.patxi.poetimizely.optimizely.ListExperiments
import com.patxi.poetimizely.optimizely.OptimizelyService
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

fun buildOptimizelyService(optimizelyToken: String): OptimizelyService {
    val httpClient = OkHttpClient.Builder().addInterceptor { chain ->
        val newRequest = chain.request().newBuilder().addHeader("Authorization", "Bearer $optimizelyToken").build()
        chain.proceed(newRequest)
    }.build()

    val retrofit = Retrofit.Builder()
        .baseUrl("https://api.optimizely.com/v2/")
        .addConverterFactory(GsonConverterFactory.create())
        .client(httpClient)
        .build()

    return retrofit.create(OptimizelyService::class.java)
}

fun buildExperimentObject(experiment: Experiment) {
    val variantsEnumClassName = ClassName("", "${experiment.key}Variants")
    val variantsEnum = TypeSpec.enumBuilder(variantsEnumClassName)
        .addSuperinterface(com.patxi.poetimizely.generator.Variant::class).apply {
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

    val variantClazz = com.patxi.poetimizely.generator.Variant::class.java
    val experimentObjectClassName =
        ClassName(variantClazz.`package`.name, variantClazz.simpleName).parameterizedBy(variantsEnumClassName)
    val experimentObject = TypeSpec.objectBuilder(experiment.key)
        .addSuperinterface(experimentObjectClassName).build()
}

fun main(args: Array<String>) {
    require(args.size == 2) { "2 arguments expected: --args=\"<optimizelyProjectId> <optimizelyToken>\"" }
    val (projectIdString, optimizelyToken) = args
    val projectId = requireNotNull(projectIdString.toLongOrNull()) { "optimizelyProjectId must be a number" }

    val service: OptimizelyService = buildOptimizelyService(optimizelyToken)
    runBlocking {
        val listExperiments = ListExperiments(service)
        val experiments = listExperiments(projectId = projectId)
        experiments.forEach(::buildExperimentObject)
    }
}
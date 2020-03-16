@file:JvmName("Main")

package com.patxi.poetimizely

import com.patxi.poetimizely.optimizely.Experiment
import com.patxi.poetimizely.optimizely.ListExperiments
import com.patxi.poetimizely.optimizely.OptimizelyService
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File

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

fun buildExperimentObject(experiment: Experiment): File {
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

    val experimentClazz = com.patxi.poetimizely.generator.Experiment::class.java
    val experimentObjectClassName =
        ClassName(experimentClazz.`package`.name, experimentClazz.simpleName).parameterizedBy(variantsEnumClassName)
    val arrayClassName =  ClassName("kotlin", "Array")
    val listOfVariants = arrayClassName.parameterizedBy(variantsEnumClassName)

    val experimentObject = TypeSpec.objectBuilder(experiment.key)
        .addSuperinterface(experimentObjectClassName).addProperty(
            PropertySpec.builder("key", String::class, KModifier.OVERRIDE).initializer("%S", experiment.key).build()
        ).addProperty(
            PropertySpec.builder("variants", listOfVariants, KModifier.OVERRIDE).initializer(CodeBlock.of("${variantsEnumClassName}.values()")).build()
        ).build()

    return File(experiment.key).also { file ->
        FileSpec.builder("", experiment.key).addType(variantsEnum).addType(experimentObject).build().writeTo(file)
    }
}

fun main(args: Array<String>) {
    require(args.size == 2) { "2 arguments expected: --args=\"<optimizelyProjectId> <optimizelyToken>\"" }
    val (projectIdString, optimizelyToken) = args
    val projectId = requireNotNull(projectIdString.toLongOrNull()) { "optimizelyProjectId must be a number" }

    val service: OptimizelyService = buildOptimizelyService(optimizelyToken)
    runBlocking {
        val listExperiments = ListExperiments(service)
        val experiments = listExperiments(projectId = projectId)
        experiments.forEach {
            buildExperimentObject(it)
        }
    }
}